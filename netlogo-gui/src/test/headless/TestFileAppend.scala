// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Files

class TestFileAppend extends AbstractTestModels {
  private val temp = Files.createTempDirectory(null)
  private val path = temp.resolve("test.txt")

  private val code = s"""|to test
                         |  file-open "${path.toString.replace("\\", "/")}"
                         |  file-print "test1"
                         |  file-print "test2"
                         |  file-print "test3"
                         |  file-close
                         |end
                         |""".stripMargin

  temp.toFile.deleteOnExit()

  // ensure that file-print appends to a file instead of overwriting the existing contents (Isaac B 9/29/25)
  testModel("file-print", Model(code = code)) {
    command("test")

    assert(Files.readString(path).split("\r?\n").sameElements(Seq("test1", "test2", "test3")))
  }
}
