package org.nlogo.app

import org.scalatest.FunSuite

class RecentFilesTests extends FunSuite {

  val rf = new RecentFiles
  val paths = List("/a/x.nlogo", "/b/y.nlogo", "/c/z.nlogo")

  test("empty pref store should yield empty path list") {
    rf.prefs.put(rf.key, "")
    rf.loadFromPrefs()
    assert(rf.paths.size === 0)
  }

  test("paths in pref store should get loaded into class") {
    rf.prefs.put(rf.key, paths.mkString("\n"))
    rf.loadFromPrefs()
    assert(rf.paths === paths)
  }

  test("start empty, add paths one by one") {
    rf.prefs.put(rf.key, "")
    rf.loadFromPrefs()
    paths.foreach(rf.add)
    assert(rf.paths === paths.reverse)
    assert(rf.prefs.get(rf.key, "") ===
      paths.reverse.mkString("\n"))
  }

  test("adding already existing path should move it to head") {
    rf.prefs.put(rf.key, paths.mkString("\n"))
    rf.loadFromPrefs()
    rf.add(paths.head)
    assert(rf.paths === paths)
    rf.add(paths.last)
    assert(rf.paths === paths.last :: paths.init)
  }

  test("calling clear should clear paths and prefs") {
    rf.prefs.put(rf.key, paths.mkString("\n"))
    rf.loadFromPrefs()
    assert(rf.paths === paths)
    rf.clear()
    assert(rf.paths === Nil)
    assert(rf.prefs.get(rf.key, "default") === "")
  }

  test("max entries should be respected") {
    for (n <- 1 to rf.maxEntries + 1) rf.add(n.toString)
    assert(rf.paths.size === rf.maxEntries)
    assert(rf.paths.head === (rf.maxEntries + 1).toString)
    assert(rf.paths.last === "2")
  }
}
