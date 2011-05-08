package org.nlogo.util

import Exceptions.ignoring

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
  var getJOGLInfoString = "JOGL: (3D View not initialized)" 
  var getGLInfoString = "OpenGL Graphics: (3D View not initialized)" 
  // keep the revision number around because it takes a while to do the lookup - ev 1/18/07
  private var svnRevision: String = null
  private val urlHeader = "URL: https://subversion.assembla.com/svn/nlogo/"
  // not threadsafe, but OK since it's event-thread-only
  def getSVNInfoString = {
    if(svnRevision == null)
      fetchSVNInfoString()
    svnRevision
  }
  private def fetchSVNInfoString() {
    svnRevision = "n/a"
    if(getClass.getResourceAsStream("/system/svnversion.txt") != null) {
      val rev = Utils.getResourceAsStringArray("/system/svnversion.txt")
      svnRevision = rev(1) + ":" + rev(0)
    }
    else ignoring(classOf[java.security.AccessControlException], classOf[java.io.IOException]) {
      def lines(cmd: String) = {
        val reader =
          new java.io.BufferedReader(
            new java.io.InputStreamReader(
              Runtime.getRuntime.exec(cmd).getInputStream))
        Iterator.continually(reader.readLine()).takeWhile(_ != null)
      }
      for(line <- lines("svn info").find(_.startsWith("URL:")))
        svnRevision = line.substring(line.indexOf(urlHeader) + urlHeader.size) + ":"
      svnRevision += lines("svnversion").next()
    }
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
