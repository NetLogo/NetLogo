package org.nlogo.util

abstract class Thunk[+T]
{
  lazy val value = compute()
  def compute(): T
}
