package io.github.in_toto.models.layout.rule;

import java.util.HashSet;
import java.util.Set;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.link.Artifact;

public final class RequireRule extends Rule implements RuleVerifier  {

    public RequireRule(String pattern) {
        super(pattern);
    }
    
    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError {
        if (filterArtifacts(artifacts).isEmpty()) {
            throw new RuleVerificationError(this, null);
        }
        return  new HashSet<>();
    }

    @Override
    public RuleType getType() {
        return RuleType.REQUIRE;
    }
}
