// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.FrontEndProcedure

/**
 * A representation of the currently executing procedure
 */
trait Activation {

  /**
   * Definition of the procedure, including basic information like name and argument names
   */
  def procedure: FrontEndProcedure

  /**
   * Parent activation
   */
  def parent: Option[_ <: Activation]
}
