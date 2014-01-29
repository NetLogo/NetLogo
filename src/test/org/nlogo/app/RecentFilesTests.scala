package org.nlogo.app

import org.scalatest.FunSuite

class RecentFilesTests extends FunSuite {

  val rf = new RecentFiles
  val paths = (1 to rf.maxEntries).map(makePath).toList
  val extraPath = makePath(rf.maxEntries + 1)
  def makePath(n: Int) = "/" + n + ".nlogo"

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
    (paths :+ extraPath).foreach(rf.add)
    assert(rf.paths.size === rf.maxEntries)
    assert(rf.paths.head === extraPath)
    assert(rf.paths.last === paths(1))
  }

  test("max entries should be respected when loading from prefs store") {
    putAndLoad((paths :+ extraPath).reverse.mkString("\n"))
    assert(rf.paths.size === rf.maxEntries)
    assert(rf.paths.head === extraPath)
    assert(rf.paths.last === paths(1))
  }

  test("distinct paths should be respected when loading from prefs store") {
    putAndLoad((paths ++ paths).mkString("\n"))
    assert(rf.paths === paths)
  }

  test("a very long path should be refused") {
    val tooLongPath = "/" + ("x" * 4096) + ".nlogo"
    putAndLoad(tooLongPath)
    assert(rf.paths.isEmpty)
  }
}
