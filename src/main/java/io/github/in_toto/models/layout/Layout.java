package io.github.in_toto.models.layout;

import java.util.Date;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Key.SetKeyJsonAdapter;
import io.github.in_toto.models.Signable;
import io.github.in_toto.models.SignableType;
import io.github.in_toto.models.layout.Inspection.InspectionArrayJsonAdapter;

public class Layout implements Signable {

    private String name;
    @SerializedName("_type")
    private SignableType type = SignableType.layout;
    private Set<Step> steps;
    @JsonAdapter(InspectionArrayJsonAdapter.class)
    private Inspection inspect;
    @JsonAdapter(SetKeyJsonAdapter.class)
    private Set<Key> keys;
    private Date expires;
    private String readme;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SignableType getType() {
        return this.type;
    }

    @Override
    public String getFullName(String shortKey) {
        return this.name+"."+this.type;
    }

}
