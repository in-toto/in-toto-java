package io.github.intoto.legacy.lib;

import io.github.intoto.legacy.keys.Key;
import io.github.intoto.legacy.keys.RSAKey;
import io.github.intoto.legacy.models.Link;
import java.io.File;
import java.io.IOException;

/** Hello world! */
public class App {
  public static void main(String[] args) {
    Key thiskey = RSAKey.read("src/test/resources/somekey.pem");

    Link link = new Link(null, null, "test", null, null, null);
    try {

      System.out.println("Loaded key ID: " + thiskey.computeKeyId());

      File fl = new File("alice");
      fl.createNewFile();

    } catch (IOException e) {
      System.out.println("Working Directory = " + System.getProperty("user.dir"));
      throw new RuntimeException("The file alice couldn't be created");
    }

    link.addMaterial("alice");
    System.out.println("dumping file...");
    link.sign(thiskey);
    link.dump("somelink.link");
  }
}
