package io.github.in_toto.exceptions;

import io.github.in_toto.models.link.Link;

public class LinkNotFoundError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public LinkNotFoundError(Link notFoundLink) {
        super(String.format("Link not found [%s]", notFoundLink.getName()));
    }

}
