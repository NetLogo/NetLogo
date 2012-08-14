// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

abstract class Thunk[+T]
{
  lazy val value = compute()
  def compute(): T
}
