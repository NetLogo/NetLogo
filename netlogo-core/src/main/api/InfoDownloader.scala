// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ File, IOException }
import java.lang.Boolean
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.security.{ DigestInputStream, MessageDigest }
import java.util.Arrays
import java.util.prefs.{ Preferences => JPreferences }

trait InfoDownloader {

  private val prefs = JPreferences.userNodeForPackage(getClass)

  def prefsKey: String

  def enabled: Boolean =
    !Boolean.getBoolean(s"netlogo.$prefsKey.disabled")

  /** Downloads the URL and update the GUI if the hash is different */
  def apply(url: URL, callback: File => Unit = ((_: File) => ())): Unit = {
    if (enabled) {
      Exceptions.ignoring(classOf[IOException]) {

        val md   = MessageDigest.getInstance("MD5")
        val conn = url.openConnection.asInstanceOf[HttpURLConnection]

        if (conn.getResponseCode == 200) {
          val response = new DigestInputStream(conn.getInputStream, md)
          Files.copy(response, Paths.get(FileIO.perUserFile(urlToHash(url))), StandardCopyOption.REPLACE_EXISTING)
        }

        val localHash = prefs.getByteArray(urlToFullHash(url), null)
        val newHash   = md.digest

        if (!Arrays.equals(localHash, newHash)) {
          prefs.putByteArray(urlToFullHash(url), newHash)
          callback(new File(FileIO.perUserFile(urlToHash(url))))
        }

      }
    }
  }

  /** Ensures the next reload updates the GUI */
  def invalidateCache(url: URL): Unit =
    prefs.put(urlToFullHash(url), "")

  def urlToHash(url: URL): String = {
    val noTrailingSlash = url.toString.stripSuffix("/")
    noTrailingSlash.substring(noTrailingSlash.lastIndexOf('/') + 1)
  }

  private def urlToFullHash(url: URL): String =
    s"${urlToHash(url)}-md5"

}
