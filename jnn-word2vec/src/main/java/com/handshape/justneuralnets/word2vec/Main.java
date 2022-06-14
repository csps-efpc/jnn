package com.handshape.justneuralnets.word2vec;

import com.handshape.justneuralnets.repo.JnnRepoException;
import com.handshape.justneuralnets.repo.JnnRepository;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JoTurner
 */
public class Main {

    private static final int DEFAULT_NUMBER_OF_FEATURES = 200;
    private static final int DEFAULT_NUMBER_OF_OCCURRENCES = 5;
    private static final int DEFAULT_WINDOW_SIZE = 5;

    public static void main(String[] arrghs) {
        try {
            JnnRepository repo = JnnRepository.getInstance();
            String fileSpec = ".";
            int numFeatures = DEFAULT_NUMBER_OF_FEATURES;
            int numOccurrences = DEFAULT_NUMBER_OF_OCCURRENCES;
            int windowsSize = DEFAULT_WINDOW_SIZE;
            Options options = buildOptions();
            DefaultParser parser = new DefaultParser();
            try {
                // parse the command line arguments
                CommandLine line = parser.parse(options, arrghs);
                if (line.hasOption("h")) {
                    printHelp(options);
                    return;
                }
                if (line.hasOption("s")) {
                    fileSpec = line.getOptionValue("s");
                }
                if (line.hasOption("n")) {
                    numFeatures = ((Number) line.getParsedOptionValue("n")).intValue();
                }
                if (line.hasOption("w")) {
                    windowsSize = ((Number) line.getParsedOptionValue("w")).intValue();
                }
                if (line.hasOption("m")) {
                    numOccurrences = ((Number) line.getParsedOptionValue("m")).intValue();
                }
            } catch (ParseException exp) {
                System.err.println("Parsing failed.  Reason: " + exp.getMessage());
                printHelp(options);
                System.exit(-1);
            }
            // The file is a unique temp file, so we can do this safely on Windows in spite of the partially-broken MMIO on the platform.
            DB db = DBMaker.tempFileDB().concurrencyDisable().fileMmapEnable().fileMmapPreclearDisable().fileDeleteAfterClose().make();
            // Something to watch out for is endpoint security agents on Windows that lock MMIO storage at random points (when the 
            final NavigableSet<String> sentences = db.treeSet("sentences", Serializer.STRING).createOrOpen();

            Files.walk(Paths.get(fileSpec)).filter(Files::isRegularFile).filter(p -> {
                return p.toString().endsWith(".txt");
            }).forEach((Path p) -> {
                System.out.println(p.toString());
                try {
                    String body = IOUtils.toString(p.toUri(), "UTF-8");
                    for (String sentence : body.split("[!\\.?\"\\:][ \\t\\n]")) {
                        sentence = sentence.replaceAll("[^\\p{L}]+", " ");
                        sentence = sentence.replaceAll(" +", " ");
                        sentences.add(sentence.toLowerCase());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            SentenceIterator iter = new SentenceIterator() {
                Iterator<String> iter = sentences.iterator();
                SentencePreProcessor preProcessor = null;

                @Override
                public String nextSentence() {
                    if (preProcessor == null) {
                        return iter.next();
                    }
                    return preProcessor.preProcess(iter.next());
                }

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public void reset() {
                    iter = sentences.iterator();
                }

                @Override
                public void finish() {
// NO-OP
                }

                @Override
                public SentencePreProcessor getPreProcessor() {
                    return preProcessor;
                }

                @Override
                public void setPreProcessor(SentencePreProcessor spp) {
                    preProcessor = spp;
                }
            };

            TokenizerFactory t = new DefaultTokenizerFactory();
            t.setTokenPreProcessor(new CommonPreprocessor());
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Building model....");
            Word2Vec vec = new Word2Vec.Builder()
                    .minWordFrequency(numOccurrences)
                    .layerSize(numFeatures)
                    //.seed(42)
                    .windowSize(windowsSize)
                    .iterate(iter)
                    .tokenizerFactory(t)
                    .build();

            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Fitting Word2Vec model....");
            vec.fit();
            WordVectorSerializer.writeWord2VecModel(vec, "word2vec.w2v");
            vec = null;
            vec = WordVectorSerializer.readWord2VecModel("word2vec.w2v", true);
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Basic term tests:");
            for (String word : Arrays.asList("phone", "money", "computer", "deck", "regulation", "minister")) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Closest words to \"{0}\":", word);
                Collection<String> lst = vec.wordsNearest(word, 10);
                System.out.println(lst);
            }
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Non-English term tests:");
            for (String word : Arrays.asList("rue", "marché", "pays", "soulier")) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Closest words to \"{0}\":", word);
                Collection<String> lst = vec.wordsNearest(word, 10);
                System.out.println(lst);
            }
            System.out.println("JNN ID: " + repo.put(FileUtils.readFileToByteArray(new File("word2vec.w2v"))));
//            UIServer server = UIServer.getInstance();
//            System.out.println("Started on port " + server.getPort());
        } catch (JnnRepoException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar word2vec.jar", options);
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(Option.builder("s").hasArg().longOpt("source-directory").type(String.class).desc("Directory from which to read the text corpus. Defaults to working directory.").build());
        options.addOption(Option.builder("n").hasArg().longOpt("num-features").type(Number.class).desc("Number of output features/dimensions. Default: " + DEFAULT_NUMBER_OF_FEATURES).build());
        options.addOption(Option.builder("m").hasArg().longOpt("min-occurrences").type(Number.class).desc("The minimum number of occurrences that a word must have to be considered. Default:  " + DEFAULT_NUMBER_OF_OCCURRENCES).build());
        options.addOption(Option.builder("w").hasArg().longOpt("window-size").type(Number.class).desc("The width of the rolling window applied to word contexts. Default:  " + DEFAULT_WINDOW_SIZE).build());
        options.addOption(Option.builder("h").longOpt("help").desc("Display this help.").build());
        return options;
    }
}
