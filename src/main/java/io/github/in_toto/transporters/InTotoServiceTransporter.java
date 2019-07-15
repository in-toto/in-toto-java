package io.github.in_toto.transporters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Signable;
import io.github.in_toto.transporters.Transporter;

public class InTotoServiceTransporter<S extends Signable> implements Transporter<S> {
	
	private URL inTotoServiceUrl;
	
	public InTotoServiceTransporter(String supplyChainId, String hostname, int port, Boolean secure) {
		String protocol = "http";
		if (secure != null && secure)
			protocol = "https";
		try {
			this.inTotoServiceUrl = new URL(protocol, hostname, port, "/api/repository/metablock/"+supplyChainId);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void dump(Metablock<S> metablock) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		try {
			HttpRequest request = new NetHttpTransport().createRequestFactory().buildPostRequest(
					new GenericUrl(inTotoServiceUrl),
					ByteArrayContent.fromString("application/json", metablock.toJson())).setHeaders(headers);
			HttpResponse response = request.execute();
			System.out.println(response.parseAsString());
			/*
			 * FIXME: should handle error codes and other situations more appropriately, but
			 * this gets the job done for a PoC
			 */
		} catch (IOException e) {
			throw new RuntimeException("couldn't serialize to HTTP server: " + e);
		}
	}

	@Override
	public <K extends Signable> Metablock<K> load(String id, Type type) {
		Metablock<K> metablock = null;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		String url = inTotoServiceUrl+"/"+id;
		try {
			HttpRequest request = new NetHttpTransport().createRequestFactory().buildGetRequest(
					new GenericUrl(url)).setHeaders(headers);
			HttpResponse response = request.execute();
			String json = null;
			try (final Reader reader = new InputStreamReader(response.getContent())) {
				json = CharStreams.toString(reader);
	        }
	      
	        Gson gson = new Gson();
			metablock = gson.fromJson(json, type);
			
		} catch (IOException e) {
			throw new RuntimeException("couldn't serialize to HTTP server: " + e);
		}
		
		return metablock;
		
	}

	@Override
	public String toString() {
		return "InTotoServiceTransporter [inTotoServiceUrl=" + inTotoServiceUrl + "]";
	}

}
