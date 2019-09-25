package io.github.in_toto.models.layout.rule;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.models.layout.Step;
import io.github.in_toto.models.layout.rule.MatchRule.MatchRuleJsonAdapter;
import io.github.in_toto.models.link.Artifact;

/**
 * 
 * @author borstg
 * 
 *         " match rules must have the format:\n\t" " MATCH <pattern> [IN
 *         <source-path-prefix>] WITH" " (MATERIALS|PRODUCTS) [IN
 *         <destination-path-prefix>] FROM <step>.\n"
 *
 */

@JsonAdapter(MatchRuleJsonAdapter.class)
public final class MatchRule extends Rule implements RuleVerifier {

    private final String sourcePathPrefix;
    private final String destinationPathPrefix;
    private final DestinationType destinationType;
    private final Step destinationStep;

    public MatchRule(String pattern, String sourcePathPrefix, String destPrefix, DestinationType destinationType,
            Step destStep) {
        super(pattern);
        this.sourcePathPrefix = this.normalizePath(sourcePathPrefix);
        this.destinationPathPrefix = this.normalizePath(destPrefix);
        this.destinationType = destinationType;
        this.destinationStep = destStep;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String result = Artifact.normalizePath(path);
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

    public String getSourcePathPrefix() {
        return sourcePathPrefix;
    }

    public String getDestinationPathPrefix() {
        return destinationPathPrefix;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public Step getDestinationStep() {
        return destinationStep;
    }

    /**
     * Filters artifacts from artifact queue using rule pattern and optional rule
     * source prefix and consumes them if there is a corresponding destination
     * artifact, filtered using the same rule pattern and an optional rule
     * destination prefix, and source and destination artifacts have matching
     * hashes.
     * 
     * NOTE: The destination artifacts are extracted from the links dictionary,
     * using destination name and destination type from the rule data. The source
     * artifacts could also be extracted from the links dictionary, but would
     * require the caller to pass source name and source type, as those are not
     * encoded in the rule. However, we choose to let the caller directly pass the
     * relevant artifacts.
     *
     * @param artifacts Not yet consumed artifacts.
     * @param step      Step on which this Rule is defined.
     * @param linkMap   Map of step names with valid links.
     * @return Set of consumed artifacts.
     */
    @Override
    public Set<Artifact> verify(final Set<Artifact> artifacts, final Set<Artifact> destinationMaterials, final Set<Artifact> destinationProducts) {
        Set<Artifact> consumed = new HashSet<>();
        // Extract destination artifacts from destination link
        Set<Artifact> destinationArtifacts = null;
        if (this.getDestinationType() == DestinationType.MATERIALS) {
            if (destinationMaterials == null) {
                return consumed;
            }
            destinationArtifacts = new HashSet<>(destinationMaterials);
        } else {
            if (destinationProducts == null) {
                return consumed;
            }
            destinationArtifacts = new HashSet<>(destinationProducts);
        }
        
        Set<Artifact> filteredSourceArtifacts = null;
        // Filter part 1 - Filter artifacts using optional source prefix, and subtract
        // prefix before filtering with rule pattern (see filter part 2) to prevent
        // globbing in the prefix.
        if (this.getSourcePathPrefix() != null) {
            Set<Artifact> dePreFixedSourceArtifacts = new HashSet<>();

            for (Artifact artifact : artifacts) {
                if (artifact.getUri().startsWith(this.getSourcePathPrefix())) {
                    dePreFixedSourceArtifacts.add(new Artifact(artifact.getUri().substring(this.getSourcePathPrefix().length()), artifact.getHash()));
                }
            }

            // re-apply prefix
            filteredSourceArtifacts = super.filterArtifacts(dePreFixedSourceArtifacts);
            Set<Artifact> preFixedSourceArtifacts = new HashSet<>();
            for (Artifact artifact : filteredSourceArtifacts) {
                preFixedSourceArtifacts.add(new Artifact(this.getSourcePathPrefix()+artifact.getUri(), artifact.getHash()));
            }
            filteredSourceArtifacts = preFixedSourceArtifacts;
        } else {
            filteredSourceArtifacts = super.filterArtifacts(artifacts);
        }
        
        // Iterate over filtered source paths and try to match the corresponding
        // source artifact hash with the corresponding destination artifact hash
        for (Artifact artifact : filteredSourceArtifacts) {
            // If a destination prefix was specified, the destination artifact should
            // be queried with the full destination path, i.e. the prefix joined with
            // the globbed path.
            Artifact destinationArtifact = artifact;
            if (this.getDestinationPathPrefix() != null) {
                destinationArtifact = new Artifact(this.getDestinationPathPrefix() + artifact.getUri(),
                        artifact.getHash());
            }            
            // Source and destination matched, consume artifact
            if (destinationArtifacts.contains(destinationArtifact)) {
                consumed.add(artifact);
            }
        }
        return consumed;
    }

    @Override
    public String toString() {
        return "MatchRule [sourcePathPrefix=" + sourcePathPrefix + ", destinationPathPrefix=" + destinationPathPrefix
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
        MatchRule other = (MatchRule) obj;
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

    public enum DestinationType {
        PRODUCTS, MATERIALS

    }

    static class MatchRuleJsonAdapter implements JsonSerializer<MatchRule>, JsonDeserializer<MatchRule> {

        @Override
        public MatchRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonArray jsonArray = json.getAsJsonArray();
            int ix = 1;
            String pattern = jsonArray.get(ix).getAsString();
            String sourcePathPrefix = null;
            DestinationType destinationType = null;
            String destinationPathPrefix = null;
            Step destinationStep = null;
            /*
             * if (rule_len == 10 and rule_lower[2] == "in" and rule_lower[4] == "with" and
             * rule_lower[6] == "in" and rule_lower[8] == "from"): source_prefix = rule[3]
             * dest_type = rule_lower[5] dest_prefix = rule[7] dest_name = rule[9]
             * 
             * # ... IN <source-path-prefix> WITH (MATERIALS|PRODUCTS) FROM <step> elif
             * (rule_len == 8 and rule_lower[2] == "in" and rule_lower[4] == "with" and
             * rule_lower[6] == "from"): source_prefix = rule[3] dest_type = rule_lower[5]
             * dest_prefix = "" dest_name = rule[7]
             * 
             * # ... WITH (MATERIALS|PRODUCTS) IN <destination-path-prefix> FROM <step> elif
             * (rule_len == 8 and rule_lower[2] == "with" and rule_lower[4] == "in" and
             * rule_lower[6] == "from"): source_prefix = "" dest_type = rule_lower[3]
             * dest_prefix = rule[5] dest_name = rule[7]
             * 
             * # ... WITH (MATERIALS|PRODUCTS) FROM <step> elif (rule_len == 6 and
             * rule_lower[2] == "with" and rule_lower[4] == "from"): source_prefix = ""
             * dest_type = rule_lower[3] dest_prefix = "" dest_name = rule[5]
             */
            ix++;
            String currentString = jsonArray.get(ix).getAsString();
            if (currentString.equals("IN")) {
                ix++;
                // get and normalize path
                sourcePathPrefix = this.normalizePath(jsonArray.get(ix).getAsString());
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("WITH")) {
                ix++;
                destinationType = DestinationType.valueOf(jsonArray.get(ix).getAsString());
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("IN")) {
                ix++;
                // get and normalize path
                destinationPathPrefix = this.normalizePath(jsonArray.get(ix).getAsString());
                ix++;
                currentString = jsonArray.get(ix).getAsString();
            }
            if (currentString.equals("FROM")) {
                ix++;
                destinationStep = new Step(jsonArray.get(ix).getAsString());
            }

            return new MatchRule(pattern, sourcePathPrefix, destinationPathPrefix, destinationType,
                    destinationStep);
        }

        private String normalizePath(String path) {
            return path.replace("\\", "/");
        }

        @Override
        public JsonElement serialize(MatchRule src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(src.getType().name());
            jsonArray.add(src.getPattern());
            if (src.getSourcePathPrefix() != null) {
                jsonArray.add("IN");
                jsonArray.add(src.getSourcePathPrefix());
            }
            if (src.getDestinationType() != null) {
                jsonArray.add("WITH");
                jsonArray.add(src.getDestinationType().name());
            }
            if (src.getDestinationPathPrefix() != null) {
                jsonArray.add("IN");
                jsonArray.add(src.getDestinationPathPrefix());
            }
            if (src.getDestinationStep() != null) {
                jsonArray.add("FROM");
                jsonArray.add(src.getDestinationStep().getName());
            }
            return jsonArray;
        }
    }

    @Override
    public RuleType getType() {
        return RuleType.MATCH;
    }

}
