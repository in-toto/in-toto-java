package io.in_toto.models;

import io.in_toto.models.Artifact;
import io.in_toto.models.Artifact.ArtifactHash;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of the in-toto Link metadata type.
 *
 *
 * @param materials a list of material artifact objects
 * @param products a list of product artifact objects
 * @param name the name of the link used for lookup
 * @param env an abstract dictionary object containing any other useful
 * information
 * @param run the command executed
 *
 */
// FIXME: public or protected? do check
public class Link extends Signable
{
    private HashMap<String, ArtifactHash> materials;
    private HashMap<String, ArtifactHash> products;
    private HashMap<String, String>  byproducts;
    private HashMap<String, String>  environment;
    private ArrayList command;
    private String name;

    public Link(HashMap<String, ArtifactHash> materials,
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

    /**
     * Serialize the current metadata into a JSON file
     *
     * This abstract method is to be populated by the subclasses in order to verify them
     */
    // FIXME: write a nice java string template...
    public String encode_canonical() {
        return "";
    }

    @Override
    public String getType() {
        return "link";
    }
}


