// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import org.nlogo.util.AnyFunSuiteEx

class MenuModelTests extends AnyFunSuiteEx {
  trait Helper {
    val model = new MenuModel[String, Int]

    def assertLeafValueAt(value: String, i: Int): Unit = {
      model.children(i) match {
        case model.Leaf(v, _) => assert(v == value)
        case _ => fail(s"expected leaf at $i, but found branch")
      }
    }
    def assertBranchAt(b: MenuModel[String, Int], i: Int): Unit = {
      model.children(i) match {
        case model.Branch(m, _, _) => assert(m == b)
        case _ => fail(s"expected branch at $i, but found leaf")
      }
    }
    def assertBranchValueAt(branchValue: Int, i: Int): Unit = {
      model.children(i) match {
        case model.Branch(_, v, _) => assert(v == branchValue)
        case _ => fail(s"expected branch at $i, but found leaf")
      }
    }
  }

  test("an empty menu model has no items") { new Helper {
    assert(model.leaves.isEmpty)
  } }

  test("when a leaf is inserted into the model, it has a single item") { new Helper {
    model.insertLeaf("abc")
    assert(model.leaves.nonEmpty)
    assertLeafValueAt("abc", 0)
  } }

  test("orders leaves with the provided ordering") { new Helper {
    model.insertLeaf("def")
    model.insertLeaf("abc")
    assertLeafValueAt("abc", 0)
    assertLeafValueAt("def", 1)
  } }

  test("can have branches inserted") { new Helper {
    val b = model.createBranch(0, "")
    b.insertLeaf("abc")
    assertBranchAt(b, 0)
  } }

  test("lists branches and leaves as children, with leaves listed first") { new Helper {
    val b = model.createBranch(0, "")
    b.insertLeaf("abc")
    model.insertLeaf("def")
    assertLeafValueAt("def", 0)
    assertBranchAt(b, 1)
  } }

  test("groups like items") { new Helper {
    model.insertLeaf("ghi", "group2")
    model.insertLeaf("def", "group1")
    model.insertLeaf("abc", "group1")
    assertLeafValueAt("ghi", 0)
    assertLeafValueAt("abc", 1)
    assertLeafValueAt("def", 2)
  } }

  test("allows branches to be placed in groups") { new Helper {
    model.insertLeaf("abc", "group1")
    model.insertLeaf("ghi", "group2")
    val b = model.createBranch(0, "group1")
    assertBranchAt(b, 1)
  } }

  test("allows branch ordering") { new Helper {
    val b1 = model.createBranch(3, "a")
    val b2 = model.createBranch(0, "a")
    assertBranchAt(b2, 0)
    assertBranchAt(b1, 1)
  } }

  test("permits removal of elements") { new Helper {
    model.insertLeaf("abc", "group1")
    model.insertLeaf("ghi", "group2")
    model.insertLeaf("def", "group2")
    model.removeElement("def")
    assert(model.children.length == 2)
    assertLeafValueAt("ghi", 1)
  } }

  test("creating a branch with the same key in the same group returns the existing branch") { new Helper {
    val b1 = model.createBranch(0, "abc")
    val b2 = model.createBranch(0, "abc")
    assert(b1 == b2)
  } }

  test("removal removes nested children") { new Helper {
    val b = model.createBranch(0, "a")
    b.insertLeaf("abc", "group1")
    b.insertLeaf("def", "group2")
    model.removeElement("def")
    assert(b.children.length == 1)
  } }

  test("removal of a child doesn't affect other branches") { new Helper {
    val b1 = model.createBranch(0, "a")
    b1.insertLeaf("abc", "group1")
    val b2 = model.createBranch(1, "a")
    b2.insertLeaf("def", "group2")
    model.removeElement("abc")
    assert(b1.children.length == 0)
    assert(b2.children.length == 1)
    assertBranchAt(b2, 0)
  } }

  test("optionally initializes with a list giving the order of groups") { new Helper {
    override val model = new MenuModel[String, Int](Seq("group1", "group2"))
    model.insertLeaf("abc", "group2")
    model.insertLeaf("def", "group1")
    assertLeafValueAt("def", 0)
    assertLeafValueAt("abc", 1)
  } }
}
