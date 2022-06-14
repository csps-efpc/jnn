package com.handshape.justneuralnets;

import org.nd4j.evaluation.classification.Evaluation;

/**
 * @author JoTurner
 */
public class ConsoleTrainingListener implements JNNTrainingListener {

    @Override
    public void endEpoch(int epoch) {
        System.out.println("Epoch " + epoch + " finished.");
    }

    @Override
    public void evaluation(Evaluation evaluation, double lastScore) {
        System.out.println("Score: " + lastScore);
        System.out.println(evaluation.toString());
    }

    @Override
    public void startEpoch(int epoch) {
        System.out.println("Epoch " + epoch + " started.");
    }

}
