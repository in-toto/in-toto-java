package io.in_toto.lib;

import com.google.gson.Gson;

import java.util.ArrayList;


import io.in_toto.keys.RSAKey;
import io.in_toto.models.Link;
import io.in_toto.models.Metablock;
import io.in_toto.models.Artifact;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        RSAKey thiskey = RSAKey.readPem("somekey.pem");
        System.out.println("Loaded key: " + thiskey.computeKeyId());

        Artifact a = new Artifact("somekey.pem");
        ArrayList<Artifact> materials = new ArrayList<Artifact>();
        materials.add(a);
        Link link = new Link(materials, null, "test", null, "do the thing");
        Metablock mb = new Metablock(link, null);
        Gson gson = new Gson();
        System.out.println("computing json: " + gson.toJson(mb));


    }
}
