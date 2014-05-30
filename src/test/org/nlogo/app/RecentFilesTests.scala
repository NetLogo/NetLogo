package org.nlogo.app

import org.scalatest.FunSuite

import org.nlogo.api

class RecentFilesTests extends FunSuite {

  val rf = new RecentFiles
  val models = (1 to rf.maxEntries).map(makePath).map(ModelEntry(_, api.ModelType.Normal)).toList ++
               (9 to rf.maxEntries).map(makePath).map(ModelEntry(_, api.ModelType.Library)).toList
  val extraModel = ModelEntry(makePath(rf.maxEntries + 1), api.ModelType.Normal)
  def makePath(n: Int) = "/" + n + ".nlogo"

  def putAndLoad(models: List[ModelEntry]) {
    rf.prefs.put(rf.key, models.mkString("\n"))
    rf.loadFromPrefs()
  }

  test("empty pref store should yield empty path list") {
    putAndLoad(List())
    assert(rf.models.size === 0)
  }

  test("models in pref store should get loaded into class") {
    putAndLoad(models)
    assert(rf.models === models)
  }

  test("start empty, add models one by one") {
    putAndLoad(List())
    models.foreach(rf.add)
    assert(rf.models === models.reverse)
    assert(rf.prefs.get(rf.key, "") ===
      models.reverse.mkString("\n"))
  }

  test("adding already existing path should move it to head") {
    putAndLoad(models)
    rf.add(models.head)
    assert(rf.models === models)
    rf.add(models.last)
    assert(rf.models === models.last :: models.init)
  }

  test("calling clear should clear models and prefs") {
    putAndLoad(models)
    assert(rf.models === models)
    rf.clear()
    assert(rf.models === Nil)
    assert(rf.prefs.get(rf.key, "default") === "")
  }

  test("max entries should be respected when adding one by one") {
    (models :+ extraModel).foreach(rf.add)
    assert(rf.models.size === rf.maxEntries)
    assert(rf.models.head === extraModel)
    assert(rf.models.last === models(1))
  }

  test("max entries should be respected when loading from prefs store") {
    putAndLoad((models :+ extraModel).reverse)
    assert(rf.models.size === rf.maxEntries)
    assert(rf.models.head === extraModel)
    assert(rf.models.last === models(1))
  }

  test("distinct models should be respected when loading from prefs store") {
    putAndLoad((models ++ models))
    assert(rf.models === models)
  }

  test("a very long path should be refused") {
    val tooLongPath = "/" + ("x" * 4096) + ".nlogo"
    putAndLoad(List(ModelEntry(tooLongPath, api.ModelType.Normal)))
    assert(rf.models.isEmpty)
  }
}
