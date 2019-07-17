package io.github.in_toto.transporters;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import io.github.in_toto.exceptions.TransporterException;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Signable;

public class InTotoServiceTransporter<S extends Signable> implements Transporter<S> {

    static final Logger logger = Logger.getLogger(InTotoServiceTransporter.class.getName());
    
    private URL inTotoServiceUrl;
    
    private HttpHeaders headers = new HttpHeaders();
    
    public InTotoServiceTransporter(String supplyChainId, String hostname, int port, Boolean secure) {
        String protocol = "http";
        if (secure != null && secure) {
            protocol = "https";
        }
        try {
            this.inTotoServiceUrl = new URL(protocol, hostname, port, "/api/repository/metablock/"+supplyChainId);
        } catch (MalformedURLException e) {
            throw new TransporterException(e.getMessage());
        }
        
        this.headers.setAccept(Json.MEDIA_TYPE);
    }

    @Override
    public void dump(Metablock<S> metablock) {
        try {
            HttpRequest request = new NetHttpTransport().createRequestFactory().buildPostRequest(
                    new GenericUrl(inTotoServiceUrl),
                    ByteArrayContent.fromString("application/json", metablock.toJson())).setHeaders(this.headers);
            request.execute();
            /*
             * FIXME: should handle error codes and other situations more appropriately, but
             * this gets the job done for a PoC
             */
        } catch (IOException e) {
            throw new TransporterException("couldn't serialize to HTTP server: " + e);
        }
    }

    @Override
    public <K extends Signable> Metablock<K> load(String id, Type type) {
        Metablock<K> metablock = null;
        String url = inTotoServiceUrl+"/"+id;
        try {
            HttpRequest request = new NetHttpTransport().createRequestFactory().buildGetRequest(
                    new GenericUrl(url)).setHeaders(this.headers);
            HttpResponse response = request.execute();
            String json = null;
            try (final Reader reader = new InputStreamReader(response.getContent())) {
                json = CharStreams.toString(reader);
            }
          
            Gson gson = new Gson();
            metablock = gson.fromJson(json, type);
            
        } catch (IOException e) {
            throw new TransporterException("couldn't serialize to HTTP server: " + e);
        }
        
        return metablock;
        
    }

    @Override
    public String toString() {
        return "InTotoServiceTransporter [inTotoServiceUrl=" + inTotoServiceUrl + "]";
    }

    public URL getInTotoServiceUrl() {
        return inTotoServiceUrl;
    }

    public void setInTotoServiceUrl(URL inTotoServiceUrl) {
        this.inTotoServiceUrl = inTotoServiceUrl;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

}
