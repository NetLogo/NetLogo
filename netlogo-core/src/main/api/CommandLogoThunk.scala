// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.util.Try

trait CommandLogoThunk {
  /**
   * @return The result of executing this code.
   *        returns `Success(true)` true if the code did a "stop" at the top level,
   *        `Success(false)` if the code didn't "stop" at the top level, and
   *        `Failure` if the code raised an exception while executing.
   * @throws LogoException
   */
  @throws(classOf[LogoException])
  def call(): Try[Boolean]
}
