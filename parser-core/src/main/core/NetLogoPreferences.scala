// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.prefs.Preferences

// this helper object minimizes duplication of the Java Preferences stuff below,
// and it reduces the risk of preferences getting stored in multiple places (Isaac B 6/17/25)
object NetLogoPreferences {
  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  def get(name: String, default: String): String =
    prefs.get(name, default)

  def getBoolean(name: String, default: Boolean): Boolean =
    prefs.getBoolean(name, default)

  def getInt(name: String, default: Int): Int =
    prefs.getInt(name, default)

  def getDouble(name: String, default: Double): Double =
    prefs.getDouble(name, default)

  def getByteArray(name: String, default: Array[Byte]): Array[Byte] =
    prefs.getByteArray(name, default)

  def put(name: String, value: String): Unit =
    prefs.put(name, value)

  def putBoolean(name: String, value: Boolean): Unit =
    prefs.putBoolean(name, value)

  def putInt(name: String, value: Int): Unit =
    prefs.putInt(name, value)

  def putDouble(name: String, value: Double): Unit =
    prefs.putDouble(name, value)

  def putByteArray(name: String, value: Array[Byte]): Unit =
    prefs.putByteArray(name, value)

  def remove(name: String): Unit =
    prefs.remove(name)
}
