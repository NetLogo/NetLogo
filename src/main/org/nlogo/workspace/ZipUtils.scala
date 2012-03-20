package org.nlogo.workspace

import collection.mutable.ListBuffer
import java.util.zip.{ZipEntry, ZipFile}

object ZipUtils {

  def extractFilesFromJar(jarpath: String, dest: String) {

    val zipFile = new ZipFile(jarpath)
    val entries = zipFile.entries
    val buffer = new ListBuffer[ZipEntry]()

    while (entries.hasMoreElements)
      buffer += entries.nextElement()

    // Sort by name size so we always get directories created before trying to write their children
    buffer.toList sortBy (_.getName.size) foreach (extractFileFromZip(dest, _, zipFile))

  }

  def extractFileFromZip(dest: String, entry: ZipEntry, zipFile: ZipFile) {

    val target = new java.io.File(dest + entry.getName)

    if (entry.isDirectory)
      target.mkdir
    else {

      val inStream = zipFile.getInputStream(entry)
      val outStream = new java.io.FileOutputStream(target)

      while (inStream.available > 0)
        outStream.write(inStream.read())

      outStream.close()
      inStream.close()

    }

  }

}
