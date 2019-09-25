package io.github.in_toto.exceptions;

import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.Link;

public class StepEqualLinksVerificationError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public StepEqualLinksVerificationError(Metablock<Link> reference, Metablock<Link> foundLink) {
        super(getErrorMessage(reference, foundLink));
    }
    
    private static String getErrorMessage(Metablock<Link> reference, Metablock<Link> foundLink) {
        return String.format("Links [%s] and [%s] have different artifacts!", reference.getFullName(), foundLink.getFullName());
        
    }

}
