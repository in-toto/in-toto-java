package io.github.in_toto.models.layout;

import java.util.List;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.exceptions.FormatError;
import io.github.in_toto.models.layout.rule.Rule;

public abstract class SupplyChainItem {
    private String name;
    @SerializedName("_type")
    private SupplyChainItemType type;
    
    @SerializedName("expected_materials")
    //@JsonAdapter(ListRuleJsonAdapter.class)
    private List<Rule> expectedMaterials;
    @SerializedName("expected_products")
    //@JsonAdapter(ListRuleJsonAdapter.class)
    private List<Rule> expectedProducts;
    
    public SupplyChainItem(String name) {
        this.name = name;
        this.type = this.getType();
    }
    
    public SupplyChainItem(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts) {
        this.name = name;
        this.expectedMaterials = expectedMaterials;
        this.expectedProducts = expectedProducts;
        this.type = this.getType();
    }
    
    /**
     * Convenience method to parse the passed rule string into a list and append it
     * to the item's list of expected_materials.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     */
    public void addMaterialRuleFromString(String ruleString) {
        this.expectedMaterials.addAll(Rule.getRulesFromString(ruleString));
    }
    
    /**
     * Convenience method to parse the passed rule string into a list and append it
     * to the item's list of expected_materials.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     * @throws FormatError If the passed rule_string is not a string. If the parsed
     *                     rule_string cannot be unpacked using rulelib.
     */
    public void addProductRuleFromString(String ruleString) throws FormatError {
        this.expectedProducts.addAll(Rule.getRulesFromString(ruleString));
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Rule> getExpectedMaterials() {
        return expectedMaterials;
    }

    public void setExpectedMaterials(List<Rule> expectedMaterials) {
        this.expectedMaterials = expectedMaterials;
    }

    public List<Rule> getExpectedProducts() {
        return expectedProducts;
    }

    public void setExpectedProducts(List<Rule> expectedProducts) {
        this.expectedProducts = expectedProducts;
    }

    public void setType(SupplyChainItemType type) {
        this.type = type;
    }

    public abstract SupplyChainItemType getType();
    
    enum SupplyChainItemType {
        inspection, step
    }
}
