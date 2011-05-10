package org.nlogo.api;

/**
 * <code>PrimitiveManager</code> ships extension primitives and
 * associated names to NetLogo.
 */
public interface PrimitiveManager {

  /**
   * Adds a new primitive. The primitive remains effective as long as this model remains
   * loaded.
   *
   * @param name the name of the primitive that will be use in NetLogo code
   * @param prim an instance of <code>Primitive</code> to associate with <code>name</code>
   */
  void addPrimitive(String name, Primitive prim);

  /**
   * Should the primitives be automatically imported into the
   * top-level NetLogo namespace.  If false, they may only be referred
   * to in NetLogo code as "extensionname:primitive".  If true, they
   * may also optionally be referred to simply as "primitive".  The
   * default is false.
   */
  void autoImportPrimitives_$eq(boolean val);

  boolean autoImportPrimitives();
}
