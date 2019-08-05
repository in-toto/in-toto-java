package io.github.in_toto.models.layout;

import java.util.List;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Key.SetKeyJsonAdapter;
import io.github.in_toto.keys.Key.SetPubKeyJsonAdapter;
import io.github.in_toto.models.layout.rule.Rule;

public class Step extends SupplyChainItem {
    
    @SerializedName("expected_command")
    private List<String> expectedCommand;
    
    @SerializedName("pubkeys")
    @JsonAdapter(SetPubKeyJsonAdapter.class)
    private Set<Key> keys;
    
    private int threshold;
    
    public Step(String name) {
        super(name);
    }

    public Step(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts) {
        super(name, expectedMaterials, expectedProducts);
    }

    @Override
    public SupplyChainItemType getType() {
        return SupplyChainItemType.step;
    }
}
