package io.github.in_toto.models.layout.rule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.in_toto.models.link.Artifact;

public final class DeleteRule extends Rule implements RuleVerifier {

    public DeleteRule(String pattern) {
        super(pattern);
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        // Consume filtered artifacts that are products but not materials
        // (materials - products)

        Set<Artifact> deletedArtifacts = new HashSet<>(materials);
        deletedArtifacts.removeAll(products);
        return  filteredArtifacts.stream().filter(deletedArtifacts::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public RuleType getType() {
        return RuleType.DELETE;
    }
    

}
