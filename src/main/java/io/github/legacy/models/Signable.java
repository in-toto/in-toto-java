package io.github.legacy.models;

import io.github.legacy.lib.JSONEncoder;

/**
 * A signable class is an abstract superclass that provides a representation method to prepare for
 * signing
 */
@Deprecated
abstract class Signable implements JSONEncoder {
  /** Subclasses must define the _type field appropriately for serialization */
  protected String _type;

  public Signable() {
    this._type = getType();
  }

  public abstract String getType();
}
