// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait RandomServices {
  def mainRNG: MersenneTwisterFast
  def auxRNG: MersenneTwisterFast
}
