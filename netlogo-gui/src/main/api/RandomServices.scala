// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.MersenneTwisterFast

trait RandomServices {
  def mainRNG: MersenneTwisterFast
  def auxRNG: MersenneTwisterFast
}
