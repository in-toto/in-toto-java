package io.github.in_toto.models.link;

import io.github.in_toto.transporters.FileTransporter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.Artifact;
import io.github.in_toto.models.link.ByProducts;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;
import io.github.in_toto.exceptions.KeyException;
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

import static org.junit.jupiter.api.Assertions.*;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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

    
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Link.class)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

}
