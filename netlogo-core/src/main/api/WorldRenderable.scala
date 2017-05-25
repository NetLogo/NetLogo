// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait WorldRenderable {
  def patchColorsDirty: Boolean
  def markPatchColorsDirty()
  def markPatchColorsClean()

  /**
   * Returns true if there is at least one partially transparent turtle, patch, link, or 3D stamp
   * present. This determines whether it is necessary to sort the objects by their distance to the
   * observer before rendering, which is necessary for transparency to work in OpenGL.
   *
   * @return True if the scene has at least one partially transparent item
   */
  def mayHavePartiallyTransparentObjects: Boolean
}

trait WorldWithWorldRenderable extends World with WorldRenderable
