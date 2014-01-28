package org.nlogo.app

import org.scalatest.FunSuite

class RecentFilesTests extends FunSuite {

  val rf = new RecentFiles
  val paths = List("/a/x.nlogo", "/b/y.nlogo", "/c/z.nlogo")

  def putAndLoad(s: String) {
    rf.prefs.put(rf.key, s)
    rf.loadFromPrefs()
  }

  test("empty pref store should yield empty path list") {
    putAndLoad("")
    assert(rf.paths.size === 0)
  }

  test("paths in pref store should get loaded into class") {
    putAndLoad(paths.mkString("\n"))
    assert(rf.paths === paths)
  }

  test("start empty, add paths one by one") {
    putAndLoad("")
    paths.foreach(rf.add)
    assert(rf.paths === paths.reverse)
    assert(rf.prefs.get(rf.key, "") ===
      paths.reverse.mkString("\n"))
  }

  test("adding already existing path should move it to head") {
    putAndLoad(paths.mkString("\n"))
    rf.add(paths.head)
    assert(rf.paths === paths)
    rf.add(paths.last)
    assert(rf.paths === paths.last :: paths.init)
  }

  test("calling clear should clear paths and prefs") {
    putAndLoad(paths.mkString("\n"))
    assert(rf.paths === paths)
    rf.clear()
    assert(rf.paths === Nil)
    assert(rf.prefs.get(rf.key, "default") === "")
  }

  test("max entries should be respected when adding one by one") {
    for (n <- 1 to rf.maxEntries + 1) rf.add(n.toString)
    assert(rf.paths.size === rf.maxEntries)
    assert(rf.paths.head === (rf.maxEntries + 1).toString)
    assert(rf.paths.last === "2")
  }

  test("max entries should be respected when loading from prefs store") {
    putAndLoad((rf.maxEntries + 1 to 1 by -1).mkString("\n"))
    assert(rf.paths.size === rf.maxEntries)
    assert(rf.paths.head === (rf.maxEntries + 1).toString)
    assert(rf.paths.last === "2")
  }

  test("distinct paths should be respected when loading from prefs store") {
    putAndLoad((paths ++ paths).mkString("\n"))
    assert(rf.paths === paths)
  }
  
  test("max entry length should be < max prefs store value length") {
    assert(rf.maxEntryLength < java.util.prefs.Preferences.MAX_VALUE_LENGTH)
  }
  
  test("a path with length == maxEntryLength should be accepted") {
    val longPath = "x" * rf.maxEntryLength
    putAndLoad(longPath)
    assert(rf.paths.head === longPath)
  }

  test("a path with length > maxEntryLength should be refused") {
    val tooLongPath = "x" * (rf.maxEntryLength + 1)
    putAndLoad(tooLongPath)
    assert(rf.paths.isEmpty)
  }

}
