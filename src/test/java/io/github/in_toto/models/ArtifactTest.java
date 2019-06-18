package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Link.LinkBuilder;

class ArtifactTest {

	String workingDir = System.getProperty("user.dir");
	String testDir = "src/test/resources/artifact_test/record_artifacts";
	RSAKey bobKey = RSAKey.read("src/test/resources/artifact_test/record_artifacts/functionary_bob/bob");
	
	@Test
    @DisplayName("Test Record Artifacts File")
    public void testRecordAFile() throws IOException {
		String bobDir = testDir + "/functionary_bob";
		Artifact fooArtifact = new Artifact("demo-project/foo.py", new Artifact(bobDir+"/demo-project/foo.py").getArtifactHashes());
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(bobDir);
		testLinkBuilder.addMaterial(Arrays.asList("demo-project"));
		Link fooLink = testLinkBuilder.build();
		assertTrue(fooLink.getMaterials().contains(fooArtifact));
		
    }
	
	@Test
    @DisplayName("Test Record Artifacts File and exlude link file")
    public void testRecordADirectory() throws IOException {
		String bobDir = testDir + "/functionary_bob";
		Artifact fooArtifact = new Artifact("demo-project/foo.py", new Artifact(bobDir+"/demo-project/foo.py").getArtifactHashes());
		Artifact linkArtifact = new Artifact("demo-project/foo.link", new Artifact(bobDir+"/demo-project/foo.link").getArtifactHashes());
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(bobDir);
		testLinkBuilder.addMaterial(Arrays.asList("demo-project"));
		Link fooLink = testLinkBuilder.build();
		assertTrue(fooLink.getMaterials().contains(fooArtifact));
		assertFalse(fooLink.getMaterials().contains(linkArtifact));
    }
	
	@Test
    @DisplayName("Test record with base path not set")
    public void testRecordWithBasepathNotSet() throws IOException {
		String demoDir = testDir + "/functionary_bob/demo-project/";
		Artifact fooArtifact = new Artifact(demoDir+"foo.py");
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.addMaterial(Arrays.asList(demoDir));
		Link fooLink = testLinkBuilder.build();
		assertTrue(fooLink.getMaterials().contains(fooArtifact));
    }
	
	@Test
    @DisplayName("Test record with follow symbolic link")
    public void testRecordWithFollowSymLink() throws IOException {
		String carlDir = testDir + "/functionary_carl";
		Artifact alicePub = new Artifact("soft_link_test/alice.pub", new Artifact(testDir+"/final_product/alice.pub").getArtifactHashes());
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(carlDir);
		testLinkBuilder.addMaterial(Arrays.asList(""));
		Link fooLink = testLinkBuilder.build();
		assertFalse(fooLink.getMaterials().contains(alicePub));
		
		fooLink = testLinkBuilder.setFollowSymlinkDirs(true).addMaterial(Arrays.asList("")).build();
		assertTrue(fooLink.getMaterials().contains(alicePub));
    }

}
