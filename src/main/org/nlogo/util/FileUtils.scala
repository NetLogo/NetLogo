package org.nlogo.util

object FileUtils {

  def deleteDir(rootFile: java.io.File) {
    Option(rootFile.listFiles) foreach (_ foreach deleteDir)
    rootFile.delete()
  }

  def createDirectoryAnew(path: String) {

    val destFile = new java.io.File(path)

    if (destFile.exists)
      deleteDir(destFile)

    destFile.mkdir

  }

}
