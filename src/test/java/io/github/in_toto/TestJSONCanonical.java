package io.github.in_toto;

import io.github.in_toto.models.Link;
import java.io.*;
import java.nio.file.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestJSONCanonical {
    @Test
    public void testCanonicalJSONEdgeCases () throws IOException {
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
        String linkString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/testvalues.link")));
        Link link = Link.read(linkString);

        // Assert that Java's canonical json representation of the link is
        // equal to reference implementation's canonical json representation
        assertEquals(Hex.toHexString(link.getCanonicalJSON(true).getBytes()),
                referenceCanonicalLinkHex);
    }
}
