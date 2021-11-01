package io.github.intoto.implementations;

import io.github.intoto.dsse.models.Signer;

/** Fake Signer implementation for testing only. */
public class FakeSigner implements Signer {

  @Override
  public byte[] sign(byte[] payload) {
    return payload;
  }

  @Override
  public String getKeyId() {
    return "Fake-Signer-Key-ID";
  }
}
