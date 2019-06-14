package io.github.in_toto.models;

import io.github.in_toto.lib.JSONEncoder;

/**
 * A signable class is an abstract superclass that provides a representation method
 * to prepare for signing
 *
 */
interface Signable extends JSONEncoder {
    public String getType();

}
