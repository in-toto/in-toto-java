package io.github.in_toto.transporters;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.in_toto.exceptions.TransporterException;
import io.github.in_toto.models.Link;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Link.LinkBuilder;
import io.github.in_toto.transporters.InTotoServiceTransporter;

class InTotoServiceTransporterTest {
    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    Random rand = new Random();



    MockLowLevelHttpResponse mockResponse = new MockLowLevelHttpResponse();
    
    @Test
    void testConstructor() throws MalformedURLException, URISyntaxException {
        // with https
        InTotoServiceTransporter<Link> transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, true);
        assertEquals(new URI("https://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic"), transporter.getInTotoServiceUri());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json; charset=UTF-8");
        assertEquals(headers, transporter.getHeaders());
        
        // with http
        transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, false);
        assertEquals(new URI("http://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic"), transporter.getInTotoServiceUri());
    }
    
    @Test
    void testToString() {
        InTotoServiceTransporter<Link> transporter = new InTotoServiceTransporter<>("Supplychains/domain1/app1/petclinic", "localhost", 1234, true);
        assertEquals("InTotoServiceTransporter [inTotoServiceUrl=https://localhost:1234/api/repository/metablock/Supplychains/domain1/app1/petclinic]",
                transporter.toString());
    }
    
    @Test
    public void testDumpLink() throws MalformedURLException {
        Metablock<Link> metablock = new Metablock<Link>(new LinkBuilder("linktest").build(), null);
        HttpTransport transportMock = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        // Return success when count is more than callsBeforeSuccess
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(200);
                        response.setContentType(Json.MEDIA_TYPE);
                        Gson gson = new Gson();
                        response.setContent(gson.toJson(metablock));
                        return response;
                    }
                };
            }
        };
        InTotoServiceTransporter<Link> transport = new InTotoServiceTransporter<Link>("Supplychains/domain1/app1/petclinic", "mockhost", 1234, false);
        String metablockId = metablock.getSigned().getName()+"/"+metablock.getShortKeyId()+"/"+metablock.getSigned().getShortHash();
        transport.setTransport(transportMock);
        transport.dump(metablock);
        // and load again
        Metablock<Link> metablock2 = transport.load(metablockId, this.metablockType);
        assertEquals(metablock, metablock2);
    }
   
    @Test //(expected = RuntimeException.class)
    public void testDumpLinkExc() throws MalformedURLException {
        InTotoServiceTransporter<Link> transport = new InTotoServiceTransporter<Link>("Supplychains/domain1/app1/chainOther", "mockhost", 1234, false);
        HttpTransport transportMock = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        // Return success when count is more than callsBeforeSuccess
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                        return response;
                    }
                };
            }
        };
        Metablock<Link> metablock = new Metablock<Link>(new LinkBuilder("linktest"+rand.nextInt()).build(), null);
        transport.setTransport(transportMock);
        Throwable exception = assertThrows(TransporterException.class, () -> {
            transport.dump(metablock);
          });
        
        assertEquals("couldn't serialize to in-toto service: com.google.api.client.http.HttpResponseException: 400", exception.getMessage());
        
    }
    
    @Test
    public void testLoadLink() throws MalformedURLException {
        Metablock<Link> metablock = new Metablock<Link>(new LinkBuilder("linktest").build(), null);
        InTotoServiceTransporter<Link> transport = new InTotoServiceTransporter<Link>("Supplychains/domain1/app1/linktest", "mockhost", 1234, false);
        HttpTransport transportMock = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        // Return success when count is more than callsBeforeSuccess
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(201);
                        response.setContentType(Json.MEDIA_TYPE);
                        Gson gson = new Gson();
                        response.setContent(gson.toJson(metablock));
                        return response;
                    }
                };
            }
        };
        transport.setTransport(transportMock);
        
        Metablock<Link> metablock2 = transport.load("Supplychains/domain1/app1/chainOther", this.metablockType);
        assertEquals(metablock, metablock2);
    }
    
    @Test
    public void testEqualsAndHash() {
        InTotoServiceTransporter<Link> transport = new InTotoServiceTransporter<Link>("Supplychains/domain1/app1/linktest", "mockhost", 1234, false);
        InTotoServiceTransporter<Link> transport2 = new InTotoServiceTransporter<Link>("Supplychains/domain1/app1/linktest", "mockhost", 1234, false);
        assertEquals(transport, transport2);
        assertEquals(transport.hashCode(), transport2.hashCode());
    }

}
