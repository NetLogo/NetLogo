// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.LibraryInfo
import org.nlogo.util.AnyFunSuiteEx

// these tests verify the behavior of the package manager's dependency resolution algorithm, as well as the related
// helper functions and data structures. (Isaac B 11/11/26)
class PackageUtilsTests extends AnyFunSuiteEx {
  test("parse full version spec") {
    assertParsedVersion("0.0.0", "0.0.0")
    assertParsedVersion("0.0.0", "0.0.0")
    assertParsedVersion("1.1.1", "1.1.1")
    assertParsedVersion("1.0.0", "1.0.0")
    assertParsedVersion("2.0.5", "2.0.5")
    assertParsedVersion("42.9999.123", "42.9999.123")
  }

  test("parse partial version spec") {
    assertParsedVersion("5", "5.0.0")
    assertParsedVersion("3.4", "3.4.0")
  }

  test("invalid version spec") {
    assertInvalidVersion("7.1.0.5")
    assertInvalidVersion("10.1.")
    assertInvalidVersion("1.")
    assertInvalidVersion(".0.1")
    assertInvalidVersion("aaaa")
    assertInvalidVersion("a.b.c")
    assertInvalidVersion(".")
    assertInvalidVersion("...")
    assertInvalidVersion("  1  .  0  .  0  ")
  }

  test("compare version spec") {
    assert(VersionSpec(0, 0, 0) < VersionSpec(0, 0, 1))
    assert(VersionSpec(0, 0, 1) < VersionSpec(0, 1, 0))
    assert(VersionSpec(1, 0, 0) < VersionSpec(1, 1, 1))
    assert(VersionSpec(1, 1000, 1000) < VersionSpec(2, 0, 0))
  }

  test("version range intersects") {
    assertIntersects("0.0.0", "0.0.0", "0.0.0", "0.0.0", true)
    assertIntersects("0.0.0", "1.0.0", "0.0.0", "1.0.0", true)
    assertIntersects("0.0.0", "0.0.0", "0.0.0", "1.0.0", true)
    assertIntersects("1.0.0", "1.0.0", "0.0.0", "1.0.0", true)
    assertIntersects("0.0.0", "1.0.0", "0.5.0", "1.5.0", true)
    assertIntersects("0.0.0", "0.0.0", "1.0.0", "1.0.0", false)
    assertIntersects("1.0.0", "1.0.0", "0.0.0", "0.0.0", false)
    assertIntersects("0.0.0", "1.0.0", "2.0.0", "3.0.0", false)
  }

  test("combine version range") {
    assertCombinedRange("0.0.0", "1.0.0", "0.5.0", "1.5.0", "0.5.0", "1.0.0")
    assertCombinedRange("0.5.0", "1.5.0", "0.0.0", "1.0.0", "0.5.0", "1.0.0")
    assertCombinedRange("0.0.0", "1.0.0", "0.5.0", "1.0.0", "0.5.0", "1.0.0")
    assertCombinedRange("0.0.0", "1.0.0", "0.0.0", "0.5.0", "0.0.0", "0.5.0")
    assertCombinedRange("0.0.0", "1.0.0", "1.0.0", "2.0.0", "1.0.0", "1.0.0")
    assertCombinedRange("1.0.0", "2.0.0", "0.0.0", "1.0.0", "1.0.0", "1.0.0")
    assertCombinedRange("0.0.0", "2.0.0", "0.5.0", "1.5.0", "0.5.0", "1.5.0")
    assertCombinedRange("0.5.0", "1.5.0", "0.0.0", "2.0.0", "0.5.0", "1.5.0")
  }

  test("package with no deps") {
    val pkg: PackageSpec = createSpec("test", "1.0.0")

    assertNewInstalls(pkg, Seq(pkg), Seq(pkg), Seq())

    assertNewInstalls(pkg, Seq(pkg), Seq(
      createSpec("test", "0.9.9"),
      pkg,
      createSpec("test", "1.0.1")
    ), Seq())

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      createSpec("test2", "1.0.0"),
      createSpec("test3", "1.0.0")
    ), Seq())
  }

  test("package with one missing dep") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq("\"dep\" == 1.0.0"))
    val dep1: PackageSpec = createSpec("dep", "0.9.9")
    val dep2: PackageSpec = createSpec("dep", "1.0.0")
    val dep3: PackageSpec = createSpec("dep", "1.0.1")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep2
    ), Seq(dep2))

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3
    ), Seq(dep2))
  }

  test("package with one existing dep") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq("\"dep\" == 1.0.0"))
    val dep1: PackageSpec = createSpec("dep", "0.9.9")
    val dep2: PackageSpec = createSpec("dep", "1.0.0")
    val dep3: PackageSpec = createSpec("dep", "1.0.1")

    assertNewInstalls(pkg, Seq(
      pkg,
      dep2
    ), Seq(
      pkg,
      dep2
    ), Seq())

    assertNewInstalls(pkg, Seq(
      pkg,
      dep2
    ), Seq(
      pkg,
      dep1,
      dep2,
      dep3
    ), Seq())
  }

  test("package with linear dep chain") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq("\"dep1\" == 1.0.0"))
    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep2\" == 1.0.0"))
    val dep2: PackageSpec = createSpec("dep2", "1.0.0", Seq("\"dep3\" == 1.0.0"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")
    val dep4: PackageSpec = createSpec("dep1", "1.0.1", Seq("\"dep2\" == 1.0.1"))
    val dep5: PackageSpec = createSpec("dep2", "1.0.1", Seq("\"dep3\" == 1.0.1"))
    val dep6: PackageSpec = createSpec("dep3", "1.0.1")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3
    ), Seq(dep1, dep2, dep3))

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3,
      dep4,
      dep5,
      dep6
    ), Seq(dep1, dep2, dep3))
  }

  test("package with divergent dep chain") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq(
      "\"dep1\" == 1.0.0",
      "\"dep2\" == 1.0.0"
    ))

    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep3\" == 1.0.0"))
    val dep2: PackageSpec = createSpec("dep2", "1.0.0", Seq("\"dep3\" == 1.0.1"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")
    val dep4: PackageSpec = createSpec("dep3", "1.0.1")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3,
      dep4
    ), Seq(dep1, dep2, dep3, dep4))
  }

  test("package with convergent dep chain") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq(
      "\"dep1\" == 1.0.0",
      "\"dep2\" == 1.0.0"
    ))

    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep3\" == 1.0.0"))
    val dep2: PackageSpec = createSpec("dep2", "1.0.0", Seq("\"dep3\" == 1.0.0"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3
    ), Seq(dep1, dep2, dep3))
  }

  test("package with overlapping dep chain") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq(
      "\"dep1\" == 1.0.0",
      "\"dep2\" == 1.0.0"
    ))

    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep3\" >= 1.0.0"))
    val dep2: PackageSpec = createSpec("dep2", "1.0.0", Seq("\"dep3\" == 1.0.1"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")
    val dep4: PackageSpec = createSpec("dep3", "1.0.1")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3,
      dep4
    ), Seq(dep1, dep2, dep4))
  }

  test("package with multiple overlapping deps") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq(
      "\"dep1\" == 1.0.0",
      "\"dep2\" == 1.0.0",
      "\"dep3\" <= 1.0.2"
    ))

    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep3\" >= 1.0.0"))
    val dep2: PackageSpec = createSpec("dep2", "1.0.0", Seq("\"dep3\" == 1.0.1"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")
    val dep4: PackageSpec = createSpec("dep3", "1.0.1")
    val dep5: PackageSpec = createSpec("dep3", "1.0.2")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3,
      dep4,
      dep5
    ), Seq(dep1, dep2, dep4))
  }

  test("package with unnecessary potential deps") {
    val pkg: PackageSpec = createSpec("test", "1.0.0", Seq(
      "\"dep1\" >= 1.0.0"
    ))

    val dep1: PackageSpec = createSpec("dep1", "1.0.0", Seq("\"dep3\" == 1.0.0"))
    val dep2: PackageSpec = createSpec("dep1", "1.0.1", Seq("\"dep3\" == 1.0.1"))
    val dep3: PackageSpec = createSpec("dep3", "1.0.0")
    val dep4: PackageSpec = createSpec("dep3", "1.0.1")

    assertNewInstalls(pkg, Seq(pkg), Seq(
      pkg,
      dep1,
      dep2,
      dep3,
      dep4
    ), Seq(dep2, dep4))
  }

  private def assertParsedVersion(input: String, expected: String): Unit = {
    assert(VersionSpec.parse(input).exists(_.toString == expected))
  }

  private def assertInvalidVersion(input: String): Unit = {
    assert(VersionSpec.parse(input).isEmpty)
  }

  private def assertIntersects(min1: String, max1: String, min2: String, max2: String, intersects: Boolean): Unit = {
    val range1 = VersionRange(VersionSpec.parse(min1).get, VersionSpec.parse(max1).get)
    val range2 = VersionRange(VersionSpec.parse(min2).get, VersionSpec.parse(max2).get)

    assert(range1.intersects(range2) == intersects)
  }

  private def assertCombinedRange(min1: String, max1: String, min2: String, max2: String, end1: String,
                                  end2: String) = {

    val range1 = VersionRange(VersionSpec.parse(min1).get, VersionSpec.parse(max1).get)
    val range2 = VersionRange(VersionSpec.parse(min2).get, VersionSpec.parse(max2).get)
    val end = VersionRange(VersionSpec.parse(end1).get, VersionSpec.parse(end2).get)

    assert(range1.combine(range2) == end)
    assert(range2.combine(range1) == end)
  }

  private def createSpec(info: LibraryInfo): PackageSpec =
    PackageUtils.createSpec(info).get

  private def createSpec(name: String, version: String, dependencies: Seq[String] = Seq()): PackageSpec =
    createSpec(LibraryInfo("", name, "", "", version, dependencies, null, false, None, None, null, false))

  private def assertNewInstalls(spec: PackageSpec, installed: Seq[PackageSpec], available: Seq[PackageSpec],
                                newInstalls: Seq[PackageSpec]): Unit = {

    val found: Seq[String] = infoStrings(PackageUtils.findDeps(spec, installed, available))
    val expected: Seq[String] = infoStrings(newInstalls)

    assert(found == expected)
  }

  private def infoStrings(infos: Iterable[PackageSpec]): Seq[String] = {
    infos.toSeq.sortWith { (a, b) =>
      if (a.name == b.name) {
        a.version < b.version
      } else {
        a.name < b.name
      }
    }.map(info => s"${info.name}-${info.version}")
  }
}
