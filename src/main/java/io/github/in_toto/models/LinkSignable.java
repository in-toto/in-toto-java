/*
 * package-private class representing the signable payload of the in-toto link
 * metadata.
 */
package io.github.in_toto.models;

import io.github.in_toto.models.Artifact;
import io.github.in_toto.models.Artifact.ArtifactHash;
import io.github.in_toto.keys.Signature;
import io.github.in_toto.models.LinkSignable;
import java.util.ArrayList;
import java.util.HashMap;


class LinkSignable
    extends Signable {

    HashMap<String, ArtifactHash> materials;
    HashMap<String, ArtifactHash> products;
    HashMap<String, String>  byproducts;
    HashMap<String, String>  environment;
    ArrayList<String> command;
    String name;

    LinkSignable(HashMap<String, ArtifactHash> materials,
            HashMap<String, ArtifactHash> products, String name,
            HashMap<String, String> environment, ArrayList<String> command,
            HashMap<String, String> byproducts) {

        super();

        if (materials == null)
            materials = new HashMap<String, ArtifactHash>();

        if (products == null)
            products = new HashMap<String, ArtifactHash>();

        //FIXME: probably warn about this would be a good idea
        if (name == null)
           name = "step";

        if (environment == null)
            environment = new HashMap<String, String>();

        if (command == null)
            command = new ArrayList<String>();

        if (byproducts == null)
            byproducts = new HashMap<String, String>();

        this.materials = materials;
        this.products = products;
        this.name = name;
        this.environment = environment;
        this.command = command;
        this.byproducts = byproducts;
    }

    @Override
    public String getType() {
        return "link";
    }
}


