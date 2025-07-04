// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Files

// this test is modeled after info found in:
// http://download.oracle.com/javase/tutorial/i18n/text/string.html
class TestFilePrimsUTF8 extends AbstractTestModels {

  val testDir = Files.createTempDirectory("file-prim-files").toString.replace("\\", "/")

  val code = s"""
globals [f utf-string]

to write-out
 set f (word "$testDir/utf8-file-written-by-test.txt" )
 if file-exists? f [ file-delete f ]
 file-open f
 file-write utf-string
 file-print ""
 file-close
 wait 0.05 ; help prevent test failure
end

to-report read-string [a-file]
 set f a-file
 file-open f
 set utf-string file-read
 file-close
 report utf-string
end

to-report read-line [a-file]
 set f a-file
 file-open f
 set utf-string file-read-line
 file-close
 report utf-string
end
"""


  testModel(testName="file-read", model=Model(code=code)){
    val utfStringIn = reporter("read-string " + quoted("test/file-prim-files/utf8-file.txt") ).get
    assert(utfStringIn === new String("A" + "\u00ea" + "\u00f1" + "\u00fc" + "C"))

    observer >> "set utf-string " +  quoted(utfStringIn.toString)

    observer >> "write-out"

    val utfStringReadInAfterWriting = reporter("read-string " + quoted(s"$testDir/utf8-file-written-by-test.txt") ).get

    assert(utfStringIn === utfStringReadInAfterWriting)
  }


  testModel(testName="file-read-line", model=Model(code=code)){
    val utfStringIn = reporter("read-line " + quoted("test/file-prim-files/utf8-file.txt") ).get.toString
    assert(utfStringIn === new String(" \"A" + "\u00ea" + "\u00f1" + "\u00fc" + "C\""))
  }
}
