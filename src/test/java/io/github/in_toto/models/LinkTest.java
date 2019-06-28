package io.github.in_toto.models;

import io.github.in_toto.models.Link;
import io.github.in_toto.models.Link.LinkBuilder;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.keys.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
/**
 * Link-specific tests
 */
 
@DisplayName("Link-specific tests")
@TestInstance(Lifecycle.PER_CLASS)
class LinkTest
{
    private LinkBuilder linkBuilder =  new LinkBuilder("test");
	private Link link = linkBuilder.build();
    private Key key = RSAKey.read("src/test/resources/link_test/somekey.pem");
    private FileTransporter transporter = new FileTransporter();
    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @TempDir
    Path temporaryFolder;

    @Test
    @DisplayName("Test Link Constructor")
    public void testLinkContructorEqual()
    {
        // test a link object
    
        assertEquals("test",link.getName());
        assertNotEquals(null,link.getName());
    }

    @Test
    @DisplayName("Test Valid Link Object Types")
    public void testValidObject()
    {
        // test link data types

        assertTrue(link instanceof Link);
        assertTrue(link.getByproducts() instanceof ByProducts);
        assertTrue(link.getEnvironment() instanceof Map);
        assertTrue(link.getName() instanceof String);
        assertTrue(link.getProducts() instanceof Set);
        assertTrue(link.getMaterials() instanceof Set);
        assertTrue(link.getCommand() instanceof List);
    }

    @Test
    @DisplayName("Validate Link addMaterials")
    public void testValidMaterial() throws IOException, ValueError
    {

        String keyFile = Files.createFile(temporaryFolder.resolve("alice")).toString();
        
        Artifact pathArtifact = Artifact.recordArtifacts(Arrays.asList(keyFile), null, null).iterator().next();;
        
        linkBuilder.addMaterial(Arrays.asList(keyFile));
        
        Link link = linkBuilder.build();

        assertTrue(link.getMaterials().contains(pathArtifact));
    }

    @Test
    @DisplayName("Validate Link addProducts")
    public void testValidProduct() throws IOException, ValueError
    {
        String path = Files.createFile(temporaryFolder.resolve("bob")).toString();
        
        Artifact pathArtifact = Artifact.recordArtifacts(Arrays.asList(path), null, null).iterator().next();; 
        
        linkBuilder.addProduct(Arrays.asList(path));
        
        Link link = linkBuilder.build();
        
        Set<Artifact> product = link.getProducts();
        Artifact entry = product.iterator().next();
        assertEquals(entry, pathArtifact);
    }

    @Test
    @DisplayName("Validate Link setByproducts")
    public void testValidByproduct()
    {
        ByProducts byproduct = new ByProducts("", null, null);
        
        linkBuilder.setByproducts(byproduct);
        
        Link link = linkBuilder.build();
        assertEquals(byproduct, link.getByproducts());
    }

    @Test
    @DisplayName("Validate Link setEnvironments")
    public void testValidEnvironment()
    {
    	Map<String, String> environment = new HashMap<String, String>();
    	
    	environment.put("bar", "foo");
        
        linkBuilder.setEnvironment(environment);
        Link link = linkBuilder.build();
        assertEquals(environment, link.getEnvironment());
    }

    @Test
    @DisplayName("Validate Link setCommand")
    public void testValidCommand()
    {
        ArrayList<String> command = new ArrayList<String>();
        command.add("<COMMAND>");
        linkBuilder.setCommand(command);
        
        Link link = linkBuilder.build();
        assertEquals(command.get(0), link.getCommand().get(0));
    }

    @AfterAll
    @Test
    @DisplayName("Validate Link sign & dump")
    public void testLinkSignDump() throws URISyntaxException
    {
        Metablock<Link> metablock = new Metablock<Link>(link, null);
        metablock.sign(key);
    	URI uri = new URI("test.0b70eafb.link");
        transporter.dump(uri, metablock);
        File fl = new File("test.0b70eafb.link");
        assertTrue(fl.exists());
        fl.delete();
    }

    @Test
    @DisplayName("Validate link serialization and deserialization with artifacts from object")
    public void testLinkSerializationWithArtifactsFromObject() throws IOException, URISyntaxException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("serialize");
    	testLinkBuilder.setBasePath("src/test/resources/link_test/serialize");
        
        Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("foo", "baz", "bar"), null, "src/test/resources/link_test/serialize");
        
        Artifact pathArtifact1 = artifacts.iterator().next();
        Artifact pathArtifact2 = artifacts.iterator().next();
        Artifact pathArtifact3 = artifacts.iterator().next();        

        testLinkBuilder.addProduct(Arrays.asList(""));
        testLinkBuilder.addMaterial(Arrays.asList(""));
        
        Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        
        URI linkFile = new URI(Files.createFile(temporaryFolder.resolve("linkFile")).toString());
        
        transporter.dump(linkFile, testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);
        
        newLinkMetablock.sign(key);
        
        assertEquals(newLinkMetablock.signatures, testMetablockLink.signatures);

        assertEquals(newLinkMetablock.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact1));
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact2));
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact3));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact1));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact2));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact3));
        
        Metablock<Link> metablockFromFile = transporter.load(new URI("src/test/resources/link_test/serialize/serialize.link"), metablockType);
        metablockFromFile.sign(key);
        
        assertEquals(metablockFromFile.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact1));
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact2));
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact3));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact1));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact2));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact3));        
        assertEquals(metablockFromFile.signatures, testMetablockLink.signatures);
    }

    
    @Test
    @DisplayName("Validate link serialization and de-serialization with artifacts from file")
    public void testLinkDeSerializationWithArtifactsFromFile() throws URISyntaxException, IOException
    {
    	Artifact testproduct = new Artifact("demo-project/foo.py", "ebebf8778035e0e842a4f1aeb92a601be8ea8e621195f3b972316c60c9e12235");

        Metablock<Link> testMetablockLink = transporter.load(new URI("src/test/resources/link_test/clone.776a00e2.link"), metablockType);
        assertTrue(testMetablockLink.signed.getName() != null);
        assertEquals(testMetablockLink.signed.getName(), "clone");
        assertTrue(testMetablockLink.signed.getProducts().contains(testproduct));

        URI linkFile = new URI(Files.createFile(temporaryFolder.resolve("cloneFile")).toString());
        
        transporter.dump(linkFile, testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);

        assertEquals(newLinkMetablock.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(newLinkMetablock.signed.getProducts().contains(testproduct));
    }
    
    @Test
    @DisplayName("Validate link deserialization and serialization with byproducts")
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
    	Metablock<Link> testMetablockLink = transporter.load(new URI("src/test/resources/link_test/byproducts.link"), metablockType);
    	
    	String linkString = testMetablockLink.getSigned().JSONEncodeCanonical();
        assertEquals(referenceCanonical, linkString);
    	
    	assertEquals(referenceCanonicalLinkHex, Hex.toHexString(testMetablockLink.getCanonicalJSON().getBytes()));
    }


    @Test
    @DisplayName("Test Apply Exclude Patterns")
    public void testApplyExcludePatterns() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();;

        String pattern = "**{foo,bar}";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }

    @Test
    @DisplayName("Test Apply Exclude Default Patterns")
    public void testApplyExcludeDefaultPatterns() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo.link")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        Path path3 = Files.createDirectory(temporaryFolder.resolve(".git"));
        
        String path4 = Files.createFile(Paths.get(path3.toString(),"baz")).toString();        
        Artifact pathArtifact2 = Artifact.recordArtifacts(Arrays.asList(path2), null, null).iterator().next();

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path4));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path4));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact2));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact2));
    }

    @Test
    @DisplayName("Test Apply Exclude All")
    public void testApplyExcludeAll() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();

        String pattern = "**";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 0);
        assertFalse(product.iterator().hasNext());

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 0);
        assertFalse(material.iterator().hasNext());
    }

    @Test
    @DisplayName("Test Apply Exclude Multiple Star")
    public void testApplyExcludeMultipleStar() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();
        
        Artifact pathArtifact1 = Artifact.recordArtifacts(Arrays.asList(path1), null, null).iterator().next();

        String pattern = "**a**";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact1));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact1));
    }

    @Test
    @DisplayName("Test Apply Exclude Question Mark")
    public void testApplyExcludeQuestionMark() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        
        Artifact pathArtifact1 = Artifact.recordArtifacts(Arrays.asList(path1), null, null).iterator().next();

        String pattern = "**ba?foo";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);
        
        assertTrue(product.contains(pathArtifact1));

        Set<Artifact> material = testLink.getProducts();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact1));
    }

    @Test
    @DisplayName("Test Apply Exclude Sequence")
    public void testApplyExcludeSeq() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("baxfoo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();

        String pattern = "**ba[xz]foo";

        testLinkBuilder.setExcludePatterns(pattern);
        
        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }


    @Test
    @DisplayName("Test Apply Exclude Negate Sequence")
    public void testApplyExcludeNegSeq() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("baxfoo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();

        String pattern = "**ba[!r]foo";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }

}
