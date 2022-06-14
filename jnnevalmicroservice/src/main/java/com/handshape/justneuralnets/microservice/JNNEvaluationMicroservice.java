package com.handshape.justneuralnets.microservice;

import com.handshape.justneuralnets.JNNModelEvaluator;
import com.handshape.justneuralnets.JNNModelSpec;
import com.handshape.justneuralnets.datafields.DataField;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JoTurner
 */
public class JNNEvaluationMicroservice {

    private static final String FORWARDED_USER_HEADER = "X-Forwarded-User";
    private static final String ANONYMOUS_USER = "anon";
    
    private File modelFile;
    private File schemeFile;
    private HttpServer server;

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("This service takes three parameters: a port, a path to a .jnn file, and a path to an .mdl file.");
            //System.exit(-1);
            args = new String[]{"9090", "best-model.jnn", "best-model.mdl"};
        }

        int port = Integer.parseInt(args[0]);
        JNNEvaluationMicroservice service = new JNNEvaluationMicroservice();
        service.setSchemeFile(new File(args[1]));
        service.setModelFile(new File(args[2]));
        service.start(port);
    }

    public synchronized void start(int port) throws IOException {
        stop();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new EvaluationHandler());
        server.setExecutor(null);
        server.start();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(5);
            server = null;
        }
    }

    /**
     * @return the modelFile
     */
    public File getModelFile() {
        return modelFile;
    }

    /**
     * @param modelFile the modelFile to set
     */
    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }

    /**
     * @return the schemeFile
     */
    public File getSchemeFile() {
        return schemeFile;
    }

    /**
     * @param schemeFile the schemeFile to set
     */
    public void setSchemeFile(File schemeFile) {
        this.schemeFile = schemeFile;
    }

    /**
     * @return the server
     */
    public HttpServer getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(HttpServer server) {
        this.server = server;
    }

    class EvaluationHandler implements HttpHandler {

        JNNModelEvaluator evaluator;

        public EvaluationHandler() throws IOException {
            evaluator = JNNModelEvaluator.fromFile(getModelFile(), getSchemeFile());
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String user = exchange.getRequestHeaders().containsKey(FORWARDED_USER_HEADER) ? exchange.getRequestHeaders().getFirst(FORWARDED_USER_HEADER) : ANONYMOUS_USER;
            Logger.getLogger(JNNEvaluationMicroservice.class.getName()).log(Level.INFO, user + " - " + exchange.getRequestURI());
            Map<String, String> evaluationData = splitQuery(exchange.getRequestURI());
            if (evaluationData != null && !evaluationData.isEmpty()) {
                int code = 200;
                String response;
                try {
                    response = new DecimalFormat("#0.0000").format(evaluator.evaluate(evaluationData));
                } catch (JNNModelSpec.InvalidInputException ex) {
                    Logger.getLogger(JNNEvaluationMicroservice.class.getName()).log(Level.SEVERE, null, ex);
                    response = "Invalid input: " + ex.getMessage();
                    code = 500;
                }
                sendResponse(exchange, code, "text/plain", response);
            } else {
                Document doc = Jsoup.parse(getClass().getResourceAsStream("/www/index.html"), "UTF-8", exchange.getRequestURI().toASCIIString());
                Element form = doc.body().appendElement("form");
                form.attr("method", "GET");
                form.attr("action", exchange.getRequestURI().toASCIIString());
                for (DataField field : evaluator.getSpec().getDataFields()) {
                    form.appendText(field.getName());
                    form.appendElement("br");
                    form.appendElement("input").attr("type", "text").attr("class", "featureField").attr("name", field.getName());
                    form.appendElement("br");
                }
                form.appendElement("input").attr("type", "submit");
                sendResponse(exchange, 200, "text/html", doc.outerHtml());
            }
        }

        private void sendResponse(HttpExchange exchange, int responseCode, String contentType, String response) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(responseCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        public Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
            Map<String, String> query_pairs = new LinkedHashMap<>();
            String query = url.getRawQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                    }
                }
            }
            return query_pairs;
        }
    }

}
