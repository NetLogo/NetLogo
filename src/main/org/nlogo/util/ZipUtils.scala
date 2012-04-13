package org.nlogo.util

import collection.mutable.ArrayBuffer
import java.util.zip.{GZIPInputStream, ZipEntry, ZipFile}
import java.io.ByteArrayInputStream

object ZipUtils {

  val DefaultByteEncoding = "ISO-8859-1"

  def gzip(data: String, encoding: String = DefaultByteEncoding): Array[Byte] = {
    val inner = new java.io.ByteArrayOutputStream()
    val outer = new java.util.zip.GZIPOutputStream(inner)
    outer.write(data.getBytes(encoding))
    outer.close()
    inner.toByteArray
  }

  def gzipAsString(data: String, encoding: String = DefaultByteEncoding): String = {
    new String(gzip(data, encoding), encoding)
  }

  def unGzip(data: Array[Byte]): Option[String] = {
    try {

      val in = new GZIPInputStream(new ByteArrayInputStream(data))
      val buffer = new ArrayBuffer[Byte]

      while (in.available() > 0)
        buffer.append(in.read().toByte)

      in.close()
      Some(buffer.map(_.toChar).mkString.reverse dropWhile (_.toByte < 0) reverse) // Make string and trim off any nonsense at end

    }
    catch {
      case _ => None
    }
  }

  def unGzipFromString(data: String, encoding: String = DefaultByteEncoding): Option[String] = {
    unGzip(data.getBytes(encoding))
  }

  def extractFilesFromJar(jarpath: String, dest: String, entryFilter: ZipEntry => Boolean = (_ => true)): List[java.io.File] = {
    
    import collection.JavaConverters.enumerationAsScalaIteratorConverter
    
    val zipFile = new ZipFile(jarpath)
    val entries = Option(zipFile.entries) map (_.asScala filter (entryFilter)) getOrElse (Iterator[ZipEntry]())
    
    // Sort by name size so we always get directories created before trying to write their children
    entries.toList sortBy (_.getName.size) foreach (extractFileFromZip(dest, _, zipFile))
    new java.io.File(dest).listFiles.toList
    
  }

  def extractFileFromZip(dest: String, entry: ZipEntry, zipFile: ZipFile) {

    val target = new java.io.File(dest + entry.getName)

    if (entry.isDirectory)
      target.mkdir
    else {

      val inStream = zipFile.getInputStream(entry)
      val outStream = new java.io.FileOutputStream(target)
      val buffer = new ArrayBuffer[Byte]()

      while (inStream.available > 0)
        buffer.append(inStream.read().toByte)

      outStream.write(buffer.toArray)

      outStream.close()
      inStream.close()

    }

  }

}
