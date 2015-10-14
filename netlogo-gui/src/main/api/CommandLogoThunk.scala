// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait CommandLogoThunk {
  /**
   * @return whether the code did a "stop" at the top level
   * @throws LogoException
   */
  @throws(classOf[LogoException])
  def call(): Boolean
}
