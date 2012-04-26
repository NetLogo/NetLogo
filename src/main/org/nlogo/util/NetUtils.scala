package org.nlogo.util

import java.io.{InputStreamReader, BufferedInputStream, FileOutputStream, BufferedOutputStream}
import java.net.{HttpURLConnection, URLEncoder, URL}

object NetUtils {

  val DefaultByteEncoding = "ISO-8859-1"
  val DefaultReadSize = 1024

  // You are strongly encouraged to keep POST data under 30K characters, lest `SocketExceptions` start getting thrown --JAB (4/26/12)
  def httpPost(postKVs: Map[String, String], dest: URL, encoding: String = DefaultByteEncoding): String = {

    val data = postKVs.toList map {
      case (key, value) => "%s=%s".format(List(key, value) map (URLEncoder.encode(_, encoding)): _*)
    } mkString ("&")

    val conn = new URL(dest.toString + "?" + data).openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)

    val in = new InputStreamReader(conn.getInputStream)
    val buff = new Array[Char](DefaultReadSize)
    in.read(buff)
    in.close()
    conn.disconnect()  // Disconnect to avoid massive memory leaks on repeated connections
    buff.mkString.trim

  }

  def httpGet(dest: URL): String = {

    val conn = dest.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")

    val in = new InputStreamReader(conn.getInputStream)
    val buff = new Array[Char](DefaultReadSize * 4)
    in.read(buff)
    buff.mkString.trim

  }

  /**
   * @param from  The path of the file to download
   * @param to    The directory into which to download the file
   * @return      The full path of the downloaded file
   */
  def downloadFile(from: String, to: String): String = {

    val readSize = DefaultReadSize
    val fileName = from.reverse takeWhile (_ != '/') reverse
    val outPath  = to + System.getProperty("file.separator") + fileName

    val inStream  = new BufferedInputStream(new URL(from).openStream())
    val outStream = new FileOutputStream(outPath)
    val outBuffer = new BufferedOutputStream(outStream, readSize)
    val data = new Array[Byte](readSize)
    var x = inStream.read(data, 0, readSize)

    while (x >= 0) {
      outBuffer.write(data, 0, x)
      x = inStream.read(data, 0, readSize)
    }

    outBuffer.close()
    inStream.close()
    outStream.close()

    outPath

  }

}
