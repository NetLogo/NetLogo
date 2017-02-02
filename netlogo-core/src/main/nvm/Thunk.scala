// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

abstract class Thunk[+T]
{
  lazy val value = compute()
  def compute(): T
}
