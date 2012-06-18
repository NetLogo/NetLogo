// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object HexString
{
  /** Convert byte array to hex string with lookup table.
      Algorithm from code example at http://www.mindprod.com/jgloss/hex.html. */
  def toHexString(bs: Array[Byte]): String =
    bs.flatMap(b => List(hexChar((b & 0xf0) >>> 4),
                         hexChar( b & 0x0f)))
      .mkString
  def toHexString(ns:Array[Int]) =
    ns.flatMap(n => List(hexChar((n >> 28) & 0xf),
                         hexChar((n >> 24) & 0xf),
                         hexChar((n >> 20) & 0xf),
                         hexChar((n >> 16) & 0xf),
                         hexChar((n & 0xf000) >> 12),
                         hexChar((n & 0x0f00) >>  8),
                         hexChar((n & 0x00f0) >>  4),
                         hexChar((n & 0x000f))))
      .mkString
  // table to convert a nibble to a hex char
  private val hexChar = "0123456789ABCDEF"
}
