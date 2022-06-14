package com.handshape.justneuralnets;

import org.nd4j.evaluation.classification.Evaluation;

/**
 * @author JoTurner
 */
public interface JNNTrainingListener {

    public void startEpoch(int epoch);

    public void endEpoch(int epoch);

    public void evaluation(Evaluation evaluation, double lastScore);

}
