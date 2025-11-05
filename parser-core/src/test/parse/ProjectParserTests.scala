// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.funsuite.AnyFunSuite

class ProjectParserTests extends AnyFunSuite {
  test("basic parsing") {
    val input = """
      |name = "foo"
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    val expectedDependencies =
      List(
        Dependency("libfoo", None, None),
        Dependency("libbar", Some(VersionBound.Exclusive(VersionNumber(List(1, 0)))), None),
        Dependency(
          "libbaz",
          Some(VersionBound.Inclusive(VersionNumber(List(1, 2, 3)))),
          Some(VersionBound.Exclusive(VersionNumber(List(4, 5, 6))))
        )
      )
    val expectedSourceFiles = List("foo.nls", "bar.nls", "baz.nls")
    val expected = Some(Project("foo", VersionNumber(List(1, 0, 0)), expectedDependencies, expectedSourceFiles))

    assertResult(expected)(result)
  }

  test("parser accepts file with fields in a different order") {
    val input = """
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |name = "foo"
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    val expectedDependencies =
      List(
        Dependency("libfoo", None, None),
        Dependency("libbar", Some(VersionBound.Exclusive(VersionNumber(List(1, 0)))), None),
        Dependency(
          "libbaz",
          Some(VersionBound.Inclusive(VersionNumber(List(1, 2, 3)))),
          Some(VersionBound.Exclusive(VersionNumber(List(4, 5, 6))))
        )
      )
    val expectedSourceFiles = List("foo.nls", "bar.nls", "baz.nls")
    val expected = Some(Project("foo", VersionNumber(List(1, 0, 0)), expectedDependencies, expectedSourceFiles))

    assertResult(expected)(result)
  }

  test("parser rejects file with duplicate fields") {
    val input = """
      |name = "foo"
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      |version = 2.0.0
      """.stripMargin

    val e = intercept[Exception] {
      ProjectCombinators.parse(input)
    }

    assertResult("Duplicate fields in project file")(e.getMessage.takeWhile(_ != ','))
  }

  test("parser accepts file without optional fields") {
    val input = """
      |name = "foo"
      |version = 1.0.0
      """.stripMargin
    val result = ProjectCombinators.parse(input)
    val expected = Some(Project("foo", VersionNumber(List(1, 0, 0)), List(), List()))

    assertResult(expected)(result)
  }

  test("parser rejects file missing a required field") {
    val input = """
      |name = "foo"
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    assertResult(None)(result)
  }

  test("parser rejects invalid version number") {
    val input = """
      |name = "foo"
      |version = 1.0.0.a
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    assertResult(None)(result)
  }

  test("parser rejects unquoted strings") {
    val input = """
      |name = foo
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    assertResult(None)(result)
  }

  test("parser rejects strings with unmatched quotes") {
    val input = """
      |name = "foo
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" >= 1.2.3 && < 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    assertResult(None)(result)
  }

  test("parser rejects dependency with flipped bounds") {
    val input = """
      |name = "foo"
      |version = 1.0.0
      |dependencies = "libfoo", "libbar" > 1.0, "libbaz" < 1.2.3 && >= 4.5.6
      |source-files = "foo.nls", "bar.nls", "baz.nls"
      """.stripMargin
    val result = ProjectCombinators.parse(input)

    assertResult(None)(result)
  }

  test("version number comparisons") {
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) < VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 2, 3)) <= VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) > VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 2, 3)) >= VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) != VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 2, 3)) == VersionNumber(Seq(1, 2, 3)))

    assertResult(true)(VersionNumber(Seq(1, 2, 3)) < VersionNumber(Seq(1, 3, 3)))
    assertResult(true)(VersionNumber(Seq(1, 2, 3)) <= VersionNumber(Seq(1, 3, 3)))
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) > VersionNumber(Seq(1, 3, 3)))
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) >= VersionNumber(Seq(1, 3, 3)))
    assertResult(true)(VersionNumber(Seq(1, 2, 3)) != VersionNumber(Seq(1, 3, 3)))
    assertResult(false)(VersionNumber(Seq(1, 2, 3)) == VersionNumber(Seq(1, 3, 3)))

    assertResult(false)(VersionNumber(Seq(1, 3, 3)) < VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1, 3, 3)) <= VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 3, 3)) > VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 3, 3)) >= VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1, 3, 3)) != VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1, 3, 3)) == VersionNumber(Seq(1, 2, 3)))

    assertResult(true)(VersionNumber(Seq(1)) < VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1)) <= VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1)) > VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1)) >= VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq(1)) != VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq(1)) == VersionNumber(Seq(1, 2, 3)))

    assertResult(true)(VersionNumber(Seq()) < VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq()) <= VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq()) > VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq()) >= VersionNumber(Seq(1, 2, 3)))
    assertResult(true)(VersionNumber(Seq()) != VersionNumber(Seq(1, 2, 3)))
    assertResult(false)(VersionNumber(Seq()) == VersionNumber(Seq(1, 2, 3)))
  }

}
