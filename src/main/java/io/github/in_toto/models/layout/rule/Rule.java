package io.github.in_toto.models.layout.rule;

import java.lang.reflect.Type;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.layout.rule.Rule.RuleJsonAdapter;
import io.github.in_toto.models.link.Artifact;

@JsonAdapter(RuleJsonAdapter.class)
public abstract class Rule implements RuleVerifier {
    private final String pattern;
    
    public Rule(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products)  throws RuleVerificationError {
        return filterArtifacts(artifacts);
    }

    @Override
    public String toString() {
        return "Rule [pattern=" + pattern + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
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
        Rule other = (Rule) obj;
        if (pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        } else if (!pattern.equals(other.pattern)) {
            return false;
        }
        return true;
    }
    
    public static class RuleJsonAdapter implements JsonSerializer<Rule>, JsonDeserializer<Rule> {

        @Override
        public Rule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray jsonArray = json.getAsJsonArray(); 
            RuleType ruleType = RuleType.valueOf(jsonArray.get(0).getAsString());            
            String pattern = jsonArray.get(1).getAsString();
            switch (ruleType) {
            case ALLOW: return new AllowRule(pattern);
            case CREATE: return new CreateRule(pattern);
            case DELETE: return new DeleteRule(pattern);
            case DISALLOW: return new DisAllowRule(pattern);
            case MATCH: return context.deserialize(json, MatchRule.class);
            case MODIFY: return new ModifyRule(pattern);
            case REQUIRE: return new RequireRule(pattern);
            default: return null;
            }
        }

        @Override
        public JsonElement serialize(Rule src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof MatchRule) {
                return context.serialize((MatchRule)src);
            }
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(src.getType().name());
            jsonArray.add(src.getPattern());
            return jsonArray;
        }
    }
}
