package org.nlogo.extensions.table

import org.scalatest.BeforeAndAfterAll

import java.io.File

import org.nlogo.headless.TestLanguage

object Tests {
  val testFileNames = Seq("tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles) with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    val file = new File("tmp/table")
    def deleteRec(f: File): Unit = {
      if (f.isDirectory) {
        f.listFiles().foreach(deleteRec)
      }
      f.delete()
    }
    deleteRec(file)
  }
}
