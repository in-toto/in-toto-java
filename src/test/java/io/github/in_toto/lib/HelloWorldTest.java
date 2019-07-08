package io.github.in_toto.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.FileLinkTransporter;
import io.github.in_toto.models.Link;
import io.github.in_toto.models.Link.LinkBuilder;
import io.github.in_toto.models.Metablock;

class HelloWorldTest {
    
    @TempDir
    Path temporaryFolder;
    
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
	void testHelloWorld() throws IOException {
		Key thiskey = RSAKey.read("src/test/resources/lib_test/somekey.pem");

		System.out.println("Loaded key ID: " + thiskey.computeKeyId());		

		LinkBuilder linkBuilder = new LinkBuilder("test");

		linkBuilder.addMaterial(Arrays.asList("alice"));
		System.out.println("dumping file...");
		Link link = linkBuilder.build();
		Metablock<Link> linkMetablock = new Metablock<Link>(link, null);
		linkMetablock.sign(thiskey);
		String linkFile = Files.createFile(temporaryFolder.resolve(linkMetablock.getFullName())).toString();
        FileLinkTransporter transporter = new FileLinkTransporter(temporaryFolder.toString());
    	transporter.dump(linkMetablock);
	}

}
