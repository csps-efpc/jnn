package com.handshape.justneuralnets.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author JoTurner
 */
public class InteractiveDesignUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void init() throws Exception {
        // Happens before start()
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream("InteractiveDesignUI.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("JNN Interactive Designer");
        stage.getIcons().clear();
        stage.getIcons().add(new Image(getClass().getResource("brain.png").toExternalForm()));
        stage.setScene(scene);
        stage.show();
        InteractiveDesignUIController controller = loader.getController();
//        Platform.runLater(() -> {
//            controller.setEvaluator(evaluator);
//        });
        Parameters cliParms = this.getParameters();
        if (!cliParms.getUnnamed().isEmpty()) {
            // Load the moel requested.
        }
    }

}
