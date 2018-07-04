package io.in_toto.lib;

import io.in_toto.lib.Artifact;
import java.util.ArrayList;

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

    private ArrayList<Artifact> materials;
    private ArrayList<Artifact> products;
    private String name;
    private String env;
    private String run;

    public Link(ArrayList<Artifact> materials, ArrayList<Artifact> products, String name, String env,
            String run) {
     
        super();

        if (materials == null)
            materials = new ArrayList<Artifact>();

        if (products == null)
            products = new ArrayList<Artifact>();

        //FIXME: probably warn about this would be a good idea
        if (name == null)
           name = "step";

        if (env == null)
            env = "";

        if (run == null)
            run = "";

        this.materials = materials;
        this.products = products;
        this.name = name;
        this.env = env;
        this.run = run;

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


