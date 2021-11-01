package io.github.intoto.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/** Helper Enum with common algorithm types, could be used to populate the {@link Subject} digest */
public enum DigestSetAlgorithmType {
  @JsonProperty("sha256")
  SHA256("sha256"),
  SHA224("sha224"),
  SHA384("sha384"),
  SHA512("sha512"),
  SHA512_224("sha512_224"),
  SHA512_256("sha512_256"),
  SHA3_224("sha3_224"),
  SHA3_256("sha3_256"),
  SHA3_384("sha3_384"),
  SHA3_512("sha3_512"),
  SHAKE128("shake128"),
  SHAKE256("shake256"),
  BLAKE2B("blake2B"),
  BLAKE2S("blake2S"),
  RIPEMD160("ripemd160"),
  SM3("sm3"),
  GOST("gost"),
  SHA1("sha1"),
  MD5("md5");

  private final String value;

  DigestSetAlgorithmType(String key) {
    this.value = key;
  }

  @JsonCreator
  public static DigestSetAlgorithmType fromString(String key) {
    return key == null ? null : DigestSetAlgorithmType.valueOf(key.toUpperCase());
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
