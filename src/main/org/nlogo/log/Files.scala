// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import org.nlogo.api.FileIO.file2String
import org.nlogo.util.Exceptions.ignoring

object Files {

  def deleteSessionFiles(path: String) {
    val directory = new java.io.File(path)
    if (directory.isDirectory) {
      val files = directory.list(
        new java.io.FilenameFilter {
          override def accept(dir: java.io.File, name: String) =
              name.startsWith("logfile_") && name.endsWith(".xml")})
      for(file <- files)
        (new java.io.File(path + System.getProperty("file.separator") + file))
          .delete()
    }
  }

  def zipSessionFiles(path: String, filename: String) {
    val directory = new java.io.File(path)
    if (directory.isDirectory) {
      val files = directory.list(
        new java.io.FilenameFilter {
          override def accept(dir: java.io.File, name: String) =
              name.startsWith("logfile_") && name.endsWith(".xml")})
      if (files.nonEmpty) {
        val out = new ZipOutputStream(new FileOutputStream(filename))
        for(file <- files) {
          // IOException probably shouldn't ever happen but in case it does just skip the file and
          // move on. ev 3/14/07
          ignoring(classOf[java.io.IOException]) {
            val fullPath = path + System.getProperty("file.separator") + file
            val data = file2String(fullPath).getBytes
            out.putNextEntry(new java.util.zip.ZipEntry(file))
            out.write(data, 0, data.length)
            out.closeEntry()
          }
        }
        out.close()
      }
    }
  }

}
