// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

/** Scoping lets the compiler know that this primitive introduces scope
 *  so that it can choose how to handle let-variables introduced in it. */
trait Scoping

/** SelfScoping indicates the primitive introduces scope as part of its
 *  operation. */
trait SelfScoping extends Scoping

/** CompilerScoping lets the compiler know that the block at the specified
 *  index should have enterScope/exitScope placed around its statements. */
trait CompilerScoping extends Scoping {
  def scopedBlockIndex: Int
}
