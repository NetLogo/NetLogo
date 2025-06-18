// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ File, InputStream }
import java.lang.Boolean
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.security.{ DigestInputStream, MessageDigest }
import java.util.Arrays

import org.nlogo.core.NetLogoPreferences

import scala.concurrent.{ ExecutionContext, Future, Promise }

trait InfoDownloader {

  def prefsKey: String

  def enabled: Boolean =
    !Boolean.getBoolean(s"netlogo.$prefsKey.disabled")

  /** Downloads the URL and update the GUI if the hash is different */
  def apply(url: URL): Future[Option[(File, Boolean)]] = {

    import ExecutionContext.Implicits.global

    if (enabled) {

      val promise = Promise[InputStream]()

      Future {

        val conn = url.openConnection.asInstanceOf[HttpURLConnection]

        // The `getResponseCode` thing actually blocks until the HTTP request resolves.
        // That's largely okay, since we're on another thread (in a `Future`).
        // This fixes problems for a small number of users, due to networking issues.
        // See GitHub issue #1794 for more. --Jason B. (4/23/25)
        if (conn.getResponseCode == 200) {
          promise.success(conn.getInputStream)
        }

      }

      promise.future.map {

        inputStream =>

          val md     = MessageDigest.getInstance("MD5")
          val digest = new DigestInputStream(inputStream, md)

          Files.copy(digest, Paths.get(FileIO.perUserFile(urlToHash(url))), StandardCopyOption.REPLACE_EXISTING)

          val localHash = NetLogoPreferences.getByteArray(urlToFullHash(url), null)
          val newHash   = md.digest
          val file      = new File(FileIO.perUserFile(urlToHash(url)))
          val isWriting = !Arrays.equals(localHash, newHash)

          if (isWriting) {
            NetLogoPreferences.putByteArray(urlToFullHash(url), newHash)
          }

          Option((file, isWriting))

      }


    } else {
      Future(None)
    }

  }

  /** Ensures the next reload updates the GUI */
  def invalidateCache(url: URL): Unit =
    NetLogoPreferences.put(urlToFullHash(url), "")

  def urlToHash(url: URL): String = {
    val noTrailingSlash = url.toString.stripSuffix("/")
    noTrailingSlash.substring(noTrailingSlash.lastIndexOf('/') + 1)
  }

  private def urlToFullHash(url: URL): String =
    s"${urlToHash(url)}-md5"

}
