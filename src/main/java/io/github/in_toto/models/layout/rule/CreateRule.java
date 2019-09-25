package io.github.in_toto.models.layout.rule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.in_toto.models.link.Artifact;

public final class CreateRule extends Rule implements RuleVerifier  {
    
    public CreateRule(String pattern) {
        super(pattern);
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        Set<Artifact> createdArtifacts = new HashSet<>(products);
        createdArtifacts.removeAll(materials);
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        // Consume filtered artifacts that are products but not materials
        // (products - materials)
        return  filteredArtifacts.stream().filter(createdArtifacts::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public RuleType getType() {
        return RuleType.CREATE;
    }
    
}
