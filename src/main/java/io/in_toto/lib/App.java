package io.in_toto.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import io.in_toto.keys.RSAKey;
import io.in_toto.keys.Key;
import io.in_toto.models.Link;
import io.in_toto.models.Artifact;
import io.in_toto.models.Artifact.ArtifactHash;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
        System.out.println("Loaded key: " + thiskey.computeKeyId());

        Link link = new Link(null, null, "test", null, null, null);
        link.addArtifact("alice");
        System.out.println("dumping file...");
        link.sign(thiskey);
        link.dump("somelink.link");

    }

}
