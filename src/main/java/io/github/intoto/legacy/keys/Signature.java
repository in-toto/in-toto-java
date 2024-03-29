package io.github.intoto.legacy.keys;

/**
 * Public class representing an in-toto Signature.
 *
 * <p>This class is an abstract template from which all the keys for different signing algorithms
 * will be based off of.
 */
public class Signature {
  String keyid;
  String sig;

  public Signature(String keyid, String sig) {
    this.keyid = keyid;
    this.sig = sig;
  }

  public String getKeyId() {
    return this.keyid;
  }
}
