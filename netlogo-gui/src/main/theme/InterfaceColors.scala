// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import com.jthemedetecor.OsThemeDetector

import java.awt.Color
import java.io.PrintWriter
import java.net.URL
import java.nio.file.{ Files, Path, Paths, StandardCopyOption }

import org.nlogo.core.{ ColorizerTheme, I18N, NetLogoPreferences, TokenType }

import scala.io.Source
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Try

object InterfaceColors {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("menu.tools.themesManager")

  val Transparent = new Color(0, 0, 0, 0)

  val ClassicTheme: ColorTheme =
    loadTheme(I18N.gui("classic"), "classic", getClass.getResource("/themes/Classic.theme"), true).get

  val LightTheme: ColorTheme =
    loadTheme(I18N.gui("light"), "light", getClass.getResource("/themes/Light.theme"), true).get

  val DarkTheme: ColorTheme =
    loadTheme(I18N.gui("dark"), "dark", getClass.getResource("/themes/Dark.theme"), true).get

  private val defaultTheme: String = {
    if (OsThemeDetector.getDetector.isDark) {
      DarkTheme.prefName
    } else {
      LightTheme.prefName
    }
  }

  private val customRoot: Path = Paths.get(System.getProperty("user.home"), ".nlogo", "themes")

  private var customThemes = Map[String, ColorTheme]()

  private var theme: ColorTheme = LightTheme

  loadCustomThemes()

  def setTheme(theme: ColorTheme): Unit = {
    this.theme = theme
  }

  def getTheme: ColorTheme =
    theme

  def prefsTheme: ColorTheme = {
    NetLogoPreferences.get("customColorTheme", NetLogoPreferences.get("colorTheme", defaultTheme)) match {
      case ClassicTheme.prefName => ClassicTheme
      case LightTheme.prefName => LightTheme
      case DarkTheme.prefName => DarkTheme
      case name =>
        customThemes.get(name).getOrElse {
          if (defaultTheme == LightTheme.prefName) {
            LightTheme
          } else {
            DarkTheme
          }
        }
    }
  }

  def getPermanentThemes: Array[ColorTheme] =
    Array(ClassicTheme, LightTheme, DarkTheme)

  def getCustomThemes: Array[ColorTheme] =
    customThemes.values.toArray

  def themeExists(name: String): Boolean = {
    (getPermanentThemes ++ getCustomThemes).exists { theme =>
      theme.name == name || theme.prefName == name
    }
  }

  def importTheme(path: Path): Boolean = {
    val dest = customRoot.resolve(path.getFileName)

    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING)

    loadCustomThemes()

    if (getCustomThemes.exists(_.name == path.getFileName.toString.stripSuffix(".theme").trim)) {
      true
    } else {
      Files.delete(dest)

      false
    }
  }

  private def loadCustomThemes(): Unit = {
    customThemes = {
      if (Files.exists(customRoot) && Files.isDirectory(customRoot)) {
        Files.list(customRoot).iterator.asScala.foldLeft(Set[(String, ColorTheme)]()) {
          case (themes, path) =>
            val name = path.getFileName.toString.stripSuffix(".theme").trim

            if (reservedName(name) || themes.exists(_._1 == name)) {
              themes
            } else {
              themes ++ loadTheme(name, name, path.toUri.toURL, false).map((name, _))
            }
        }.toMap
      } else {
        Map()
      }
    }
  }

  def reservedName(name: String): Boolean =
    getPermanentThemes.exists(theme => name == theme.name || name == theme.prefName)

  private def loadTheme(name: String, prefName: String, url: URL, permanent: Boolean): Option[ColorTheme] = {
    val source: Source = Source.fromURL(url)

    val result: Option[ColorTheme] = Try {
      val lines: Iterator[String] = source.getLines

      val isDark = lines.next.toBoolean

      val colors: Map[String, Color] = {
        lines.map { line =>
          val split = line.split(" ")

          (split(0), new Color(split(1).toInt, split(2).toInt, split(3).toInt, split(4).toInt))
        }.toMap
      }

      if (permanent) {
        ColorTheme(name, prefName, isDark, true, colors)
      } else {
        val default: ColorTheme = {
          if (isDark) {
            DarkTheme
          } else {
            LightTheme
          }
        }

        ColorTheme(name, prefName, isDark, false,
                   default.colors.map((key, color) => (key, colors.getOrElse(key, color))))
      }
    }.toOption

    source.close()

    result
  }

  def saveTheme(theme: ColorTheme, path: Option[Path] = None): Unit = {
    val dest = path.getOrElse(customRoot)

    Files.createDirectories(dest)

    val writer = new PrintWriter(dest.resolve(s"${theme.name}.theme").toFile)

    writer.println(theme.isDark.toString)

    theme.colors.foreach { (key, color) =>
      writer.println(s"$key ${color.getRed} ${color.getGreen} ${color.getBlue} ${color.getAlpha}")
    }

    writer.close()
  }

  def deleteTheme(theme: ColorTheme): Unit = {
    Files.deleteIfExists(customRoot.resolve(s"${theme.name}.theme"))
  }

  def widgetText(): Color = theme.colors("widgetText")
  def widgetTextError(): Color = theme.colors("widgetTextError")
  def widgetHoverShadow(): Color = theme.colors("widgetHoverShadow")
  def widgetPreviewCover(): Color = theme.colors("widgetPreviewCover")
  def widgetPreviewCoverNote(): Color = theme.colors("widgetPreviewCoverNote")
  def widgetHandle(): Color = theme.colors("widgetHandle")
  def displayAreaBackground(): Color = theme.colors("displayAreaBackground")
  def displayAreaText(): Color = theme.colors("displayAreaText")
  def scrollBarBackground(): Color = theme.colors("scrollBarBackground")
  def scrollBarForeground(): Color = theme.colors("scrollBarForeground")
  def scrollBarForegroundHover(): Color = theme.colors("scrollBarForegroundHover")
  def interfaceBackground(): Color = theme.colors("interfaceBackground")
  def commandCenterBackground(): Color = theme.colors("commandCenterBackground")
  def commandCenterText(): Color = theme.colors("commandCenterText")
  def locationToggleImage(): Color = theme.colors("locationToggleImage")
  def commandOutputBackground(): Color = theme.colors("commandOutputBackground")
  def splitPaneDividerBackground(): Color = theme.colors("splitPaneDividerBackground")
  def speedSliderBarBackground(): Color = theme.colors("speedSliderBarBackground")
  def speedSliderBarBackgroundFilled(): Color = theme.colors("speedSliderBarBackgroundFilled")
  def speedSliderThumb(): Color = theme.colors("speedSliderThumb")
  def speedSliderThumbDisabled(): Color = theme.colors("speedSliderThumbDisabled")
  def buttonBackground(): Color = theme.colors("buttonBackground")
  def buttonBackgroundHover(): Color = theme.colors("buttonBackgroundHover")
  def buttonBackgroundPressed(): Color = theme.colors("buttonBackgroundPressed")
  def buttonBackgroundPressedHover(): Color = theme.colors("buttonBackgroundPressedHover")
  def buttonBackgroundDisabled(): Color = theme.colors("buttonBackgroundDisabled")
  def buttonText(): Color = theme.colors("buttonText")
  def buttonTextPressed(): Color = theme.colors("buttonTextPressed")
  def buttonTextDisabled(): Color = theme.colors("buttonTextDisabled")
  def sliderBackground(): Color = theme.colors("sliderBackground")
  def sliderBarBackground(): Color = theme.colors("sliderBarBackground")
  def sliderBarBackgroundFilled(): Color = theme.colors("sliderBarBackgroundFilled")
  def sliderThumbBorder(): Color = theme.colors("sliderThumbBorder")
  def sliderThumbBackground(): Color = theme.colors("sliderThumbBackground")
  def sliderThumbBackgroundPressed(): Color = theme.colors("sliderThumbBackgroundPressed")
  def switchBackground(): Color = theme.colors("switchBackground")
  def switchToggle(): Color = theme.colors("switchToggle")
  def switchToggleBackgroundOn(): Color = theme.colors("switchToggleBackgroundOn")
  def switchToggleBackgroundOff(): Color = theme.colors("switchToggleBackgroundOff")
  def chooserBackground(): Color = theme.colors("chooserBackground")
  def chooserBorder(): Color = theme.colors("chooserBorder")
  def inputBackground(): Color = theme.colors("inputBackground")
  def inputBorder(): Color = theme.colors("inputBorder")
  def viewBackground(): Color = theme.colors("viewBackground")
  def viewBorder(): Color = theme.colors("viewBorder")
  def monitorBackground(): Color = theme.colors("monitorBackground")
  def monitorBorder(): Color = theme.colors("monitorBorder")
  def plotBackground(): Color = theme.colors("plotBackground")
  def plotBorder(): Color = theme.colors("plotBorder")
  def plotMouseBackground(): Color = theme.colors("plotMouseBackground")
  def plotMouseText(): Color = theme.colors("plotMouseText")
  def outputBackground(): Color = theme.colors("outputBackground")
  def outputBorder(): Color = theme.colors("outputBorder")
  def toolbarBackground(): Color = theme.colors("toolbarBackground")
  def tabBackground(): Color = theme.colors("tabBackground")
  def tabBackgroundHover(): Color = theme.colors("tabBackgroundHover")
  def tabBackgroundSelected(): Color = theme.colors("tabBackgroundSelected")
  def tabBackgroundError(): Color = theme.colors("tabBackgroundError")
  def tabText(): Color = theme.colors("tabText")
  def tabTextSelected(): Color = theme.colors("tabTextSelected")
  def tabTextError(): Color = theme.colors("tabTextError")
  def tabBorder(): Color = theme.colors("tabBorder")
  def tabSeparator(): Color = theme.colors("tabSeparator")
  def tabCloseButtonBackgroundHover(): Color = theme.colors("tabCloseButtonBackgroundHover")
  def toolbarText(): Color = theme.colors("toolbarText")
  def toolbarTextSelected(): Color = theme.colors("toolbarTextSelected")
  def toolbarControlBackground(): Color = theme.colors("toolbarControlBackground")
  def toolbarControlBackgroundHover(): Color = theme.colors("toolbarControlBackgroundHover")
  def toolbarControlBackgroundPressed(): Color = theme.colors("toolbarControlBackgroundPressed")
  def toolbarControlBorder(): Color = theme.colors("toolbarControlBorder")
  def toolbarControlBorderSelected(): Color = theme.colors("toolbarControlBorderSelected")
  def toolbarControlFocus(): Color = theme.colors("toolbarControlFocus")
  def toolbarToolSelected(): Color = theme.colors("toolbarToolSelected")
  def toolbarImage(): Color = theme.colors("toolbarImage")
  def toolbarImageSelected(): Color = theme.colors("toolbarImageSelected")
  def toolbarImageDisabled(): Color = theme.colors("toolbarImageDisabled")
  def toolbarSeparator(): Color = theme.colors("toolbarSeparator")
  def infoBackground(): Color = theme.colors("infoBackground")
  def infoH1Background(): Color = theme.colors("infoH1Background")
  def infoH1Color(): Color = theme.colors("infoH1Color")
  def infoH2Background(): Color = theme.colors("infoH2Background")
  def infoH2Color(): Color = theme.colors("infoH2Color")
  def infoH3Color(): Color = theme.colors("infoH3Color")
  def infoH4Color(): Color = theme.colors("infoH4Color")
  def infoPColor(): Color = theme.colors("infoPColor")
  def infoCodeBackground(): Color = theme.colors("infoCodeBackground")
  def infoCodeText(): Color = theme.colors("infoCodeText")
  def infoBlockBar(): Color = theme.colors("infoBlockBar")
  def infoLink(): Color = theme.colors("infoLink")
  def checkFilled(): Color = theme.colors("checkFilled")
  def errorLabelText(): Color = theme.colors("errorLabelText")
  def errorLabelBackground(): Color = theme.colors("errorLabelBackground")
  def warningLabelText(): Color = theme.colors("warningLabelText")
  def warningLabelBackground(): Color = theme.colors("warningLabelBackground")
  def errorHighlight(): Color = theme.colors("errorHighlight")
  def codeBackground(): Color = theme.colors("codeBackground")
  def codeLineHighlight(): Color = theme.colors("codeLineHighlight")
  def codeBracketHighlight(): Color = theme.colors("codeBracketHighlight")
  def codeSelection(): Color = theme.colors("codeSelection")
  def codeSeparator(): Color = theme.colors("codeSeparator")
  def checkboxBackgroundSelected(): Color = theme.colors("checkboxBackgroundSelected")
  def checkboxBackgroundSelectedHover(): Color = theme.colors("checkboxBackgroundSelectedHover")
  def checkboxBackgroundUnselected(): Color = theme.colors("checkboxBackgroundUnselected")
  def checkboxBackgroundUnselectedHover(): Color = theme.colors("checkboxBackgroundUnselectedHover")
  def checkboxBackgroundDisabled(): Color = theme.colors("checkboxBackgroundDisabled")
  def checkboxBorder(): Color = theme.colors("checkboxBorder")
  def checkboxCheck(): Color = theme.colors("checkboxCheck")
  def menuBarBorder(): Color = theme.colors("menuBarBorder")
  def menuBackground(): Color = theme.colors("menuBackground")
  def menuBackgroundHover(): Color = theme.colors("menuBackgroundHover")
  def menuBorder(): Color = theme.colors("menuBorder")
  def menuTextHover(): Color = theme.colors("menuTextHover")
  def menuTextDisabled(): Color = theme.colors("menuTextDisabled")
  def dialogBackground(): Color = theme.colors("dialogBackground")
  def dialogBackgroundSelected(): Color = theme.colors("dialogBackgroundSelected")
  def dialogText(): Color = theme.colors("dialogText")
  def dialogTextSelected(): Color = theme.colors("dialogTextSelected")
  def radioButtonBackground(): Color = theme.colors("radioButtonBackground")
  def radioButtonBackgroundHover(): Color = theme.colors("radioButtonBackgroundHover")
  def radioButtonSelected(): Color = theme.colors("radioButtonSelected")
  def radioButtonSelectedHover(): Color = theme.colors("radioButtonSelectedHover")
  def radioButtonBorder(): Color = theme.colors("radioButtonBorder")
  def primaryButtonBackground(): Color = theme.colors("primaryButtonBackground")
  def primaryButtonBackgroundHover(): Color = theme.colors("primaryButtonBackgroundHover")
  def primaryButtonBackgroundPressed(): Color = theme.colors("primaryButtonBackgroundPressed")
  def primaryButtonBorder(): Color = theme.colors("primaryButtonBorder")
  def primaryButtonText(): Color = theme.colors("primaryButtonText")
  def secondaryButtonBackground(): Color = theme.colors("secondaryButtonBackground")
  def secondaryButtonBackgroundHover(): Color = theme.colors("secondaryButtonBackgroundHover")
  def secondaryButtonBackgroundPressed(): Color = theme.colors("secondaryButtonBackgroundPressed")
  def secondaryButtonBorder(): Color = theme.colors("secondaryButtonBorder")
  def secondaryButtonText(): Color = theme.colors("secondaryButtonText")
  def textAreaBackground(): Color = theme.colors("textAreaBackground")
  def textAreaText(): Color = theme.colors("textAreaText")
  def textAreaBorderEditable(): Color = theme.colors("textAreaBorderEditable")
  def textAreaBorderNoneditable(): Color = theme.colors("textAreaBorderNoneditable")
  def infoIcon(): Color = theme.colors("infoIcon")
  def warningIcon(): Color = theme.colors("warningIcon")
  def errorIcon(): Color = theme.colors("errorIcon")
  def updateIcon(): Color = theme.colors("updateIcon")
  def stockBackground(): Color = theme.colors("stockBackground")
  def converterBackground(): Color = theme.colors("converterBackground")
  def commentColor(): Color = theme.colors("commentToken")
  def commandColor(): Color = theme.colors("commandToken")
  def reporterColor(): Color = theme.colors("reporterToken")
  def keywordColor(): Color = theme.colors("keywordToken")
  def constantColor(): Color = theme.colors("literalToken")
  def defaultColor(): Color = theme.colors("defaultToken")
  def announceX(): Color = theme.colors("announceX")
  def announceXHovered(): Color = theme.colors("announceXHovered")
  def announceXPressed(): Color = theme.colors("announceXPressed")
  def announceRelease(): Color = theme.colors("announceRelease")
  def announceAdvisory(): Color = theme.colors("announceAdvisory")
  def announceEvent(): Color = theme.colors("announceEvent")
  def colorPickerOutputBackground(): Color = theme.colors("colorPickerOutputBackground")
  def colorPickerCheckmark(): Color = theme.colors("colorPickerCheckmark")
  def colorPickerCopyHover(): Color = theme.colors("colorPickerCopyHover")
  def agentMonitorSeparator(): Color = theme.colors("agentMonitorSeparator")
}

case class ColorTheme(name: String, prefName: String, isDark: Boolean, permanent: Boolean,
                      colors: Map[String, Color]) {

  def colorizerTheme: ColorizerTheme = {
    new ColorizerTheme {
      override def getColor(tpe: TokenType): Color = {
        tpe match {
          case TokenType.Literal => colors("literalToken")
          case TokenType.Command => colors("commandToken")
          case TokenType.Reporter => colors("reporterToken")
          case TokenType.Keyword => colors("keywordToken")
          case TokenType.Comment => colors("commentToken")
          case _ => colors("defaultToken")
        }
      }
    }
  }
}

trait ThemeSync {
  def syncTheme(): Unit
}
