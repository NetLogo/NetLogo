// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import java.awt.Color
import java.lang.reflect.Method

import org.nlogo.core.I18N
import org.nlogo.util.AnyFunSuiteEx

import scala.util.Try

class TestThemeKeys extends AnyFunSuiteEx {
  test("All permanent themes contain the same keys") {
    val themes = InterfaceColors.getPermanentThemes

    if (themes.tail.exists(_.colors.keys != themes.head.colors.keys))
      fail()
  }

  test("All permanent themes contain the keys referenced in InterfaceColors methods") {
    val methods: Array[Method] = InterfaceColors.getClass.getMethods.filter(_.getReturnType == classOf[Color])

    val failures: Array[String] = InterfaceColors.getPermanentThemes.flatMap { theme =>
      InterfaceColors.setTheme(theme)

      val failed: Array[String] = methods.flatMap { method =>
        Try(method.invoke(InterfaceColors)).failed.toOption.map(_ => method.getName)
      }

      if (failed.nonEmpty) {
        Some(s"Theme \"${theme.name}\" is missing keys referenced in these methods:\n    ${failed.mkString("\n    ")}")
      } else {
        None
      }
    }

    InterfaceColors.setTheme(InterfaceColors.LightTheme)

    if (failures.nonEmpty)
      fail(failures.mkString("\n"))
  }

  test("All keys in permanent themes are defined in EditableTheme") {
    new EditableThemeTester
  }

  test("All keys in permanent themes are defined in I18N") {
    val defined: Set[String] = I18N.gui.keys(I18N.gui.defaultLocale)
    val missing: Iterable[String] = InterfaceColors.LightTheme.colors.keys
                                      .filter(key => !defined.contains(s"menu.tools.themeEditor.$key"))

    if (missing.nonEmpty)
      fail(s"Keys referenced in the following methods are not defined in I18N:\n    ${missing.mkString("\n    ")}")
  }

  private class EditableThemeTester extends EditableTheme(InterfaceColors.LightTheme) {
    val defined: Seq[String] = colorGroups.flatMap(_._2.map(_.key))
    val missing: Iterable[String] = InterfaceColors.LightTheme.colors.keys.filterNot(defined.contains)

    if (missing.nonEmpty)
      fail(s"The following keys are not defined in EditableTheme:\n    ${missing.mkString("\n    ")}")
  }
}
