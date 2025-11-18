// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.nio.file.{ Files, Paths }

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FileIOTests extends AnyFunSuite {
  test("getResourceLines") {
    val expected = "NetLogo author: Uri Wilensky"
    assert(FileIO.getResourceAsString("/system/about.txt").take(expected.size) ===
      expected)
  }

  test("locateFile finds a file with an absolute path") {
    val tempFile = Files.createTempFile("filetest", ".tmp")
    assertResult(Some(tempFile))(FileIO.resolvePath(tempFile.toAbsolutePath.toString))
  }

  test("locateFile tries to resolve a file relative to the peerFile path, if provided") {
    val tempFile = Files.createTempFile("filetest", ".tmp")
    val peerFile = Files.createTempFile("filetestpeer", ".tmp")
    assertResult(Some(tempFile))(FileIO.resolvePath(tempFile.getName(tempFile.getNameCount - 1).toString, peerFile))
  }

  test("if locateFile can't resolve relative to peerFile, resolves relative to user.home system property") {
    val fileInHome = Paths.get(System.getProperty("user.home"), "foo.txt")
    assertResult(Some(fileInHome))(FileIO.resolvePath("foo.txt"))
  }

  test("locateFile returns absolute paths") {
    val resolved = FileIO.resolvePath("foo.txt", Paths.get("."))
    assertResult(Some(Paths.get(".").toAbsolutePath.getParent.resolve("foo.txt")))(resolved)
  }

  test("resolvePath handles path options") {
    val resolved = FileIO.resolvePath("foo.txt", Some(Paths.get(".")))
    assertResult(Some(Paths.get(".").toAbsolutePath.getParent.resolve("foo.txt")))(resolved)
  }
}

class FileIOTests2 extends AnyPropSpec with ScalaCheckPropertyChecks {

  import org.scalacheck.Gen
  import org.scalacheck.Arbitrary.arbitrary

  property("reader2String is inverse of StringReader") {
    forAll(arbitrary[String], Gen.chooseNum(1, 4096))(
      (ns, bufferSize) =>
        assertResult(ns)(
          FileIO.reader2String(new java.io.StringReader(ns), bufferSize)))}
}
