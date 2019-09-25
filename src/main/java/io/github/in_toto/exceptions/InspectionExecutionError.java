package io.github.in_toto.exceptions;

import io.github.in_toto.models.layout.Inspection;

public class InspectionExecutionError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public InspectionExecutionError(Inspection inspection, String message) {
        super(String.format("Exception during execution of inspection [%s] message: [%s]", inspection.getName(), message));
    }
}
