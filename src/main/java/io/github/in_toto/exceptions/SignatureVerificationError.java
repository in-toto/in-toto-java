package io.github.in_toto.exceptions;

public class SignatureVerificationError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public SignatureVerificationError(String message) {
        super(message);
    }

}
