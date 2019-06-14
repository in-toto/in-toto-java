package io.github.in_toto.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.FileTransporter;
import io.github.in_toto.models.Link;
import io.github.in_toto.models.Link.LinkBuilder;
import io.github.in_toto.models.Metablock;

class HelloWorldTest {
	File fl = new File("alice");
	
	@BeforeEach
	void setUp() throws Exception {
		try {

			fl.createNewFile();

		} catch (IOException e) {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			throw new RuntimeException("The file alice couldn't be created");
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		fl.delete();
		new File("test.0b70eafb.link").delete();
	}

	@Test
	void testHelloWorld() {
		Key thiskey = RSAKey.read("src/test/resources/somekey.pem");

		System.out.println("Loaded key ID: " + thiskey.computeKeyId());		

		LinkBuilder linkBuilder = new LinkBuilder("test");

		linkBuilder.addMaterial("alice");
		System.out.println("dumping file...");
		Link link = linkBuilder.build();
		Metablock<Link> linkMetablock = new Metablock<Link>(link, null);
		linkMetablock.sign(thiskey);
		FileTransporter transport = new FileTransporter();
		try {
			transport.dump(new URI("test.0b70eafb.link"), linkMetablock);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
