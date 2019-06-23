package io.github.in_toto.lib;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.FileTransporter;
import io.github.in_toto.models.Link;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Link.LinkBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
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
    private FileTransporter transporter = new FileTransporter();
    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @Test
    public void testCanonicalJSONEdgeCases () throws IOException, URISyntaxException {
        // from securesystemslib.formats import encode_canonical
        // from in_toto.models.metadata import Metablock
        // linkMb = Metablock.load("src/test/resources/test_json_canonical/testvalues.link")
        // encode_canonical(
        //      linkMb.signed.signable_dict).encode("UTF-8").hex()
        String referenceCanonicalLinkHex = "7b225f74797065223a226c696e6b222c22627970726f6475637473223a7b227265"
        		+ "7475726e2d76616c7565223a6e756c6c2c22737464657272223a6e756c6c2c227374646f7574223a6e756c6c7d2"
        		+ "c22636f6d6d616e64223a5b5d2c22656e7669726f6e6d656e74223a7b22776f726b646972223a22666f6f2f6261"
        		+ "72227d2c226d6174657269616c73223a7b7d2c226e616d65223a2274657374222c2270726f6475637473223a7b7"
        		+ "d7d";
        
        String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{\"workdir\":\"foo/bar\"},\"materials\":{},\"name\":\"test\",\"products\":{}}";

        // Load test link with special values (edge cases) in opaque
        // environment field
        Metablock<Link> metablock = transporter.load(new URI("src/test/resources/test_json_canonical/testvalues.link"), metablockType);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String linkString = JSONEncoder.canonicalize(gson.toJsonTree(metablock.getSigned()));
        assertEquals(referenceCanonical, linkString);
        // Assert that Java's canonical json representation of the link is
        // equal to reference implementation's canonical json representation
        assertEquals(referenceCanonicalLinkHex, Hex.toHexString(metablock.getCanonicalJSON(true).getBytes()));
    }
    
    @Test
    @DisplayName("Validate canonical")
    public void testCanonicalJSON() throws IOException
    {
    	String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{},\"materials\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}},\"name\":\"serialize\",\"products\":{\"src/test/resources/test_json_canonical/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},\"src/test/resources/test_json_canonical/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},\"src/test/resources/test_json_canonical/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}}}";
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
