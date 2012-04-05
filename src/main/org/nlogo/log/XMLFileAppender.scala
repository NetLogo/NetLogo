// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.apache.log4j.FileAppender

//  This class must be public because log4j needs to be able to find it and we refer to it in the
//  configuration file
class XMLFileAppender extends FileAppender with XMLAppender {

  @throws(classOf[java.io.IOException])
  override def setFile(fileName: String, append: Boolean, bufferedIO: Boolean, bufferSize: Int) {
    super.setFile(fileName, append, bufferedIO, bufferSize)
    initializeTransformer(fileName, qw)
  }

  override def closeFile() {
    closeDocument()
    super.closeFile()
  }

  override def close() {
    closeFile()
  }

}
