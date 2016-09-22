// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.concurrent.Future
import java.awt.image.BufferedImage

/** ControlSet is an abstraction over the various runtime-only parts of model
 *  state. For instance, what output is shown to the user? What does
 *  the interface look like for the user. At the moment, it is likely
 *  it will only be needed for NetLogo UI, but there's a chance it could also
 *  be useful for some aspect of Headless in the future.
 *
 *  Methods on this class should perform the "correct" action regardless of whether
 *  they are called from the job or swing event thread. `Future` is used because these
 *  computations may be asynchronous.
 */
trait ControlSet {
  def userInterface: Future[BufferedImage]
  def userOutput: Future[String]
}
