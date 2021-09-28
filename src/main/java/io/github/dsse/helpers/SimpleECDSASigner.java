package io.github.dsse.helpers;

import io.github.dsse.models.Signer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/** Example implementation of a {@link Signer} */
public class SimpleECDSASigner implements Signer {
  private final PrivateKey privateKey;
  private final String keyId;

  public SimpleECDSASigner(PrivateKey privateKey, String keyId) {
    this.privateKey = privateKey;
    this.keyId = keyId;
  }

  @Override
  public byte[] sign(byte[] payload)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign(privateKey);
    signature.update(payload);
    return signature.sign();
  }

  @Override
  public String getKeyId() {
    return this.keyId;
  }
}
