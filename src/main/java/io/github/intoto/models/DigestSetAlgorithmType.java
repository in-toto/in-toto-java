package io.github.intoto.models;

/** Helper Enum with common algorithm types, could be used to populate the {@link Subject} digest */
public enum DigestSetAlgorithmType {
  SHA256,
  SHA224,
  SHA384,
  SHA512,
  SHA512_224,
  SHA512_256,
  SHA3_224,
  SHA3_256,
  SHA3_384,
  SHA3_512,
  SHAKE128,
  SHAKE256,
  BLAKE2B,
  BLAKE2S,
  RIPEMD160,
  SM3,
  GOST,
  SHA1,
  MD5
}
