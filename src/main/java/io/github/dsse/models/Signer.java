package io.github.dsse.models;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/** Interface for a DSSE Signer. */
public interface Signer {

  /**
   * Returns the signature of the payload.
   *
   * @param payload the message that you want to sign.
   */
  byte[] sign(byte[] payload)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;

  /** Returns the ID of this key, or null if not supported. */
  String getKeyId();
}
