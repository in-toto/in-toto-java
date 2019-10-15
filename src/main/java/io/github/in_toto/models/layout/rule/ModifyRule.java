package io.github.in_toto.models.layout.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.in_toto.models.link.Artifact;

public final class ModifyRule extends Rule implements RuleVerifier {

    public ModifyRule(String pattern) {
        super(pattern);
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        Set<Artifact> consumed = new HashSet<>();
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = super.filterArtifacts(artifacts);
        Map<String, Artifact> productsMap = this.createMap(products);
        Map<String, Artifact> materialsMap = this.createMap(materials);
        
        for (Artifact artifact:filteredArtifacts) {
            // Consume filtered artifacts that have different hashes
            if (productsMap.containsKey(artifact.getUri()) 
                    && materialsMap.containsKey(artifact.getUri())
                    && (!productsMap.get(artifact.getUri()).getHash().equals(materialsMap.get(artifact.getUri()).getHash()))) {
                consumed.add(artifact);
            }
        }
        return  consumed;
    }
    
    private Map<String, Artifact> createMap(Set<Artifact> artifacts) {
        Map<String, Artifact> result = new HashMap<>();
        for (Artifact artifact:artifacts) {
            result.put(artifact.getUri(), artifact);
        }
        return result;
    }

    @Override
    public RuleType getType() {
        return RuleType.MODIFY;
    }
    

}
