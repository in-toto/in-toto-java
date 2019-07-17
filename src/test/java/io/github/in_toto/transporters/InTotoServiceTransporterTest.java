package io.github.in_toto.transporters;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import io.github.in_toto.models.Link;
import io.github.in_toto.transporters.InTotoServiceTransporter;

class InTotoServiceTransporterTest {
    
    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse();
    
    @Test
    void testConstructor() throws MalformedURLException {
        // with https
        InTotoServiceTransporter<Link> transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, true);
        assertEquals(new URL("https://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic"), transporter.getInTotoServiceUrl());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json; charset=UTF-8");
        assertEquals(headers, transporter.getHeaders());
        
        // with http
        transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, false);
        assertEquals(new URL("http://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic"), transporter.getInTotoServiceUrl());
    }
    
    @Test
    void testToString() {
        InTotoServiceTransporter<Link> transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, true);
        assertEquals("InTotoServiceTransporter [inTotoServiceUrl=https://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic]",
                transporter.toString());
    }

}
