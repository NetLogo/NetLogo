package org.nlogo.workspace

import java.net.URL
import java.io.{BufferedOutputStream, FileOutputStream, BufferedInputStream}

object NetUtils {

  def downloadFile(webPath: String, dest: String): String = {

    val ReadSize = 1024
    val fileName = webPath.reverse takeWhile (_ != '/') reverse
    val outPath = dest + System.getProperty("file.separator") + fileName

    val inStream = new BufferedInputStream(new URL(webPath).openStream())
    val outStream = new FileOutputStream(outPath)
    val outBuffer = new BufferedOutputStream(outStream, ReadSize)
    val data = new Array[Byte](ReadSize)
    var x = inStream.read(data, 0, ReadSize)

    while (x >= 0) {
      outBuffer.write(data, 0, x)
      x = inStream.read(data, 0, ReadSize)
    }

    outBuffer.close()
    inStream.close()
    outStream.close()

    outPath

  }

}
