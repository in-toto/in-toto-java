package io.github.intoto.utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/** Utility class to generate new private.key and public.key files */
public class KeyGeneratorForTest {
  public static void main(String[] args)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Security.addProvider(new BouncyCastleProvider());
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair keyPair = keyGen.generateKeyPair();
    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();

    X509EncodedKeySpec x509EncodedKeySpecPublic = new X509EncodedKeySpec(publicKey.getEncoded());
    PKCS8EncodedKeySpec pkcs8EncodedKeySpecPrivate =
        new PKCS8EncodedKeySpec(privateKey.getEncoded());

    try {
      FileOutputStream fos = new FileOutputStream("public.key");
      fos.write(x509EncodedKeySpecPublic.getEncoded());
      fos.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      FileOutputStream fos = new FileOutputStream("private.key");
      fos.write(pkcs8EncodedKeySpecPrivate.getEncoded());
      fos.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
