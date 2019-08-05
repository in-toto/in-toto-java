package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.reflect.TypeToken;

import io.github.in_toto.exceptions.KeyException;
import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.layout.Layout;
import io.github.in_toto.models.link.Artifact;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;
import io.github.in_toto.transporters.FileTransporter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class MetablockTest {
    private LinkBuilder linkBuilder =  new LinkBuilder("test");
	private Link link = linkBuilder.build();
    private Key key1 = RSAKey.read("src/test/resources/io/github/in_toto/model/metablock_test/somekey.pem");
    private Key key2 = RSAKey.read("src/test/resources/io/github/in_toto/model/metablock_test/someotherkey.pem");

    private Key key = key1;
    

    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @TempDir
    Path temporaryFolder;

	@Test
	void testSignatures() {
		Metablock<Link> metablock = new Metablock<Link>(link, null);
        metablock.sign(key1);
        String shortKeyId = key1.getShortKeyId();
        assertEquals("0b70eafb", shortKeyId);
        metablock.sign(key2);
        RuntimeException exc = assertThrows(RuntimeException.class, () -> {
            metablock.getFullName();
        });
        assertEquals("Signature id is ambiguous because there is more than 1 signer available", exc.getMessage());
	}
    
    @Test
    @DisplayName("Validate link metablock serialization and de-serialization with artifacts from file")
    public void testLinkDeSerializationWithArtifactsFromFile() throws URISyntaxException, IOException
    {
        Artifact testproduct = new Artifact("demo-project/foo.py", "ebebf8778035e0e842a4f1aeb92a601be8ea8e621195f3b972316c60c9e12235");


        FileTransporter<Link> transporter = new FileTransporter<>();

        Metablock<Link> testMetablockLink = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/clone.776a00e2.link", metablockType);
        assertTrue(testMetablockLink.getSigned().getName() != null);
        assertEquals(testMetablockLink.getSigned().getName(), "clone");
        assertTrue(testMetablockLink.getSigned().getProducts().contains(testproduct));
        
        String linkFile = Files.createFile(temporaryFolder.resolve(testMetablockLink.getFullName())).toString();

        transporter = new FileTransporter<>(temporaryFolder.toString());
        
        transporter.dump(testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);

        assertEquals(newLinkMetablock.getSigned().getName(), testMetablockLink.getSigned().getName());
        assertTrue(newLinkMetablock.getSigned().getProducts().contains(testproduct));
    }
    
    @Test
    @DisplayName("Validate link metablock deserialization and serialization with byproducts")
    public void testLinkDeserializationSerializationWithByProducts() throws IOException, URISyntaxException, ValueError
    {
        String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{\"return-value\":0,\"stderr\":\"\",\"stdout\":\"demo-project/\n" + 
                "demo-project/foo.py\n" + 
                "\"},\"command\":[\"tar\",\"--exclude\",\".git\",\"-zcvf\",\"demo-project.tar.gz\",\"demo-project\"],\"environment\":{},"
                + "\"materials\":{\"demo-project/foo.py\":{\"sha256\":\"c2c0ea54fa94fac3a4e1575d6ed3bbd1b01a6d0b8deb39196bdc31c457ef731b\"}},"
                + "\"name\":\"package\",\"products\":{\"demo-project.tar.gz\":{\"sha256\":\"17ffccbc3bb4822a63bef7433a9bea79d726d4e02606242b9b97e317fd89c462\"}}}";
        String referenceCanonicalLinkHex = "7b225f74797065223a226c696e6b222c22627970726f6475637473223a7b2272657475726e2d76616c7565223a302c22737464657272223a22222c"
                + "227374646f7574223a2264656d6f2d70726f6a6563742f0a64656d6f2d70726f6a6563742f666f6f2e70790a227d2c22636f6d6d616e64223a5b22746172222c222d2d6578636c7"
                + "56465222c222e676974222c222d7a637666222c2264656d6f2d70726f6a6563742e7461722e677a222c2264656d6f2d70726f6a656374225d2c22656e7669726f6e6d656e74223a"
                + "7b7d2c226d6174657269616c73223a7b2264656d6f2d70726f6a6563742f666f6f2e7079223a7b22736861323536223a22633263306561353466613934666163336134653135373"
                + "56436656433626264316230316136643062386465623339313936626463333163343537656637333162227d7d2c226e616d65223a227061636b616765222c2270726f64756374732"
                + "23a7b2264656d6f2d70726f6a6563742e7461722e677a223a7b22736861323536223a223137666663636263336262343832326136336265663734333361396265613739643732366"
                + "4346530323630363234326239623937653331376664383963343632227d7d7d";
        


        FileTransporter<Link> transporter = new FileTransporter<>();
        Metablock<Link> testMetablockLink = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/byproducts.link", metablockType);
        
        String linkString = testMetablockLink.getSigned().jsonEncodeCanonical();
        assertEquals(referenceCanonical, linkString);
        
        assertEquals(referenceCanonicalLinkHex, Hex.toHexString(testMetablockLink.getSigned().jsonEncodeCanonical().getBytes()));
    }
    
    String canonicalizeString(String src) {
        String pattern = "([\\\\\"])";
        return String.format("\"%s\"", src.replaceAll(pattern, "\\\\$1"));
    }
    
    @Test
    @DisplayName("Validate layout metablock deserialization and serialization with byproducts")
    public void testLayoutDeserializationSerializationWithByProducts() throws IOException, URISyntaxException, ValueError, JSONException
    {
        String jsonString = new String ( Files.readAllBytes( Paths.get("src/test/resources/io/github/in_toto/model/metablock_test/root.layout") ) );
        Type layoutMetablockType = new TypeToken<Metablock<Layout>>() {}.getType();
        
        FileTransporter<Layout> transporter = new FileTransporter<>();
        Metablock<Layout> testLayoutMetablock = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/root.layout", layoutMetablockType);
        
        String layoutMetablockString = testLayoutMetablock.jsonEncodeCanonical();
        JSONAssert.assertEquals(jsonString, layoutMetablockString, false);
    }
    
    @Test
    @DisplayName("Test unsigned Metablock.")
    public void testUnsigned() {
        Metablock<Link> testMetablockLink = new Metablock<Link>(link, null);
        assertEquals(testMetablockLink.getSignatures().size(), 1);
        assertEquals(testMetablockLink.getSignatures().iterator().next().getKey().getKeyid(), "UNSIGNED");
        assertEquals(testMetablockLink.getShortSignatureId(), "UNSIGNED");
    }
    
    @Test
    @DisplayName("Test not signable with Key.")
    public void testNotSignable() {
        Metablock<Link> testMetablockLink = new Metablock<Link>(link, null);
        Throwable exception = assertThrows(KeyException.class, () -> {
            testMetablockLink.sign(new Key("foo"));
          });
        
        assertEquals("Can't sign with a null or public key!", exception.getMessage());
    }
    
    @Test
    @DisplayName("Validate Link sign & dump")
    public void testLinkSignDump() throws URISyntaxException, IOException
    {
        Metablock<Link> metablock = new Metablock<Link>(link, null);
        metablock.sign(key);
        String linkFile = Files.createFile(temporaryFolder.resolve(metablock.getFullName())).toString();
        FileTransporter<Link> transporter = new FileTransporter<>(temporaryFolder.toString());
        transporter.dump(metablock);
        assertTrue(Files.exists(Paths.get(linkFile)));
    }
    
    @Test
    @DisplayName("sign with wrong key")
    public void testLinkSignExc() throws URISyntaxException, IOException
    {
        Metablock<Link> metablock = new Metablock<Link>(link, null);
        Throwable exception = assertThrows(KeyException.class, () -> {
            metablock.sign(null);
          });
        
        assertEquals("Can't sign with a null or public key!", exception.getMessage());
        
    }

    @Test
    @DisplayName("Validate link serialization and deserialization with artifacts from object")
    public void testLinkSerializationWithArtifactsFromObject() throws IOException, URISyntaxException, ValueError
    {
        LinkBuilder testLinkBuilder = new LinkBuilder("serialize");
        testLinkBuilder.setBasePath("src/test/resources/io/github/in_toto/model/metablock_test/serialize");
        
        Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("foo", "baz", "bar"), null, "src/test/resources/io/github/in_toto/model/metablock_test/serialize");
        
        Artifact pathArtifact1 = artifacts.iterator().next();
        Artifact pathArtifact2 = artifacts.iterator().next();
        Artifact pathArtifact3 = artifacts.iterator().next();        

        testLinkBuilder.addProduct(Arrays.asList(""));
        testLinkBuilder.addMaterial(Arrays.asList(""));
        
        Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        
        String linkFile = Files.createFile(temporaryFolder.resolve(testMetablockLink.getFullName())).toString();

        FileTransporter<Link> transporter = new FileTransporter<>(temporaryFolder.toString());
        
        transporter.dump(testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);
        
        newLinkMetablock.sign(key);
        
        assertEquals(newLinkMetablock.getSignatures(), testMetablockLink.getSignatures());

        assertEquals(newLinkMetablock.getSigned().getName(), testMetablockLink.getSigned().getName());
        assertTrue(newLinkMetablock.getSigned().getProducts().contains(pathArtifact1));
        assertTrue(newLinkMetablock.getSigned().getProducts().contains(pathArtifact2));
        assertTrue(newLinkMetablock.getSigned().getProducts().contains(pathArtifact3));
        assertTrue(newLinkMetablock.getSigned().getMaterials().contains(pathArtifact1));
        assertTrue(newLinkMetablock.getSigned().getMaterials().contains(pathArtifact2));
        assertTrue(newLinkMetablock.getSigned().getMaterials().contains(pathArtifact3));
        
        Metablock<Link> metablockFromFile = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/serialize/serialize.link", metablockType);
        metablockFromFile.sign(key);
        
        assertEquals(metablockFromFile.getSigned().getName(), testMetablockLink.getSigned().getName());
        assertTrue(metablockFromFile.getSigned().getProducts().contains(pathArtifact1));
        assertTrue(metablockFromFile.getSigned().getProducts().contains(pathArtifact2));
        assertTrue(metablockFromFile.getSigned().getProducts().contains(pathArtifact3));
        assertTrue(metablockFromFile.getSigned().getMaterials().contains(pathArtifact1));
        assertTrue(metablockFromFile.getSigned().getMaterials().contains(pathArtifact2));
        assertTrue(metablockFromFile.getSigned().getMaterials().contains(pathArtifact3));        
        assertEquals(metablockFromFile.getSignatures(), testMetablockLink.getSignatures());
    }
    
    @Test
    @DisplayName("Test Link equals and hashcode.")
    public void testEqualsAndHashCode() {

        Metablock<Link> testMetablockLink = new Metablock<Link>(link, null);
        testMetablockLink.sign(key1);
        Metablock<Link> testMetablockLink2 = new Metablock<Link>(link, null);
        testMetablockLink2.sign(key1);
        assertEquals(testMetablockLink, testMetablockLink2);
        assertEquals(testMetablockLink.hashCode(), testMetablockLink2.hashCode());
        testMetablockLink.sign(key1);
        assertEquals(testMetablockLink.getSignatures().size(), 1);
        assertEquals(testMetablockLink, testMetablockLink2);
        
        FileTransporter<Link> transporter = new FileTransporter<>();
        testMetablockLink = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/byproducts.link", metablockType);
        testMetablockLink2 = transporter.load("src/test/resources/io/github/in_toto/model/metablock_test/byproducts.link", metablockType);
        assertEquals(testMetablockLink.getSigned(), testMetablockLink2.getSigned());
        assertEquals(testMetablockLink.getSigned().hashCode(), testMetablockLink2.getSigned().hashCode());
    }   
    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Metablock.class)
            .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
