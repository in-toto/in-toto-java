package io.github.in_toto.transporters;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

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
			this.inTotoServiceUrl = new URL(protocol, hostname, port, "/api/repository/si/"+supplyChainId);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void dump(Metablock<S> metablock) {
		try {
			HttpRequest request = new NetHttpTransport().createRequestFactory().buildPostRequest(
					new GenericUrl(inTotoServiceUrl),
					ByteArrayContent.fromString("application/json", metablock.toJson()));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "InTotoServiceTransporter [inTotoServiceUrl=" + inTotoServiceUrl + "]";
	}

}
