package io.github.intoto.slsa.models.v1;

import java.util.Map;
import java.util.Objects;
import org.hibernate.validator.constraints.URL;

public class ResourceDescriptor {

  private String name;

  @URL(message = "Not a valid URI")
  private String uri;

  private Map<String, String> digest;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Map<String, String> getDigest() {
    return digest;
  }

  public void setDigest(Map<String, String> digest) {
    this.digest = digest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceDescriptor resourceDescriptor = (ResourceDescriptor) o;
    return Objects.equals(name, resourceDescriptor.name)
        && Objects.equals(uri, resourceDescriptor.uri)
        && Objects.equals(digest, resourceDescriptor.digest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name, uri, digest);
  }
}
