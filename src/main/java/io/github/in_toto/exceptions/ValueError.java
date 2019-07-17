package io.github.in_toto.exceptions;

public class ValueError extends RuntimeException {
    
    private static final long serialVersionUID = -3389967170705792300L;

    public ValueError(String message) {
        super(message);
    }

}
