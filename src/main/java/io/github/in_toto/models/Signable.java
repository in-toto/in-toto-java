package io.github.in_toto.models;

import com.google.gson.annotations.JsonAdapter;

import io.github.in_toto.lib.JSONEncoder;

/**
 * A signable class is an abstract superclass that provides a representation method
 * to prepare for signing
 *
 */
@JsonAdapter(SignableDeserializer.class)
public interface Signable extends JSONEncoder {
    
    public String getName();
    
    public String getFullName(String shortKey);

    public SignableType getType();
}
