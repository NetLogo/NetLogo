// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

// this test is modeled after info found in:
// http://download.oracle.com/javase/tutorial/i18n/text/string.html

class TestFilePrimsUTF8 extends FixtureSuite {

  val code = """|globals [f utf-string]
                |
                |to write-out
                | __mkdir "/tmp"
                | set f (word "test/file-prim-files/utf8-file-written-by-test.txt" )
                | if file-exists? f [ file-delete f ]
                | file-open f
                | file-write utf-string
                | file-print ""
                | file-close
                |end
                |
                |to-report read-string [a-file]
                | set f (word "test/file-prim-files/" a-file)
                | file-open f
                | set utf-string file-read
                | file-close
                | report utf-string
                |end
                |
                |to-report read-line [a-file]
                | set f (word "test/file-prim-files/" a-file)
                | file-open f
                | set utf-string file-read-line
                | file-close
                | report utf-string
                |end
    """.stripMargin

  val expected = "A" + "\u00ea" + "\u00f1" + "\u00fc" + "C"

  test("file-read") { implicit fixture =>
    import fixture._
    declare(code)
    testReporter("""read-string "utf8-file.txt"""", '"' + expected + '"')
    testCommand(s"""set utf-string "$expected"""")
    testCommand("write-out")
    testReporter("""read-string "utf8-file-written-by-test.txt"""",
      '"' + expected + '"')
  }

  test("file-read-line") { implicit fixture =>
    import fixture._
    declare(code)
    testReporter("""read-line "utf8-file.txt"""",
      "\" \\\"" + expected + "\\\"\"")
  }
}
