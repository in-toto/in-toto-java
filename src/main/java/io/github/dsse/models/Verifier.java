package io.github.dsse.models;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public interface Verifier {
  boolean verify(byte[] publicKey, byte[] encryptedMessage, String message)
      throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException,
          InvalidKeyException;

  String getKeyId();
}
