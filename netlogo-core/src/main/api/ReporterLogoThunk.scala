// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.util.Try

/**
 * This is used by SliderConstraint.  Sliders have snippets of Logo code associated with them, which
 * need be to be evaluated to produce numbers for the min, max, and increment.  But here in the
 * org.nlogo.agent package we don't know anything about Logo code and how to run it.  This interface
 * lets other code keep us in the dark by giving us a black box that magically produces an object
 * whenever we ask for one.  (In the case of SliderConstraint the object should be a boxed double.)
 */

trait ReporterLogoThunk {
  @throws(classOf[LogoException])
  def call(): Try[AnyRef]
}
