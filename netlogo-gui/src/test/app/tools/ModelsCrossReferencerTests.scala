// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.scalatest.funsuite.AnyFunSuite

import com.typesafe.config.ConfigFactory

import org.nlogo.workspace.ModelsLibrary.{ Leaf, Node, Tree }

import ModelCrossReferencer.addReference

class ModelsCrossReferencerTests extends AnyFunSuite {
  val model = Leaf("Bar.nlogo", "foo/Bar.nlogo")
  val model2 = Leaf("Wolf.nlogo", "qux/Wolf.nlogo")

  def tree(n: String, children: Node*): Tree = Tree(n, n + "/", children.toList)
  def refTree(n: String, children: Node*): Tree = Tree(n, "xref", children.toList)

  test("adds a reference to a single model to a ModelsLibrary tree when the model and directory already exist") {
    val original = tree("", tree("baz"), tree("foo", model))
    val expected = tree("", tree("baz", model), tree("foo", model))
    assertResult(expected)(addReference(original, "foo/Bar.nlogo", "baz"))
  }
  test("adds a reference to a single model to a ModelsLibrary tree when the model exists but the directory doesn't") {
    val original = tree("", tree("foo", model))
    val expected = tree("", refTree("baz", model), tree("foo", model))
    assertResult(expected)(addReference(original, "foo/Bar.nlogo", "baz"))
  }

  test("leaves other models in the target directory intact") {
    val original = tree("", tree("foo", model), tree("qux", model2))
    val expected = tree("", tree("foo", model), tree("qux", model, model2))
    assertResult(expected)(addReference(original, "foo/Bar.nlogo", "qux"))
  }

  test("inserts multiple cross-references in sorted order") {
    val original = tree("", tree("foo", model), tree("qux", model2))
    val expected = tree("", refTree("baz", model, model2), tree("foo", model), tree("qux", model2))
    assertResult(expected)(addReference(addReference(original, "foo/Bar.nlogo", "baz"), "qux/Wolf.nlogo", "baz"))
  }

  test("does not add a reference to a ModelsLibrary tree when the model does not exist") {
    val original = tree("", tree("foo"))
    val xreffed = addReference(original, "foo/Bar.nlogo", "baz")
    assertResult(original)(xreffed)
  }

  test("adds a reference to all models in a directory to the ModelsLibrary tree when the source and destination directories exist") {
    val original = tree("", tree("bar", model2), tree("foo", model))
    val expected = tree("", tree("bar", model, model2), tree("foo", model))
    assertResult(expected)(ModelCrossReferencer.addDirectoryReference(original, "foo", "bar"))
  }

  test("does not add references to child directories in non-recursive mode") {
    val original = tree("", tree("bar", model), tree("foo", tree("qux")))
    val expected = tree("", tree("bar", model), tree("foo", tree("qux")))
    assertResult(expected)(ModelCrossReferencer.addDirectoryReference(original, "foo", "bar"))
  }

  test("adds a reference to all models in a directory to the ModelsLibrary tree when only the source directory exists") {
    val original = tree("", tree("foo", model))
    val expected = tree("", refTree("bar", model), tree("foo", model))
    assertResult(expected)(ModelCrossReferencer.addDirectoryReference(original, "foo", "bar"))
  }

  test("can adds references recursively to all models in a directory and subdirectories") {
    val original = tree("", tree("foo", tree("qux", model)))
    val expected = tree("", refTree("bar", refTree("qux", model)), tree("foo", tree("qux", model)))
    assertResult(expected)(ModelCrossReferencer.addDirectoryReferenceRecursive(original, "foo", "bar"))
  }

  test("does not add references to a ModelsLibrary tree when the source directory does not exist") {
    val original = tree("", tree("foo", tree("qux", model)))
    assertResult(original)(ModelCrossReferencer.addDirectoryReference(original, "baz", "qux"))
  }

  test("given a configuration, performs operations which conform to that configuration, logging errors") {
    val config = ConfigFactory.load("app/tools/test-cross-ref.conf")
    val modelA = Leaf("A.nlogo", "foo/qux/A.nlogo")
    val modelB = Leaf("B.nlogo", "rec/B.nlogo")
    val modelC = Leaf("C.nlogo", "rec/inner/C.nlogo")
    val original = tree("", tree("foo", model, tree("qux", modelA)), tree("rec", modelB, tree("inner", modelC)))
    val expected = tree("", refTree("baz", modelA, modelB, model, refTree("inner", modelC)), original.children(0), original.children(1))
    val actual = ModelCrossReferencer.applyConfig(original, config)
    assertResult(expected)(actual)
  }
}
