package io.github.in_toto.models.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Key.SetPubKeyJsonAdapter;
import io.github.in_toto.models.layout.rule.Rule;

public final class Step extends SupplyChainItem {
    
    @SerializedName("expected_command")
    private final List<String> expectedCommand;
    
    @SerializedName("pubkeys")
    @JsonAdapter(SetPubKeyJsonAdapter.class)
    private final Set<Key> authorizedKeys;
    
    private final int threshold;
    
    public Step(String name) {
        this(name, null, null, null, null, 0);
    }

    public Step(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts, List<String> expectedCommand, Set<Key> authorizedKeys, int threshold) {
        super(name, SupplyChainItemType.step, expectedMaterials, expectedProducts);
        if (expectedCommand != null) {
            this.expectedCommand = Collections.unmodifiableList(new ArrayList<>(expectedCommand));
        } else {
            this.expectedCommand = Collections.unmodifiableList(new ArrayList<>()); 
        }
        if (authorizedKeys != null) {
            this.authorizedKeys = Collections.unmodifiableSet(new HashSet<>(authorizedKeys));
        } else {
            this.authorizedKeys = Collections.unmodifiableSet(new HashSet<>()); 
        }
        this.threshold = threshold;
    }

    public List<String> getExpectedCommand() {
        return expectedCommand;
    }

    public Set<Key> getAuthorizedKeys() {
        return authorizedKeys;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public SupplyChainItemType getType() {
        return SupplyChainItemType.step;
    }

    @Override
    public String toString() {
        return "Step [expectedCommand=" + expectedCommand + ", authorizedKeys=" + authorizedKeys + ", threshold="
                + threshold + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((authorizedKeys == null) ? 0 : authorizedKeys.hashCode());
        result = prime * result + ((expectedCommand == null) ? 0 : expectedCommand.hashCode());
        result = prime * result + threshold;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Step other = (Step) obj;
        if (authorizedKeys == null) {
            if (other.authorizedKeys != null) {
                return false;
            }
        } else if (!authorizedKeys.equals(other.authorizedKeys)) {
            return false;
        }
        if (expectedCommand == null) {
            if (other.expectedCommand != null) {
                return false;
            }
        } else if (!expectedCommand.equals(other.expectedCommand)) {
            return false;
        }
        if (threshold != other.threshold) {
            return false;
        }
        return true;
    }
}
