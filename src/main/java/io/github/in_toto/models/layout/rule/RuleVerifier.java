package io.github.in_toto.models.layout.rule;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.models.link.Artifact;

public interface RuleVerifier {
    
    public Set<Artifact> verify(final Set<Artifact> artifacts, final Set<Artifact> destinationMaterials, final Set<Artifact> destinationProducts) throws RuleVerificationError;
    
    public String getPattern();
    
    public RuleType getType();
    
    public default Set<Artifact> filterArtifacts(Set<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.getPattern());
        Set<Artifact> filteredArtifacts = new HashSet<>();
        Iterator<Artifact> artifactIterator = artifacts.iterator();
        while (artifactIterator.hasNext()) {
            Artifact artifact = artifactIterator.next();
            if (matcher.matches(Paths.get(artifact.getUri()))) {
                filteredArtifacts.add(artifact);
            }
        }
        return filteredArtifacts;
    }

}
