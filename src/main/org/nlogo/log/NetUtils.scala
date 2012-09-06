package org.nlogo.log

import java.net.URL
import java.io.{BufferedInputStream, FileOutputStream, BufferedOutputStream}
import java.nio.charset.Charset

import scala.io.Source

import org.apache.http.HttpResponse
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

// Redundant with other `NetUtils` files in our codebase, but separated for the sake of simplifying dependencies
object NetUtils {

  // Bad, Bad Bizzle #1: I've littered through the `<x>Utils` objects various uses of this encoding,
  // which seems necessary for my remote logging functionality to work.  This is done
  // pending further investigation.  Ideally, these should be "UTF-8".
  // I find it amusing that I sometimes act as if I'm going to use an encoding, and then ignore it.
  // I apologize for this mess.  Search for other erroneously instances of this encoding by using
  // the search string --JAB (8/30/12)
  val DefaultByteEncoding = "ISO-8859-1"
  val DefaultReadSize = 1024

  protected def generateClient = new org.apache.http.impl.client.DefaultHttpClient

  def httpGet(dest: URL): String = readResponse(generateClient.execute(new HttpGet(dest.toURI)))

  def httpPost(postKVs: Map[String, String], dest: URL, encoding: String = DefaultByteEncoding): String = {
    import collection.JavaConverters.seqAsJavaListConverter
    val post = new HttpPost(dest.toURI)
    post.setEntity(new UrlEncodedFormEntity((postKVs map { case (key, value) => new BasicNameValuePair(key, value) } toSeq) asJava, Charset.forName("UTF-8")))
    readResponse(generateClient.execute(post))
  }

  private def readResponse(response: HttpResponse) = Source.fromInputStream(response.getEntity.getContent).mkString.trim

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

