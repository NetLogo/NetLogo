// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util


object SysInfo
{
  def getProperty(property: String): String =
    try System.getProperty(property)
    catch { case _: RuntimeException => null }
  def getVMInfoString = {
    var result = getProperty("java.vm.name") +
      " " + getProperty("java.version") +
      " (" + getProperty("java.vendor")
    if(getProperty("java.fullversion") != null)
      result += "; " + getProperty("java.fullversion")
    else if(getProperty("java.runtime.version") != null)
      result += "; " + getProperty("java.runtime.version")
    result += ")"
    result
  }
  def isLibgcj =
    getVMInfoString.indexOf("libgcj") != -1
  def getOSInfoString =
    "operating system: " + getProperty("os.name") +
      " " + getProperty("os.version") +
      " ("  + getProperty("os.arch") + " processor)"
  def getMemoryInfoString = {
    val runtime = Runtime.getRuntime()
    System.gc()
    val total = runtime.totalMemory() / 1024 / 1024
    val free = runtime.freeMemory() / 1024 / 1024
    val max = runtime.maxMemory() / 1024 / 1024
    "Java heap: " +
      "used = " + (total - free) +
      " MB, free = " + free +
      " MB, max = " + max +
      " MB"
  }
  def getBrowserInfoString =
    try {
      var nulls = 0
      var browser = getProperty("browser")
      if(browser == null) {
        nulls += 1
        browser = "(unknown browser)"
      }
      var version = getProperty("browser.version")
      if(version == null) {
        nulls += 1
        version = "(unknown version)"
      }
      var vendor = getProperty("browser.vendor")
      if(vendor == null) {
        nulls += 1
        vendor = ""
      }
      else
        vendor = " (" + vendor + ")"
      if(nulls == 3) null
      else browser + " " + version + vendor
    }
    // fail at least somewhat gracefully if we run into permissions problems
    catch { case ex: RuntimeException => null }
  def getScalaVersionString =
    "Scala " + scala.util.Properties.versionString
}
