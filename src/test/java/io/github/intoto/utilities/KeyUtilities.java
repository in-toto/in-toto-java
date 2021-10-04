package io.github.intoto.utilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/** Convenience methods for handling keys. */
public final class KeyUtilities {

  /**
   * Reads Public Key from file using {@link PEMParser}
   *
   * @param file the file that contains the public key
   * @return a PublicKey
   * @throws IOException thrown when there are issues reading the file.
   */
  public static PublicKey readPublicKey(File file) throws IOException {
    try (FileReader keyReader = new FileReader(file)) {
      PEMParser pemParser = new PEMParser(keyReader);
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
      return converter.getPublicKey(publicKeyInfo);
    }
  }

  /**
   * Reads the private key from a file using the PemReader.
   *
   * @param file the file that contains the pkcs8 encoded pivate key.
   * @return the PrivateKey
   * @throws Exception
   */
  public static PrivateKey readPrivateKey(File file) throws Exception {
    KeyFactory factory = KeyFactory.getInstance("EC");

    try (FileReader keyReader = new FileReader(file);
        PemReader pemReader = new PemReader(keyReader)) {

      PemObject pemObject = pemReader.readPemObject();
      byte[] content = pemObject.getContent();
      PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
      return factory.generatePrivate(privKeySpec);
    }
  }
}
