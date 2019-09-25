package io.github.in_toto.exceptions;

import io.github.in_toto.models.layout.Step;

public class ThresholdVerificationError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public ThresholdVerificationError(Step step, int found) {
        super(getThresholdMessage(step, found));
    }
    
    private static String getThresholdMessage(Step step, int found) {
        return String.format("Step [%s] requires at least [%s] links validly signed by different authorized functionaries. Only found [%s].", step.getName(), step.getThreshold(), found);
    }

}
