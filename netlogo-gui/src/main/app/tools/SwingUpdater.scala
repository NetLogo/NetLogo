// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.{ File, IOException }
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.security.{ DigestInputStream, MessageDigest }
import java.util.Arrays
import java.util.prefs.{ Preferences => JPreferences }

import org.nlogo.api.{ Exceptions, FileIO }
import org.nlogo.swing.{ ProgressListener, SwingWorker }

/** SwingUpdater can update GUI based on a remote resource.
 *
 *  This class keeps a local copy of the file. Whenever the file's hash changes
 *  the GUI is updated with the new file.
 *
 *  URLs for resources used with this class must be unique.
 */
object SwingUpdater {

  private val prefs = JPreferences.userNodeForPackage(getClass)

  private def urlToHash(url: URL): String = {
    val noTrailingSlash = url.toString.stripSuffix("/")
    noTrailingSlash.substring(noTrailingSlash.lastIndexOf('/') + 1)
  }

  private def urlToFullHash(url: URL): String = s"${urlToHash(url)}-md5"

  /** Ensures the next reload updates the GUI */
  def invalidateCache(url: URL) = prefs.put(urlToFullHash(url), "")

  /** Downloads the URL and update the GUI if the hash is different */
  def reload(progressListener: ProgressListener)(url: URL, updateGUI: File => Unit) = {

    progressListener.start()

    (new SwingWorker[Any, Any] {

      private var changed = false

      override def doInBackground(): Unit = Exceptions.ignoring(classOf[IOException]) {

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
          changed = true
        }

      }

      override def onComplete(): Unit = {
        if (changed)
          updateGUI(new File(FileIO.perUserFile(urlToHash(url))))
        progressListener.finish()
      }

    }).execute()

  }

}
