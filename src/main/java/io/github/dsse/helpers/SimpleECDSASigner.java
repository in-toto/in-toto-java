package io.github.dsse.helpers;

import io.github.dsse.models.Signer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class SimpleECDSASigner implements Signer {
  private final PrivateKey privateKey;

  public SimpleECDSASigner(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public byte[] sign(String payload)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance("SHA1withECDSA");
    signature.initSign(privateKey);
    signature.update(payload.getBytes());
    return signature.sign();
  }

  @Override
  public String getKeyId() {
    return null;
  }
}
