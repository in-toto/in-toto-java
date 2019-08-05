package io.github.in_toto.models.layout;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.github.in_toto.models.layout.rule.Rule;

public class Inspection extends SupplyChainItem {
    
    private List<String> run;

    public Inspection(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts, List<String> run) {
        super(name, expectedMaterials, expectedProducts);
        this.run = run;
    }

    @Override
    public SupplyChainItemType getType() {
        return SupplyChainItemType.inspection;
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
}
