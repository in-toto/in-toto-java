package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Link.LinkBuilder;

class ArtifactTest {

	String workingDir = System.getProperty("user.dir");
	String testDir = "src/test/resources/artifact_test/record_artifacts";
	RSAKey bobKey = RSAKey.read("src/test/resources/artifact_test/record_artifacts/functionary_bob/bob");
    
    @TempDir
    Path temporaryFolder;
	
	@Test
    @DisplayName("Test Record Artifacts File")
    public void testRecordAFile() throws IOException {
		String bobDir = testDir + "/functionary_bob";
		Artifact fooArtifact = Artifact.recordArtifacts(Arrays.asList("demo-project/foo.py"), null, bobDir).iterator().next();
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
		Artifact fooArtifact = Artifact.recordArtifacts(Arrays.asList("demo-project/foo.py"), null, bobDir).iterator().next();
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(bobDir);
		testLinkBuilder.addMaterial(Arrays.asList("demo-project"));
		Link fooLink = testLinkBuilder.build();
		assertTrue(fooLink.getMaterials().contains(fooArtifact));
		assertTrue(fooLink.getMaterials().size() == 1);
    }
	
	@Test
    @DisplayName("Test record with base path not set")
    public void testRecordWithBasepathNotSet() throws IOException {
		String demoDir = testDir + "/functionary_bob/demo-project/";
		Artifact fooArtifact = Artifact.recordArtifacts(Arrays.asList(demoDir+"foo.py"), null, null).iterator().next();
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
		Artifact alicePub = Artifact.recordArtifacts(Arrays.asList("soft_link_test/alice.pub"), null, carlDir).iterator().next();
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(carlDir);
		testLinkBuilder.addMaterial(Arrays.asList(""));
		Link fooLink = testLinkBuilder.build();
		assertFalse(fooLink.getMaterials().contains(alicePub));
		
		fooLink = testLinkBuilder.setFollowSymlinkDirs(true).addMaterial(Arrays.asList("")).build();
		assertTrue(fooLink.getMaterials().contains(alicePub));
    }
	
	@Test
    @DisplayName("Test record Symlinked files are always recorded.")
    public void testRecordWithFollowSymLinkFiles() throws IOException {
		String carlDir = testDir + "/functionary_carl";
		Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("demo-project/carl"), null, carlDir, false, null);
		assertTrue(artifacts.size() == 1);
    }
	
	@Test
    @DisplayName("Test record Dead symlinks are never recorded.")
    public void testRecordWithoutDeadSymlinks() throws IOException {
		String carlDir = testDir + "/functionary_carl";
		Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("demo-project/bar"), null, carlDir, false, null);
		assertTrue(artifacts.size() == 0);
    }
	
	@Test
    @DisplayName("Test record with normalize line endings")
    public void testRecordWithNormalizeLineEndings() throws IOException {
		String[] lineEndings = {"\n", "\r\n", "\r"};
		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (int i=0; i< lineEndings.length;i++) {
			String path = Files.createFile(temporaryFolder.resolve("file"+i)).toString();
			String msg = "hello"+lineEndings[i]+"toto";
			FileWriter writer = new FileWriter(path);
            writer.write(msg);
            writer.flush();
            writer.close();
            Artifact artifact = Artifact.recordArtifacts(Arrays.asList(path.toString()), null, null, null, true).iterator().next();
			artifacts.add(new Artifact("file", artifact.getHash()));
		}
		Iterator<Artifact> it = artifacts.iterator();
		Artifact prev = it.next();
		assertEquals(prev, it.next());
		assertEquals(prev, it.next());
	}
	
	@Test
    @DisplayName("Test record with normalize line endings with file exactly digest size")
    public void testRecordWithNormalizeLineEndingsGtDigestSize() throws IOException {
		String path = Files.createFile(temporaryFolder.resolve("file")).toString();
		FileOutputStream writer = new FileOutputStream(path);
		// reference with '\n'
		byte[] testLF = {
		        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x0A, 0x41,
		        0x41, 0x41, 0x41, 0x0A, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41      
		    };
		// with CRLF
		byte[] testCRLF = {
		        0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x0D, 0x0A,
		        0x41, 0x41, 0x41, 0x41, 0x0D, 0x0A, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41        
		    };
		writer.write(testLF);
        writer.flush();
        writer.close();
		Artifact artifactLF = Artifact.recordArtifacts(Arrays.asList(path.toString()), null, null, null, true).iterator().next();
		writer = new FileOutputStream(path);
        writer.write(testCRLF);
        writer.flush();
        writer.close();
        Artifact artifactCRLF = Artifact.recordArtifacts(Arrays.asList(path.toString()), null, null, null, true).iterator().next();
		// test result
		assertEquals(artifactLF, artifactCRLF);
	}
	
	@Test
    @DisplayName("Test record with normalize line endings with CRLF over digest size")
    public void testRecordWithNormalizeLineEndingsAndCRLFOnDigestSize() throws IOException {
		String path = Files.createFile(temporaryFolder.resolve("file")).toString();
		FileOutputStream writer = new FileOutputStream(path);
		// reference with '\n'
		int digestSize = new SHA256Digest().getDigestSize();
		byte[] testLF = new byte[digestSize];
		for (int i=0;i<testLF.length;i++) {
			testLF[i] = 0x41;
		}
		testLF[digestSize-1] = 0x0A;
		byte[] testCRLF = new byte[digestSize+1];
		for (int i=0;i<testLF.length;i++) {
			testCRLF[i] = 0x41;
		}
		testCRLF[digestSize-1] = 0x0D;
		testCRLF[digestSize] = 0x0A;
		writer.write(testLF);
        writer.flush();
        writer.close();
		Artifact artifactLF = Artifact.recordArtifacts(Arrays.asList(path.toString()), null, null, null, true).iterator().next();
		writer = new FileOutputStream(path);
        writer.write(testCRLF);
        writer.flush();
        writer.close();
        Artifact artifactCRLF = Artifact.recordArtifacts(Arrays.asList(path.toString()), null, null, null, true).iterator().next();
		// test result
		assertEquals(artifactLF, artifactCRLF);
	}
	
	@Test
    @DisplayName("Test record with bad base path settings")
    public void testBadBasePathSetting() {
		String basePath = "/bad/base/path";
		assertThrows(ValueError.class, () -> {
			Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("foo"), null, basePath);
	    });
	}
	
	@Test
    @DisplayName("Test record Empty list passed. Return empty dict.")
    public void testEmptyArtifactsListRecordNothing() {
		Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList(), null, null);
		assertTrue(artifacts.size() == 0);
	}
	
	@Test
    @DisplayName("Test record List with not existing artifact passed. Return empty dict.")
    public void testNotExistingArtifactsInListRecordNothing() {
		Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("xxx"), null, null);
		assertTrue(artifacts.size() == 0);
	}
	
	@Test
    @DisplayName("Test record raverse dir and subdirs. Record three files.")
    public void testDotCheckFiles() {
		String aliceDir = testDir + "/owner_alice";
		// get materials
		LinkBuilder testLinkBuilder = new LinkBuilder("clone");
		testLinkBuilder.setBasePath(aliceDir);
		testLinkBuilder.addMaterial(Arrays.asList("."));
		Link fooLink = testLinkBuilder.build();
		assertTrue(fooLink.getMaterials().size() == 2);
	}
	

	


}
