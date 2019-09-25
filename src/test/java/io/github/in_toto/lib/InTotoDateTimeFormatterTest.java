package io.github.in_toto.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InTotoDateTimeFormatterTest {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testDateTimeFormatter() {
        ZonedDateTime date = ZonedDateTime.of(2019, 7, 14, 17, 55, 21, 0, ZoneId.of("GMT"));
        String zonedDateTime = InTotoDateTimeFormatter.DATE_TIME_FORMATTER.format(date);
        assertEquals("2019-07-14T17:55:21Z", zonedDateTime);
    }

}
