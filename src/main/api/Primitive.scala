// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

/**
 * Top-level interface for primitives (commands or reporters).  Not to be implemented directly; the
 * <code>Command</code> or <code>Reporter</code> interface should be used instead.
 *
 * @see Command
 * @see Reporter
 */

trait Primitive {

  /**
   * Returns Syntax which specifies the syntax that is acceptable for this primitive.  Used by the
   * compiler for type-checking.
   *
   * @return the Syntax for the primitive.
   * @see Syntax
   */
  def getSyntax: core.Syntax

}
