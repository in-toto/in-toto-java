package io.github.in_toto.lib;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class InTotoDateTimeFormatter {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("GMT"));
    
    private InTotoDateTimeFormatter() {}

}
