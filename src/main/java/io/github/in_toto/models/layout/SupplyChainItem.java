package io.github.in_toto.models.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.layout.rule.MatchRule;
import io.github.in_toto.models.layout.rule.Rule;
import io.github.in_toto.models.link.Artifact;
import io.github.in_toto.models.link.Link;

public abstract class SupplyChainItem {
    private final String name;
    @SerializedName("_type")
    private final SupplyChainItemType type;
    
    @SerializedName("expected_materials")
    //@JsonAdapter(ListRuleJsonAdapter.class)
    private final List<Rule> expectedMaterials;
    @SerializedName("expected_products")
    //@JsonAdapter(ListRuleJsonAdapter.class)
    private final List<Rule> expectedProducts;
    
    public SupplyChainItem(String name, SupplyChainItemType type) {
        this(name, type, null, null);
    }
    
    public SupplyChainItem(String name, SupplyChainItemType type, List<Rule> expectedMaterials, List<Rule> expectedProducts) {
        this.name = name;
        this.type = type;
        if (expectedMaterials != null) {
            this.expectedMaterials = Collections.unmodifiableList(new ArrayList<>(expectedMaterials));
        } else {
            this.expectedMaterials = Collections.unmodifiableList(new ArrayList<>()); 
        }
        if (expectedProducts != null) {
            this.expectedProducts = Collections.unmodifiableList(new ArrayList<>(expectedProducts));
        } else {
            this.expectedProducts = Collections.unmodifiableList(new ArrayList<>()); 
        }
    }
    
    public String getName() {
        return name;
    }

    public List<Rule> getExpectedMaterials() {
        return expectedMaterials;
    }

    public List<Rule> getExpectedProducts() {
        return expectedProducts;
    }

    public SupplyChainItemType getType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return "SupplyChainItem [name=" + name + ", type=" + type + ", expectedMaterials=" + expectedMaterials
                + ", expectedProducts=" + expectedProducts + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expectedMaterials == null) ? 0 : expectedMaterials.hashCode());
        result = prime * result + ((expectedProducts == null) ? 0 : expectedProducts.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        SupplyChainItem other = (SupplyChainItem) obj;
        if (expectedMaterials == null) {
            if (other.expectedMaterials != null) {
                return false;
            }
        } else if (!expectedMaterials.equals(other.expectedMaterials)) {
            return false;
        }
        if (expectedProducts == null) {
            if (other.expectedProducts != null) {
                return false;
            }
        } else if (!expectedProducts.equals(other.expectedProducts)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return type == other.type;
    }

    /**
     * """
  <Purpose>
    Apply all passed material or product rules (see source_type) of a given
    step or inspection (see source_name), to enforce and authorize the
    corresponding artifacts and to guarantee that artifacts are linked together
    across steps of the supply chain.

    The mode of operation is similar to that of a firewall:
    In the beginning all materials or products of the step or inspection are
    placed into an artifact queue. The rules are then applied sequentially,
    consuming artifacts in the queue, i.e. removing them from the queue upon
    successful application.

    The consumption of artifacts by itself has no effects on the verification.
    Only through a subsequent "DISALLOW" rule, that finds unconsumed artifacts,
    is an exception raised. Similarly does the "REQUIRE" rule raise exception,
    if it does not find the artifact it requires, because it has falsely been
    consumed or was not there from the beginning.



  <Arguments>
    source_name:
            The name of the item (step or inspection) being verified.

    source_type:
            One of "materials" or "products" depending on whether the rules are
            taken from the "expected_materials" or "expected_products" field of
            the item being verified.

    rules:
            The list of rules (material or product rules) for the item being
            verified.

    links:
            A dictionary containing link metadata per step or inspection, e.g.:
            {
              <link name> : <Metablock containing a link>,
              ...
            }

  <Exceptions>
    FormatError
        if source_type is not "materials" or "products", or
        if a rule in the passed list of rules does not conform with any rule
        format.

    RuleVerificationError
        if a DISALLOW rule matches disallowed artifacts, or
        if a REQUIRE rule does not find a required artifact.

  <Side Effects>
    Clears and populates the global RULE_TRACE data structure.

  """
     * @throws RuleVerificationError 
     */
    public void verifyRules(Map<String, List<Metablock<Link>>> linkMap) throws RuleVerificationError {
        if (linkMap == null || !linkMap.containsKey(this.getName())) {
            return;
        }
        for (Metablock<Link> link: linkMap.get(this.name)) {
            Set<Artifact> artifacts = link.getSigned().getMaterials();
            this.verifyRules(this.getExpectedMaterials(), artifacts, linkMap);
            artifacts = link.getSigned().getProducts();
            this.verifyRules(this.getExpectedProducts(), artifacts, linkMap);
        }
    }
    
    private void verifyRules(List<Rule> rules, Set<Artifact> artifacts, Map<String, List<Metablock<Link>>> linkMap) throws RuleVerificationError {
        Set<Artifact> notConsumed = new HashSet<>(artifacts);
        for (Rule rule: rules) {
            if (rule instanceof MatchRule) {
                MatchRule matchRule = (MatchRule) rule;
                Link destinationLink = linkMap.get(matchRule.getDestinationStep().getName()).get(0).getSigned();
                matchRule.verify(notConsumed, destinationLink.getMaterials(), destinationLink.getProducts());
            }
            Link sourceLink = linkMap.get(this.getName()).get(0).getSigned();
            Set<Artifact> consumed = rule.verify(notConsumed, sourceLink.getMaterials(), sourceLink.getProducts());
            notConsumed.removeAll(consumed);
        }
    }
    
    @SuppressWarnings("squid:S00115")
    enum SupplyChainItemType {
        inspection, step
    }
}
