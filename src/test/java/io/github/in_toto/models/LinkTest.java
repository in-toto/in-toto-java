package io.github.in_toto.models;

import io.github.in_toto.models.Artifact.ArtifactHash;
import io.github.in_toto.models.Link;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.keys.Key;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

/**
 * Link-specific tests
 */
 
@DisplayName("Link-specific tests")
@EnableRuleMigrationSupport
@TestInstance(Lifecycle.PER_CLASS)
class LinkTest
{
    private Link link = new Link(null, null, "test", null, null, null);
    private Key key = RSAKey.read("src/test/resources/somekey.pem");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        assertTrue(link.getByproducts() instanceof HashMap);
        assertTrue(link.getEnvironment() instanceof HashMap);
        assertTrue(link.getName() instanceof String);
        assertTrue(link.getProducts() instanceof HashMap);
        assertTrue(link.getMaterials() instanceof HashMap);
        assertTrue(link.getCommand() instanceof ArrayList);
    }

    @Test
    @DisplayName("Validate Link addMaterials")
    public void testValidMaterial() throws IOException
    {
        File file = temporaryFolder.newFile("alice");
        String path = file.getAbsolutePath();
        link.addMaterial(path);

        Map<String, ArtifactHash> material = link.getMaterials();
        Map.Entry<String, ArtifactHash> entry = material.entrySet().iterator().next();
        assertEquals(entry.getKey(), path);

        file.delete();
    }

    @Test
    @DisplayName("Validate Link addProducts")
    public void testValidProduct() throws IOException
    {
        File file = temporaryFolder.newFile("bob");
        String path = file.getAbsolutePath();

        link.addProduct(path);

        Map<String, ArtifactHash> product = link.getProducts();
        Map.Entry<String, ArtifactHash> entry = product.entrySet().iterator().next();
        assertEquals(entry.getKey(), path);
    
        file.delete();
    }

    @Test
    @DisplayName("Validate Link setByproducts")
    public void testValidByproduct()
    {
        HashMap<String, String> byproduct = new HashMap<>();
        byproduct.put("stdin","");
        link.setByproducts(byproduct);
        assertEquals(byproduct, link.getByproducts());
    }

    @Test
    @DisplayName("Validate Link setEnvironments")
    public void testValidEnvironment()
    {
        HashMap<String, String> environment = new HashMap<>();
        environment.put("variables", "<ENV>");
        link.setEnvironment(environment);
        assertEquals(environment, link.getEnvironment());
    }

    @Test
    @DisplayName("Validate Link setCommand")
    public void testValidCommand()
    {
        ArrayList<String> command = new ArrayList<String>();
        command.add("<COMMAND>");
        link.setCommand(command);
        assertEquals(command.get(0), link.getCommand().get(0));
    }

    @AfterAll
    @Test
    @DisplayName("Validate Link sign & dump")
    public void testLinkSignDump()
    {
        link.sign(key);
        link.dump("dump.link");
        File fl = new File("dump.link");
        assertTrue(fl.exists());
        fl.delete();
    }

    @Test
    @DisplayName("Validate link serialization and de-serialization")
    public void testLinkDeSerialization()
    {

        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        String jsonString = testLink.dumpString();
        Link newLink = Link.read(jsonString);

        assertTrue(newLink.getName() != null);
        assertEquals(testLink.getName(), newLink.getName());
    }

    @Test
    @DisplayName("Test Apply Exclude Patterns")
    public void testApplyExcludePatterns() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("foo");
        File file2 = temporaryFolder.newFile("bar");
        File file3 = temporaryFolder.newFile("baz");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**{foo,bar}";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path3);

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path3);

        file1.delete();
        file2.delete();
        file3.delete();
    }

    @Test
    @DisplayName("Test Apply Exclude Default Patterns")
    public void testApplyExcludeDefaultPatterns() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("foo.link");
        File file2 = temporaryFolder.newFile("bar");
        File file3 = temporaryFolder.newFolder(".git");
        File file4 = temporaryFolder.newFile(file3.getName() + "/baz");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path4 = file4.getAbsolutePath();

        testLink.addProduct(path1);
        testLink.addProduct(path2);
        testLink.addProduct(path4);

        testLink.addMaterial(path1);
        testLink.addMaterial(path2);
        testLink.addMaterial(path4);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path2);

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path2);

        file1.delete();
        file2.delete();
        file3.delete();
    }

    @Test
    @DisplayName("Test Apply Exclude All")
    public void testApplyExcludeAll() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("foo");
        File file2 = temporaryFolder.newFile("bar");
        File file3 = temporaryFolder.newFile("baz");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 0);
        assertFalse(product.entrySet().iterator().hasNext());

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(material.size(), 0);
        assertFalse(material.entrySet().iterator().hasNext());

        file1.delete();
        file2.delete();
        file3.delete();
    }

    @Test
    @DisplayName("Test Apply Exclude Multiple Star")
    public void testApplyExcludeMultipleStar() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("foo");
        File file2 = temporaryFolder.newFile("bar");
        File file3 = temporaryFolder.newFile("baz");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**a**";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path1);

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path1);

        file1.delete();
        file2.delete();
        file3.delete();
    }

    @Test
    @DisplayName("Test Apply Exclude Question Mark")
    public void testApplyExcludeQuestionMark() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("foo");
        File file2 = temporaryFolder.newFile("bazfoo");
        File file3 = temporaryFolder.newFile("barfoo");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**ba?foo";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path1);

        Map<String, ArtifactHash> material = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path1);

        file1.delete();
        file2.delete();
        file3.delete();
    }

    @Test
    @DisplayName("Test Apply Exclude Sequence")
    public void testApplyExcludeSeq() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("baxfoo");
        File file2 = temporaryFolder.newFile("bazfoo");
        File file3 = temporaryFolder.newFile("barfoo");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**ba[xz]foo";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path3);

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path3);

        file1.delete();
        file2.delete();
        file3.delete();
    }


    @Test
    @DisplayName("Test Apply Exclude Negate Sequence")
    public void testApplyExcludeNegSeq() throws IOException
    {
        Link testLink = new Link(null, null, "sometestname",
                null, null, null);

        File file1 = temporaryFolder.newFile("baxfoo");
        File file2 = temporaryFolder.newFile("bazfoo");
        File file3 = temporaryFolder.newFile("barfoo");

        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();
        String path3 = file3.getAbsolutePath();

        String pattern = "**ba[!r]foo";

        testLink.addProduct(path1, pattern);
        testLink.addProduct(path2, pattern);
        testLink.addProduct(path3, pattern);

        testLink.addMaterial(path1, pattern);
        testLink.addMaterial(path2, pattern);
        testLink.addMaterial(path3, pattern);

        Map<String, ArtifactHash> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry1 = product.entrySet().iterator().next();
        assertEquals(entry1.getKey(), path3);

        Map<String, ArtifactHash> material = testLink.getMaterials();
        assertEquals(product.size(), 1);

        Map.Entry<String, ArtifactHash> entry2 = material.entrySet().iterator().next();
        assertEquals(entry2.getKey(), path3);

        file1.delete();
        file2.delete();
        file3.delete();
    }

}
