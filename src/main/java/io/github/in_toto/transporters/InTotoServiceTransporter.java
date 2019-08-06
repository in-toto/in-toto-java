package io.github.in_toto.transporters;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import io.github.in_toto.exceptions.TransporterException;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Signable;

public class InTotoServiceTransporter<S extends Signable> implements Transporter<S> {

    static final Logger logger = Logger.getLogger(InTotoServiceTransporter.class.getName());
    
    private URI inTotoServiceUri;
    
    private HttpHeaders headers = new HttpHeaders();
    
    private HttpTransport transport = new NetHttpTransport();
    
    public InTotoServiceTransporter(String supplyChainId, String hostname, int port, Boolean secure) {
        String protocol = "http";
        if (secure != null && secure) {
            protocol = "https";
        }
        try {
            this.inTotoServiceUri = new URI(protocol, null, hostname, port, "/api/repository/metablock/"+supplyChainId, null, null);
        } catch (URISyntaxException e) {
            throw new TransporterException("Couldn't create URI: " + e);
        }
        
        this.headers.setAccept(Json.MEDIA_TYPE);
    }

    @Override
    public void dump(Metablock<S> metablock) {
        try {
            HttpRequest request = transport.createRequestFactory().buildPostRequest(
                    new GenericUrl(inTotoServiceUri),
                    ByteArrayContent.fromString("application/json", metablock.toJson())).setHeaders(this.headers);
            request.execute();
            /*
             * FIXME: should handle error codes and other situations more appropriately, but
             * this gets the job done for a PoC
             */
        } catch (IOException e) {
            throw new TransporterException("couldn't serialize to in-toto service: " + e);
        }
    }

    @Override
    public <K extends Signable> Metablock<K> load(String id, Type type) {
        Metablock<K> metablock = null;
        try {
            URI uri = new URI(String.format("%s/%s", inTotoServiceUri.toString(), id));
            HttpRequest request = transport.createRequestFactory().buildGetRequest(
                    new GenericUrl(uri)).setHeaders(this.headers);
            HttpResponse response = request.execute();
            String json = null;
            try (final Reader reader = new InputStreamReader(response.getContent())) {
                json = CharStreams.toString(reader);
            }
          
            Gson gson = new Gson();
            metablock = gson.fromJson(json, type);
            
        } catch (IOException | URISyntaxException e) {
            throw new TransporterException("Couldn't get Metablock from in-toto service: " + e);
        }
        
        return metablock;
        
    }

    @Override
    public String toString() {
        return "InTotoServiceTransporter [inTotoServiceUrl=" + inTotoServiceUri + "]";
    }

    public URI getInTotoServiceUri() {
        return inTotoServiceUri;
    }

    public void setInTotoServiceUri(URI inTotoServiceUri) {
        this.inTotoServiceUri = inTotoServiceUri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public HttpTransport getTransport() {
        return transport;
    }

    public void setTransport(HttpTransport transport) {
        this.transport = transport;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((inTotoServiceUri == null) ? 0 : inTotoServiceUri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InTotoServiceTransporter other = (InTotoServiceTransporter) obj;
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        if (inTotoServiceUri == null) {
            if (other.inTotoServiceUri != null) {
                return false;
            }
        } else if (!inTotoServiceUri.equals(other.inTotoServiceUri)) {
            return false;
        }
        // HttpTransport doesn't implement equals
        return (transport == null && other.transport == null)
                || (transport != null && other.transport != null);
    }

}