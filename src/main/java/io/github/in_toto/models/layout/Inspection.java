package io.github.in_toto.models.layout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.github.in_toto.models.layout.rule.Rule;

public final class Inspection extends SupplyChainItem {
    
    private final List<String> run;

    public Inspection(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts, List<String> run) {
        super(name, SupplyChainItemType.inspection, expectedMaterials, expectedProducts);
        if (run != null) {
            this.run = Collections.unmodifiableList(new ArrayList<>(run));
        } else {
            this.run = Collections.unmodifiableList(new ArrayList<>()); 
        }
    }
    
    public List<String> getRun() {
        return run;
    }

    static class InspectionArrayJsonAdapter implements JsonSerializer<Inspection>, JsonDeserializer<Inspection> {

        @Override
        public Inspection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray jsonArray = json.getAsJsonArray();
            return context.deserialize(jsonArray.get(0).getAsJsonObject(), Inspection.class) ;
        }

        @Override
        public JsonElement serialize(Inspection src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray jsonArray = new JsonArray();           
            jsonArray.add(context.serialize(src));
            return jsonArray;
        }
    }

    @Override
    public String toString() {
        return "Inspection [run=" + run + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((run == null) ? 0 : run.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Inspection other = (Inspection) obj;
        if (run == null) {
            if (other.run != null) {
                return false;
            }
        } else if (!run.equals(other.run)) {
            return false;
        }
        return true;
    }
    
}
