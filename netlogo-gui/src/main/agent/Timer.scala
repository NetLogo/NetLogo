// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import System.currentTimeMillis

class Timer {
  private var timer = currentTimeMillis
  def reset() { timer = currentTimeMillis }
  def read = (currentTimeMillis - timer) / 1000.0
}
