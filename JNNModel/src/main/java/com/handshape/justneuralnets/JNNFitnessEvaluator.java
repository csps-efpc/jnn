package com.handshape.justneuralnets;

import com.handshape.justneuralnets.datafields.LabelDataField;
import com.handshape.justneuralnets.input.ITabularInput;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JoTurner
 */
public class JNNFitnessEvaluator {

    public static FitnessEvaluation evaluateFitness(JNNModelEvaluator evaluator, ITabularInput input) {
        MutableLong truePositives = new MutableLong();
        MutableLong trueNegatives = new MutableLong();
        MutableLong falsePositives = new MutableLong();
        MutableLong falseNegatives = new MutableLong();
        MutableLong count = new MutableLong();

        JNNModelSpec spec = evaluator.getSpec();
        LabelDataField labelField = spec.getLabelDataField();
        input.forEach((Map<String, String> row) -> {
            count.increment();
            try {
                double evaluation = evaluator.evaluate(row);
                double actual = labelField.normalizeInputToFeatures(row.get(labelField.getName()))[1];
                if (evaluation > 0.5) {
                    // Positive
                    if (actual > 0.5) {
                        // true
                        truePositives.increment();
                    } else {
                        // false
                        falsePositives.increment();
                    }
                } else {
                    // Negative
                    if (actual > 0.5) {
                        // false
                        falseNegatives.increment();
                    } else {
                        // true
                        trueNegatives.increment();
                    }
                }
            } catch (JNNModelSpec.InvalidInputException ex) {
                Logger.getLogger(JNNFitnessEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return new FitnessEvaluation(truePositives.longValue(), trueNegatives.longValue(), falsePositives.longValue(), falseNegatives.longValue());
    }

    public static class FitnessEvaluation {

        private final long truePositives;
        private final long trueNegatives;
        private final long falsePositives;
        private final long falseNegatives;

        public FitnessEvaluation(long truePositives, long trueNegatives, long falsePositives, long falseNegatives) {
            this.truePositives = truePositives;
            this.trueNegatives = trueNegatives;
            this.falsePositives = falsePositives;
            this.falseNegatives = falseNegatives;
        }

        /**
         * @return the truePositives
         */
        public long getTruePositives() {
            return truePositives;
        }

        /**
         * @return the trueNegatives
         */
        public long getTrueNegatives() {
            return trueNegatives;
        }

        /**
         * @return the falsePositives
         */
        public long getFalsePositives() {
            return falsePositives;
        }

        /**
         * @return the falseNegatives
         */
        public long getFalseNegatives() {
            return falseNegatives;
        }

        // The Matthews Correlation Coefficient - https://en.wikipedia.org/wiki/Matthews_correlation_coefficient
        public double getMCC() {
            double tp = getTruePositives();
            double fp = getFalsePositives();
            double tn = getTrueNegatives();
            double fn = getFalseNegatives();
            return ((tp * tn) - (fp * fn)) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
        }

    }

}
