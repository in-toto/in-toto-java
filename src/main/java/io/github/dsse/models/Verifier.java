package io.github.dsse.models;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/** Interface for a DSSE Verifier */
public interface Verifier {

  /**
   * Validates the given message based on the given public key and encrypted message.
   *
   * @param publicKey the public key that should be used to verify the message
   * @param encryptedMessage the encrypted message
   * @param message the message we are validating against.
   * @return true if the given message matches the encryptedMessage
   * @throws NoSuchAlgorithmException thrown when a particular cryptographic algorithm is requested
   *     but is not available in the environment.
   * @throws SignatureException This is the generic Signature exception.
   * @throws InvalidKeySpecException This is the exception for invalid key specifications.
   * @throws InvalidKeyException This is the exception for invalid Keys (invalid encoding, wrong
   *     length, uninitialized, etc).
   */
  boolean verify(byte[] publicKey, byte[] encryptedMessage, String message)
      throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException,
          InvalidKeyException;

  /** Returns the ID of this key, or null if not supported. */
  String getKeyId();
}
