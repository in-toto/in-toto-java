package io.github.in_toto.exceptions;

import io.github.in_toto.lib.InTotoDateTimeFormatter;
import io.github.in_toto.models.layout.Layout;

public class LayoutExpiredError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public LayoutExpiredError(Layout layout) {
        super(getExpiryDate(layout));
    }
    
    private static String getExpiryDate(Layout layout) {
        return String.format("Layout expired on [%s]", InTotoDateTimeFormatter.DATE_TIME_FORMATTER.format(layout.getExpires()));
    }

}
