package io.github.in_toto.models.layout.rule;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.models.layout.Step;
import io.github.in_toto.models.layout.rule.ComplexRule.ComplexRuleJsonAdapter;

/**
 * 
 * @author borstg
 * 
 * " match rules must have the format:\n\t"
          " MATCH <pattern> [IN <source-path-prefix>] WITH"
          " (MATERIALS|PRODUCTS) [IN <destination-path-prefix>] FROM <step>.\n"
 *
 */

@JsonAdapter(ComplexRuleJsonAdapter.class)
public final class ComplexRule extends Rule {
    
    private Path sourcePathPrefix;
    private Path destinationPathPrefix;
    private DestinationType destinationType;
    private Step destinationStep;
    
    public ComplexRule(String pattern) {
        this(pattern, null, null, null, null);
    }
    
    public ComplexRule(String pattern, Path sourcePathPrefix, Path destPrefix,
            DestinationType destinationType, Step destStep) {
        super(RuleType.MATCH, pattern);
        this.sourcePathPrefix = sourcePathPrefix;
        this.destinationPathPrefix = destPrefix;
        this.destinationType = destinationType;
        this.destinationStep = destStep;
    }
    
    public Path getSourcePathPrefix() {
        return sourcePathPrefix;
    }


    public Path getDestinationPathPrefix() {
        return destinationPathPrefix;
    }


    public DestinationType getDestinationType() {
        return destinationType;
    }


    public Step getDestinationStep() {
        return destinationStep;
    }

    public void setSourcePathPrefix(Path sourcePathPrefix) {
        this.sourcePathPrefix = sourcePathPrefix;
    }
    public void setDestinationPathPrefix(Path destinationPathPrefix) {
        this.destinationPathPrefix = destinationPathPrefix;
    }
    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }
    public void setDestinationStep(Step destinationStep) {
        this.destinationStep = destinationStep;
    }
    @Override
    public String toString() {
        return "ComplexRule [sourcePathPrefix=" + sourcePathPrefix + ", destinationPathPrefix=" + destinationPathPrefix
                + ", destinationType=" + destinationType + ", destinationStep=" + destinationStep + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((destinationPathPrefix == null) ? 0 : destinationPathPrefix.hashCode());
        result = prime * result + ((destinationStep == null) ? 0 : destinationStep.hashCode());
        result = prime * result + ((destinationType == null) ? 0 : destinationType.hashCode());
        result = prime * result + ((sourcePathPrefix == null) ? 0 : sourcePathPrefix.hashCode());
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
        ComplexRule other = (ComplexRule) obj;
        if (destinationPathPrefix == null) {
            if (other.destinationPathPrefix != null) {
                return false;
            }
        } else if (!destinationPathPrefix.equals(other.destinationPathPrefix)) {
            return false;
        }
        if (destinationStep == null) {
            if (other.destinationStep != null) {
                return false;
            }
        } else if (!destinationStep.equals(other.destinationStep)) {
            return false;
        }
        if (destinationType != other.destinationType) {
            return false;
        }
        if (sourcePathPrefix == null) {
            if (other.sourcePathPrefix != null) {
                return false;
            }
        } else if (!sourcePathPrefix.equals(other.sourcePathPrefix)) {
            return false;
        }
        return true;
    }
    
    @Override 
    public boolean canEqual(Object other) {
        return (other instanceof ComplexRule);
    }

    public enum DestinationType {
        PRODUCTS, MATERIALS

    }
    
    static class ComplexRuleJsonAdapter implements JsonSerializer<ComplexRule>, JsonDeserializer<ComplexRule> {

        @Override
        public ComplexRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray jsonArray = json.getAsJsonArray();
            int ix = 1;
            String pattern = jsonArray.get(ix).getAsString();
            ComplexRule rule = new ComplexRule(pattern);
            /*
            if (rule_len == 10 and rule_lower[2] == "in" and
                    rule_lower[4] == "with" and rule_lower[6] == "in" and
                    rule_lower[8] == "from"):
                  source_prefix = rule[3]
                  dest_type = rule_lower[5]
                  dest_prefix = rule[7]
                  dest_name = rule[9]

                # ... IN <source-path-prefix> WITH (MATERIALS|PRODUCTS) FROM <step>
                elif (rule_len == 8 and rule_lower[2] == "in" and
                    rule_lower[4] == "with" and rule_lower[6] == "from"):
                  source_prefix = rule[3]
                  dest_type = rule_lower[5]
                  dest_prefix = ""
                  dest_name = rule[7]

                # ... WITH (MATERIALS|PRODUCTS) IN <destination-path-prefix> FROM <step>
                elif (rule_len == 8 and rule_lower[2] == "with" and
                    rule_lower[4] == "in" and rule_lower[6] == "from"):
                  source_prefix = ""
                  dest_type = rule_lower[3]
                  dest_prefix = rule[5]
                  dest_name = rule[7]

                # ... WITH (MATERIALS|PRODUCTS) FROM <step>
                elif (rule_len == 6 and rule_lower[2] == "with" and
                    rule_lower[4] == "from"):
                  source_prefix = ""
                  dest_type = rule_lower[3]
                  dest_prefix = ""
                  dest_name = rule[5]
            */
            ix++;
            String currentString = jsonArray.get(ix).getAsString();
            if (currentString.equals("IN")) {
                ix++;
                rule.setSourcePathPrefix(Paths.get(jsonArray.get(ix).getAsString()));
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("WITH")) {
                ix++;
                rule.setDestinationType(DestinationType.valueOf(jsonArray.get(ix).getAsString()));
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("IN")) {
                ix++;
                rule.setDestinationPathPrefix(Paths.get(jsonArray.get(ix).getAsString()));
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("FROM")) {
                ix++;
                rule.setDestinationStep(new Step(jsonArray.get(ix).getAsString()));
            }
            return rule;
        }

        @Override
        public JsonElement serialize(ComplexRule src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(src.getType().name());
            jsonArray.add(src.getPattern());
            if (src.getSourcePathPrefix() != null) {
                jsonArray.add("IN");
                jsonArray.add(src.getSourcePathPrefix().toString());
            }

            if (src.getDestinationType() != null) {
                jsonArray.add("WITH");
                jsonArray.add(src.getDestinationType().name());
            }
            if (src.getDestinationPathPrefix() != null) {
                jsonArray.add("IN");
                jsonArray.add(src.getDestinationPathPrefix().toString());
            }

            if (src.getDestinationStep() != null) {
                jsonArray.add("FROM");
                jsonArray.add(src.getDestinationStep().getName());
            }
            return jsonArray;
        }
    }
    
}
