package io.github.dsse.helpers;

import io.github.dsse.models.Verifier;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SimpleECDSAVerifier implements Verifier {

  private PublicKey publicKey;

  @Override
  public boolean verify(byte[] publicKeyByteArray, byte[] encryptedMessage, String message)
      throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException,
          InvalidKeyException {
    Signature signature = Signature.getInstance("SHA1withECDSA");
    // Create the public key from the byte array
    PublicKey publicKey =
        KeyFactory.getInstance("ECDSA").generatePublic(new X509EncodedKeySpec(publicKeyByteArray));
    this.publicKey = publicKey;
    signature.initVerify(publicKey);
    signature.update(message.getBytes());
    return signature.verify(encryptedMessage);
  }

  @Override
  public String getKeyId() {
    return publicKey.toString();
  }
}
