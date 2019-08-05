package io.github.in_toto.lib;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;
import io.github.in_toto.transporters.FileTransporter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestJSONCanonical {

    private Key key = RSAKey.read("src/test/resources/test_json_canonical/somekey.pem");
    private FileTransporter<Link> transporter = new FileTransporter<Link>();
	private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @Test
    public void testCanonicalJSONEdgeCases () throws IOException, URISyntaxException {
        // from securesystemslib.formats import encode_canonical
        // from in_toto.models.metadata import Metablock
        // linkMb = Metablock.load("src/test/resources/test_json_canonical/testvalues.link")
        // encode_canonical(
        //      linkMb.signed.signable_dict).encode("UTF-8").hex()
        String referenceCanonicalLinkHex = "7b225f74797065223a226c696e6b222c22627970726f6475637473223a7b7d2c22636f"
        		+ "6d6d616e64223a5b5d2c22656e7669726f6e6d656e74223a7b22776f726b646972223a22666f6f227d2c226d6174657"
        		+ "269616c73223a7b7d2c226e616d65223a2274657374222c2270726f6475637473223a7b7d7d";
        
        String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{\"workdir\":\"foo\"},\"materials\":{},\"name\":\"test\",\"products\":{}}";

        // Load test link with special values (edge cases) in opaque
        // environment field
        Metablock<Link> metablock = transporter.load("src/test/resources/test_json_canonical/testvalues.link", metablockType);

        String linkString = metablock.getSigned().jsonEncodeCanonical();
        assertEquals(referenceCanonical, linkString);
        // Assert that Java's canonical json representation of the link is
        // equal to reference implementation's canonical json representation
        assertEquals(referenceCanonicalLinkHex, Hex.toHexString(linkString.getBytes()));
    }
    
    @Test
    @DisplayName("Validate canonical link metablock")
    public void testCanonicalJSONLinkMetablock() throws IOException
    {
    	String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{},"
    			+ "\"materials\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}},"
    			+ "\"name\":\"serialize\",\"products\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea"
    			+ "04fae5511b68fbf8fb9\"},\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}}}";
    	LinkBuilder testLinkBuilder = new LinkBuilder("serialize");

        String path1 = "src/test/resources/test_json_canonical/serialize";
        
        testLinkBuilder.addProduct(Arrays.asList(path1));

        testLinkBuilder.addMaterial(Arrays.asList(path1));
    	
    	Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String linkString = JSONEncoder.canonicalize(gson.toJsonTree(testMetablockLink.getSigned()));
        assertEquals(referenceCanonical, linkString);
    }
    
    @Test
    @DisplayName("Validate canonical")
    public void testEncodeCanonical() throws IOException
    {

        /*
         * self.assertEqual('""', encode(""))
        self.assertEqual('[1,2,3]', encode([1, 2, 3]))
        self.assertEqual('[1,2,3]', encode([1,2,3]))
        self.assertEqual('[]', encode([]))
        self.assertEqual('{}', encode({}))
        self.assertEqual('{"A":[99]}', encode({"A": [99]}))
        self.assertEqual('{"A":true}', encode({"A": True}))
        self.assertEqual('{"B":false}', encode({"B": False}))
        self.assertEqual('{"x":3,"y":2}', encode({"x": 3, "y": 2}))

        self.assertEqual('{"x":3,"y":null}', encode({"x": 3, "y": None}))
         */
    	String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{},"
    			+ "\"materials\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}},"
    			+ "\"name\":\"serialize\",\"products\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea"
    			+ "04fae5511b68fbf8fb9\"},\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},"
    			+ "\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}}}";
    	LinkBuilder testLinkBuilder = new LinkBuilder("serialize");

        String path1 = "src/test/resources/test_json_canonical/serialize";
        
        testLinkBuilder.addProduct(Arrays.asList(path1));

        testLinkBuilder.addMaterial(Arrays.asList(path1));
    	
    	Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String linkString = JSONEncoder.canonicalize(gson.toJsonTree(testMetablockLink.getSigned()));
        assertEquals(referenceCanonical, linkString);
    }
}
