package io.github.in_toto.exceptions;

import java.util.Set;

import io.github.in_toto.models.layout.rule.Rule;
import io.github.in_toto.models.link.Artifact;

public class RuleVerificationError extends LayoutVerificationError {
    
    private static final long serialVersionUID = -6923219476614859770L;

    public RuleVerificationError(Rule rule, Set<Artifact> filteredArtifacts) {
        super(String.format("\'%s [%s]\' matched the following " + "artifacts: [%s]", rule.getType().name(), rule.getPattern(),
                filteredArtifacts));
    }
}
