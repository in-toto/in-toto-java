package io.github.intoto.dsse.models;

import dev.sigstore.KeylessSignerException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/** Interface for a DSSE Signer. */
public interface Signer {

  /**
   * Returns the signature of the payload.
   *
   * @param payload the message that you want to sign.
   */
  byte[] sign(byte[] payload)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException, CertificateException, IOException, InvalidKeySpecException, KeylessSignerException;

  /** Returns the ID of this key, or null if not supported. */
  String getKeyId();
}
