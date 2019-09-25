package io.github.in_toto.models.layout.rule;

import java.util.Set;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.link.Artifact;

public final class DisAllowRule extends Rule implements RuleVerifier {

    public DisAllowRule(String pattern) {
        super(pattern);
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError {
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        if (!filteredArtifacts.isEmpty()) {
            throw new RuleVerificationError(this, filteredArtifacts);
        }
        return filteredArtifacts;
    }

    @Override
    public RuleType getType() {
        return RuleType.DISALLOW;
    }

}
