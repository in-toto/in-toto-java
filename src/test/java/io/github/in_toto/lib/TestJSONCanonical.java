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
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestJSONCanonical {

    private Key key = RSAKey.read("src/test/resources/somekey.pem");
    private FileTransporter transporter = new FileTransporter();
    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @Test
    public void testCanonicalJSONEdgeCases () throws IOException, URISyntaxException {
        // from securesystemslib.formats import encode_canonical
        // from in_toto.models.metadata import Metablock
        // linkMb = Metablock.load("src/test/resources/testvalues.link")
        // encode_canonical(
        //      linkMb.signed.signable_dict).encode("UTF-8").hex()
        String referenceCanonicalLinkHex = "7b225f74797065223a226c696e6b222" +
                "c22627970726f6475637473223a7b7d2c22636f6d6d616e64223a5b5d2" +
                "c22656e7669726f6e6d656e74223a7b2261223a22575446222c2262223" +
                "a747275652c2263223a66616c73652c2264223a6e756c6c2c2265223a3" +
                "12c2266223a221befbfbf465c5c6e5c22227d2c226d6174657269616c7" +
                "3223a7b7d2c226e616d65223a2274657374222c2270726f64756374732" +
                "23a7b7d7d";

        // Load test link with special values (edge cases) in opaque
        // environment field
        Metablock<Link> metablock = transporter.load(new URI("src/test/resources/testvalues.link"), metablockType);

        // Assert that Java's canonical json representation of the link is
        // equal to reference implementation's canonical json representation
        assertEquals(Hex.toHexString(metablock.getCanonicalJSON(true).getBytes()),
                referenceCanonicalLinkHex);
    }
    
    @Test
    @DisplayName("Validate canonical")
    public void testCanonicalJSON() throws IOException
    {
    	String referenceCanonical = "{\"_type\":\"link\",\"byproducts\":{},\"command\":[],\"environment\":{},\"materials\":{\"src/test/resources/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},\"src/test/resources/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},\"src/test/resources/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}},\"name\":\"serialize\",\"products\":{\"src/test/resources/serialize/bar\":{\"sha256\":\"fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9\"},\"src/test/resources/serialize/baz\":{\"sha256\":\"baa5a0964d3320fbc0c6a922140453c8513ea24ab8fd0577034804a967248096\"},\"src/test/resources/serialize/foo\":{\"sha256\":\"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae\"}}}";
    	LinkBuilder testLinkBuilder = new LinkBuilder("serialize");

        String path1 = "src/test/resources/serialize/foo";
        String path2 = "src/test/resources/serialize/baz";
        String path3 = "src/test/resources/serialize/bar";
        
        testLinkBuilder.addProduct(path1);
        testLinkBuilder.addProduct(path2);
        testLinkBuilder.addProduct(path3);

        testLinkBuilder.addMaterial(path1);
        testLinkBuilder.addMaterial(path2);
        testLinkBuilder.addMaterial(path3);
    	
    	Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
        String linkString = JSONEncoder.canonicalize(gson.toJsonTree(testMetablockLink.getSigned()));
        assertEquals(linkString, referenceCanonical);
    }
}
