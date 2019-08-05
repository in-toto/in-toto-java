package io.github.in_toto.models.layout.rule;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.exceptions.FormatError;
import io.github.in_toto.models.layout.rule.Rule.RuleJsonAdapter;

@JsonAdapter(RuleJsonAdapter.class)
public class Rule {
    
    private final RuleType type;
    private final String pattern;
    
    public Rule(RuleType type, String pattern) {
        super();
        this.type = type;
        this.pattern = pattern;
    }

    /**
     * Convenience method to parse the passed rule string into a list of rules.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     * @throws FormatError If the passed rule_string is not a string. If the parsed
     *                     rule_string cannot be unpacked using rulelib.
     */
    public static List<Rule> getRulesFromString(String ruleString) throws FormatError {
        return null;
    }

    public RuleType getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "Rule [type=" + type + ", pattern=" + pattern + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
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
        if (!(obj instanceof Rule)) {
            return false;
        }
        Rule other = (Rule) obj;
        if (!other.canEqual(this)) {
            return false;
        }
        if (pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        } else if (!pattern.equals(other.pattern)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }
    
    public boolean canEqual(Object other) {
        return (other instanceof Rule);
    }
    
    public static class RuleJsonAdapter implements JsonSerializer<Rule>, JsonDeserializer<Rule> {

        @Override
        public Rule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray jsonArray = json.getAsJsonArray(); 
            RuleType ruleType = RuleType.valueOf(jsonArray.get(0).getAsString());
            if (ruleType == RuleType.MATCH) {
                return context.deserialize(json, ComplexRule.class);
            }
            return new Rule(ruleType, jsonArray.get(1).getAsString());
        }

        @Override
        public JsonElement serialize(Rule src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof ComplexRule) {
                return context.serialize((ComplexRule)src);
            }
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(src.getType().name());
            jsonArray.add(src.getPattern());
            return jsonArray;
        }
    }
}
