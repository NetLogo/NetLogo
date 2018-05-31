// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.io.File
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.security.{ DigestInputStream, MessageDigest }
import java.util.Arrays
import java.util.concurrent.ExecutionException
import java.util.prefs.Preferences
import javax.swing.SwingWorker

object SwingUpdater {
  private val prefs = Preferences.userNodeForPackage(getClass)
}

/** SwingUpdater can update GUI based on a remote resource.
 *
 *  This class keeps a local copy of the file. Whenever the file's hash changes
 *  the GUI is updated with the new file.
 *
 *  Basenames for resources used with this class must be unique.
 */
class SwingUpdater(url: URL, updateGUI: File => Unit) {
  import SwingUpdater._

  private val basename = {
    val noTrailingSlash = url.toString.stripSuffix("/")
    noTrailingSlash.substring(noTrailingSlash.lastIndexOf('/') + 1)
  }
  private val hashKey = basename + "-md5"

  /** Downloads the URL and update the GUI if the hash is different */
  def reload() = new Worker().execute()

  /** Ensures the next reload updates the GUI */
  def invalidateCache() = prefs.put(hashKey, "")

  private class Worker extends SwingWorker[Any, Any] {
    private var changed = false

    override def doInBackground(): Unit = {
      val md = MessageDigest.getInstance("MD5")
      val conn = url.openConnection.asInstanceOf[HttpURLConnection]
      if (conn.getResponseCode == 200) {
        val response = new DigestInputStream(conn.getInputStream, md)
        Files.copy(response, Paths.get(basename), StandardCopyOption.REPLACE_EXISTING)
      }
      val localHash = prefs.getByteArray(hashKey, null)
      val newHash = md.digest
      if (!Arrays.equals(localHash, newHash)) {
        prefs.putByteArray(hashKey, newHash)
        changed = true
      }
    }

    override def done(): Unit = {
      try get() catch { // propagate any exception produced on `doInBackground`
        case ex: ExecutionException => throw ex.getCause
      }
      if (changed) updateGUI(new File(basename))
    }
  }
}
