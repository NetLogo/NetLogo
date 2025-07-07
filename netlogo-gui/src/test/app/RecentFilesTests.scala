// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.File

import org.scalatest.funsuite.AnyFunSuite

import org.nlogo.api.{ ModelType, Version }
import org.nlogo.core.NetLogoPreferences

class RecentFilesTests extends AnyFunSuite {

  val rf = new RecentFiles
  val models = (1 to rf.maxEntries).map(makePath).map(ModelEntry(_, ModelType.Normal)).toList ++
               (9 to rf.maxEntries).map(makePath).map(ModelEntry(_, ModelType.Library)).toList
  val extraModel = ModelEntry(makePath(rf.maxEntries + 1), ModelType.Normal)
  def makePath(n: Int) = new File(n.toString + (if (Version.is3D) ".nlogox3d" else ".nlogox")).getCanonicalPath()

  def putAndLoad(models: List[ModelEntry]): Unit = {
    NetLogoPreferences.put(rf.key, models.mkString("\n"))
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
    assert(NetLogoPreferences.get(rf.key, "") ===
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
    assert(NetLogoPreferences.get(rf.key, "default") === "")
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
    putAndLoad(List(ModelEntry(tooLongPath, ModelType.Normal)))
    assert(rf.models.isEmpty)
  }

  test("library version of model should replace normal version of model") {
    putAndLoad(List(ModelEntry(makePath(1), ModelType.Normal),
                    ModelEntry(makePath(2), ModelType.Normal),
                    ModelEntry(makePath(3), ModelType.Normal)))
    rf.add(ModelEntry(makePath(2), ModelType.Library))
    assert(rf.models === List(ModelEntry(makePath(2), ModelType.Library),
                              ModelEntry(makePath(1), ModelType.Normal),
                              ModelEntry(makePath(3), ModelType.Normal)))
  }

  test("normal version of model should bump library version of model but not replace") {
    putAndLoad(List(ModelEntry(makePath(1), ModelType.Normal),
                    ModelEntry(makePath(2), ModelType.Library),
                    ModelEntry(makePath(3), ModelType.Normal)))
    rf.add(ModelEntry(makePath(2), ModelType.Normal))
    assert(rf.models === List(ModelEntry(makePath(2), ModelType.Library),
                              ModelEntry(makePath(1), ModelType.Normal),
                              ModelEntry(makePath(3), ModelType.Normal)))
  }
}
