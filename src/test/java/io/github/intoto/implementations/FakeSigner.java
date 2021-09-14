package io.github.intoto.implementations;

import io.github.dsse.models.Signer;
import java.nio.charset.StandardCharsets;

/** Fake Signer implementation for testing only. */
public class FakeSigner implements Signer {

  @Override
  public byte[] sign(String payload) {
    return payload.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String getKeyId() {
    return "Fake-Signer-Key-ID";
  }
}
