// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.theme

import com.jthemedetecor.OsThemeDetector

import java.awt.Color
import java.io.PrintWriter
import java.nio.file.{ Files, Path, Paths }

import org.nlogo.core.{ ColorizerTheme, I18N, NetLogoPreferences, TokenType }

import scala.io.Source
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Try

object InterfaceColors {
  val Transparent = new Color(0, 0, 0, 0)

  private lazy val defaultTheme: String = {
    if (OsThemeDetector.getDetector.isDark) {
      DarkTheme.name
    } else {
      LightTheme.name
    }
  }

  private lazy val customRoot: Path = Paths.get(System.getProperty("user.home"), ".nlogo", "themes")

  private lazy val customThemes: Map[String, ColorTheme] = {
    if (Files.exists(customRoot) && Files.isDirectory(customRoot)) {
      Files.list(customRoot).iterator.asScala.foldLeft(Set[(String, ColorTheme)]()) {
        case (themes, path) =>
          loadTheme(path) match {
            case Some(theme) if !reservedName(theme.name) && !themes.exists(_._1 == theme.name) =>
              themes + ((path.getFileName.toString.stripSuffix(".theme").trim, theme))

            case _ =>
              themes
          }
      }.toMap
    } else {
      Map()
    }
  }

  private var theme: ColorTheme = LightTheme

  def setTheme(theme: ColorTheme): Unit = {
    this.theme = theme
  }

  def getTheme: ColorTheme =
    theme

  def prefsTheme: ColorTheme = {
    // two options for each built-in theme for compatibility with old preference values (Isaac B 1/30/26)
    NetLogoPreferences.get("colorTheme", defaultTheme) match {
      case "classic" | ClassicTheme.name => ClassicTheme
      case "light" | LightTheme.name => LightTheme
      case "dark" | DarkTheme.name => DarkTheme
      case name =>
        customThemes.get(name).getOrElse {
          if (defaultTheme == LightTheme.name) {
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

  private def reservedName(name: String): Boolean = {
    name == "classic" || name == "light" || name == "dark" ||
    name == ClassicTheme.name || name == LightTheme.name || name == DarkTheme.name
  }

  private def loadTheme(path: Path): Option[ColorTheme] = {
    Try {
      val source: Source = Source.fromFile(path.toFile)
      val lines: Iterator[String] = source.getLines

      val isDark = lines.next.toBoolean

      val default: ColorTheme = {
        if (isDark) {
          DarkTheme
        } else {
          LightTheme
        }
      }

      val colors: Map[String, Color] = {
        lines.map { line =>
          val split = line.split(" = ")

          (split(0), new Color(split(1).toInt))
        }.toMap
      }

      source.close()

      new ColorTheme(path.getFileName.toString.stripSuffix(".theme").trim, isDark, false) {
        override def widgetText: Color = colors.getOrElse("widgetText", default.widgetText)
        override def widgetTextError: Color = colors.getOrElse("widgetTextError", default.widgetTextError)
        override def widgetHoverShadow: Color = colors.getOrElse("widgetHoverShadow", default.widgetHoverShadow)
        override def widgetPreviewCover: Color = colors.getOrElse("widgetPreviewCover", default.widgetPreviewCover)
        override def widgetPreviewCoverNote: Color = colors.getOrElse("widgetPreviewCoverNote", default.widgetPreviewCoverNote)
        override def widgetHandle: Color = colors.getOrElse("widgetHandle", default.widgetHandle)
        override def displayAreaBackground: Color = colors.getOrElse("displayAreaBackground", default.displayAreaBackground)
        override def displayAreaText: Color = colors.getOrElse("displayAreaText", default.displayAreaText)
        override def scrollBarBackground: Color = colors.getOrElse("scrollBarBackground", default.scrollBarBackground)
        override def scrollBarForeground: Color = colors.getOrElse("scrollBarForeground", default.scrollBarForeground)
        override def scrollBarForegroundHover: Color = colors.getOrElse("scrollBarForegroundHover", default.scrollBarForegroundHover)
        override def interfaceBackground: Color = colors.getOrElse("interfaceBackground", default.interfaceBackground)
        override def commandCenterBackground: Color = colors.getOrElse("commandCenterBackground", default.commandCenterBackground)
        override def commandCenterText: Color = colors.getOrElse("commandCenterText", default.commandCenterText)
        override def locationToggleImage: Color = colors.getOrElse("locationToggleImage", default.locationToggleImage)
        override def commandOutputBackground: Color = colors.getOrElse("commandOutputBackground", default.commandOutputBackground)
        override def splitPaneDividerBackground: Color = colors.getOrElse("splitPaneDividerBackground", default.splitPaneDividerBackground)
        override def speedSliderBarBackground: Color = colors.getOrElse("speedSliderBarBackground", default.speedSliderBarBackground)
        override def speedSliderBarBackgroundFilled: Color = colors.getOrElse("speedSliderBarBackgroundFilled", default.speedSliderBarBackgroundFilled)
        override def speedSliderThumb: Color = colors.getOrElse("speedSliderThumb", default.speedSliderThumb)
        override def speedSliderThumbDisabled: Color = colors.getOrElse("speedSliderThumbDisabled", default.speedSliderThumbDisabled)
        override def buttonBackground: Color = colors.getOrElse("buttonBackground", default.buttonBackground)
        override def buttonBackgroundHover: Color = colors.getOrElse("buttonBackgroundHover", default.buttonBackgroundHover)
        override def buttonBackgroundPressed: Color = colors.getOrElse("buttonBackgroundPressed", default.buttonBackgroundPressed)
        override def buttonBackgroundPressedHover: Color = colors.getOrElse("buttonBackgroundPressedHover", default.buttonBackgroundPressedHover)
        override def buttonBackgroundDisabled: Color = colors.getOrElse("buttonBackgroundDisabled", default.buttonBackgroundDisabled)
        override def buttonText: Color = colors.getOrElse("buttonText", default.buttonText)
        override def buttonTextPressed: Color = colors.getOrElse("buttonTextPressed", default.buttonTextPressed)
        override def buttonTextDisabled: Color = colors.getOrElse("buttonTextDisabled", default.buttonTextDisabled)
        override def sliderBackground: Color = colors.getOrElse("sliderBackground", default.sliderBackground)
        override def sliderBarBackground: Color = colors.getOrElse("sliderBarBackground", default.sliderBarBackground)
        override def sliderBarBackgroundFilled: Color = colors.getOrElse("sliderBarBackgroundFilled", default.sliderBarBackgroundFilled)
        override def sliderThumbBorder: Color = colors.getOrElse("sliderThumbBorder", default.sliderThumbBorder)
        override def sliderThumbBackground: Color = colors.getOrElse("sliderThumbBackground", default.sliderThumbBackground)
        override def sliderThumbBackgroundPressed: Color = colors.getOrElse("sliderThumbBackgroundPressed", default.sliderThumbBackgroundPressed)
        override def switchBackground: Color = colors.getOrElse("switchBackground", default.switchBackground)
        override def switchToggle: Color = colors.getOrElse("switchToggle", default.switchToggle)
        override def switchToggleBackgroundOn: Color = colors.getOrElse("switchToggleBackgroundOn", default.switchToggleBackgroundOn)
        override def switchToggleBackgroundOff: Color = colors.getOrElse("switchToggleBackgroundOff", default.switchToggleBackgroundOff)
        override def chooserBackground: Color = colors.getOrElse("chooserBackground", default.chooserBackground)
        override def chooserBorder: Color = colors.getOrElse("chooserBorder", default.chooserBorder)
        override def inputBackground: Color = colors.getOrElse("inputBackground", default.inputBackground)
        override def inputBorder: Color = colors.getOrElse("inputBorder", default.inputBorder)
        override def viewBackground: Color = colors.getOrElse("viewBackground", default.viewBackground)
        override def viewBorder: Color = colors.getOrElse("viewBorder", default.viewBorder)
        override def monitorBackground: Color = colors.getOrElse("monitorBackground", default.monitorBackground)
        override def monitorBorder: Color = colors.getOrElse("monitorBorder", default.monitorBorder)
        override def plotBackground: Color = colors.getOrElse("plotBackground", default.plotBackground)
        override def plotBorder: Color = colors.getOrElse("plotBorder", default.plotBorder)
        override def plotMouseBackground: Color = colors.getOrElse("plotMouseBackground", default.plotMouseBackground)
        override def plotMouseText: Color = colors.getOrElse("plotMouseText", default.plotMouseText)
        override def outputBackground: Color = colors.getOrElse("outputBackground", default.outputBackground)
        override def outputBorder: Color = colors.getOrElse("outputBorder", default.outputBorder)
        override def toolbarBackground: Color = colors.getOrElse("toolbarBackground", default.toolbarBackground)
        override def tabBackground: Color = colors.getOrElse("tabBackground", default.tabBackground)
        override def tabBackgroundHover: Color = colors.getOrElse("tabBackgroundHover", default.tabBackgroundHover)
        override def tabBackgroundSelected: Color = colors.getOrElse("tabBackgroundSelected", default.tabBackgroundSelected)
        override def tabBackgroundError: Color = colors.getOrElse("tabBackgroundError", default.tabBackgroundError)
        override def tabText: Color = colors.getOrElse("tabText", default.tabText)
        override def tabTextSelected: Color = colors.getOrElse("tabTextSelected", default.tabTextSelected)
        override def tabTextError: Color = colors.getOrElse("tabTextError", default.tabTextError)
        override def tabBorder: Color = colors.getOrElse("tabBorder", default.tabBorder)
        override def tabSeparator: Color = colors.getOrElse("tabSeparator", default.tabSeparator)
        override def tabCloseButtonBackgroundHover: Color = colors.getOrElse("tabCloseButtonBackgroundHover", default.tabCloseButtonBackgroundHover)
        override def toolbarText: Color = colors.getOrElse("toolbarText", default.toolbarText)
        override def toolbarTextSelected: Color = colors.getOrElse("toolbarTextSelected", default.toolbarTextSelected)
        override def toolbarControlBackground: Color = colors.getOrElse("toolbarControlBackground", default.toolbarControlBackground)
        override def toolbarControlBackgroundHover: Color = colors.getOrElse("toolbarControlBackgroundHover", default.toolbarControlBackgroundHover)
        override def toolbarControlBackgroundPressed: Color = colors.getOrElse("toolbarControlBackgroundPressed", default.toolbarControlBackgroundPressed)
        override def toolbarControlBorder: Color = colors.getOrElse("toolbarControlBorder", default.toolbarControlBorder)
        override def toolbarControlBorderSelected: Color = colors.getOrElse("toolbarControlBorderSelected", default.toolbarControlBorderSelected)
        override def toolbarControlFocus: Color = colors.getOrElse("toolbarControlFocus", default.toolbarControlFocus)
        override def toolbarToolSelected: Color = colors.getOrElse("toolbarToolSelected", default.toolbarToolSelected)
        override def toolbarImage: Color = colors.getOrElse("toolbarImage", default.toolbarImage)
        override def toolbarImageSelected: Color = colors.getOrElse("toolbarImageSelected", default.toolbarImageSelected)
        override def toolbarImageDisabled: Color = colors.getOrElse("toolbarImageDisabled", default.toolbarImageDisabled)
        override def toolbarSeparator: Color = colors.getOrElse("toolbarSeparator", default.toolbarSeparator)
        override def infoBackground: Color = colors.getOrElse("infoBackground", default.infoBackground)
        override def infoH1Background: Color = colors.getOrElse("infoH1Background", default.infoH1Background)
        override def infoH1Color: Color = colors.getOrElse("infoH1Color", default.infoH1Color)
        override def infoH2Background: Color = colors.getOrElse("infoH2Background", default.infoH2Background)
        override def infoH2Color: Color = colors.getOrElse("infoH2Color", default.infoH2Color)
        override def infoH3Color: Color = colors.getOrElse("infoH3Color", default.infoH3Color)
        override def infoH4Color: Color = colors.getOrElse("infoH4Color", default.infoH4Color)
        override def infoPColor: Color = colors.getOrElse("infoPColor", default.infoPColor)
        override def infoCodeBackground: Color = colors.getOrElse("infoCodeBackground", default.infoCodeBackground)
        override def infoCodeText: Color = colors.getOrElse("infoCodeText", default.infoCodeText)
        override def infoBlockBar: Color = colors.getOrElse("infoBlockBar", default.infoBlockBar)
        override def infoLink: Color = colors.getOrElse("infoLink", default.infoLink)
        override def checkFilled: Color = colors.getOrElse("checkFilled", default.checkFilled)
        override def errorLabelText: Color = colors.getOrElse("errorLabelText", default.errorLabelText)
        override def errorLabelBackground: Color = colors.getOrElse("errorLabelBackground", default.errorLabelBackground)
        override def warningLabelText: Color = colors.getOrElse("warningLabelText", default.warningLabelText)
        override def warningLabelBackground: Color = colors.getOrElse("warningLabelBackground", default.warningLabelBackground)
        override def errorHighlight: Color = colors.getOrElse("errorHighlight", default.errorHighlight)
        override def codeBackground: Color = colors.getOrElse("codeBackground", default.codeBackground)
        override def codeLineHighlight: Color = colors.getOrElse("codeLineHighlight", default.codeLineHighlight)
        override def codeBracketHighlight: Color = colors.getOrElse("codeBracketHighlight", default.codeBracketHighlight)
        override def codeSelection: Color = colors.getOrElse("codeSelection", default.codeSelection)
        override def codeSeparator: Color = colors.getOrElse("codeSeparator", default.codeSeparator)
        override def checkboxBackgroundSelected: Color = colors.getOrElse("checkboxBackgroundSelected", default.checkboxBackgroundSelected)
        override def checkboxBackgroundSelectedHover: Color = colors.getOrElse("checkboxBackgroundSelectedHover", default.checkboxBackgroundSelectedHover)
        override def checkboxBackgroundUnselected: Color = colors.getOrElse("checkboxBackgroundUnselected", default.checkboxBackgroundUnselected)
        override def checkboxBackgroundUnselectedHover: Color = colors.getOrElse("checkboxBackgroundUnselectedHover", default.checkboxBackgroundUnselectedHover)
        override def checkboxBackgroundDisabled: Color = colors.getOrElse("checkboxBackgroundDisabled", default.checkboxBackgroundDisabled)
        override def checkboxBorder: Color = colors.getOrElse("checkboxBorder", default.checkboxBorder)
        override def checkboxCheck: Color = colors.getOrElse("checkboxCheck", default.checkboxCheck)
        override def menuBarBorder: Color = colors.getOrElse("menuBarBorder", default.menuBarBorder)
        override def menuBackground: Color = colors.getOrElse("menuBackground", default.menuBackground)
        override def menuBackgroundHover: Color = colors.getOrElse("menuBackgroundHover", default.menuBackgroundHover)
        override def menuBorder: Color = colors.getOrElse("menuBorder", default.menuBorder)
        override def menuTextHover: Color = colors.getOrElse("menuTextHover", default.menuTextHover)
        override def menuTextDisabled: Color = colors.getOrElse("menuTextDisabled", default.menuTextDisabled)
        override def dialogBackground: Color = colors.getOrElse("dialogBackground", default.dialogBackground)
        override def dialogBackgroundSelected: Color = colors.getOrElse("dialogBackgroundSelected", default.dialogBackgroundSelected)
        override def dialogText: Color = colors.getOrElse("dialogText", default.dialogText)
        override def dialogTextSelected: Color = colors.getOrElse("dialogTextSelected", default.dialogTextSelected)
        override def radioButtonBackground: Color = colors.getOrElse("radioButtonBackground", default.radioButtonBackground)
        override def radioButtonBackgroundHover: Color = colors.getOrElse("radioButtonBackgroundHover", default.radioButtonBackgroundHover)
        override def radioButtonSelected: Color = colors.getOrElse("radioButtonSelected", default.radioButtonSelected)
        override def radioButtonSelectedHover: Color = colors.getOrElse("radioButtonSelectedHover", default.radioButtonSelectedHover)
        override def radioButtonBorder: Color = colors.getOrElse("radioButtonBorder", default.radioButtonBorder)
        override def primaryButtonBackground: Color = colors.getOrElse("primaryButtonBackground", default.primaryButtonBackground)
        override def primaryButtonBackgroundHover: Color = colors.getOrElse("primaryButtonBackgroundHover", default.primaryButtonBackgroundHover)
        override def primaryButtonBackgroundPressed: Color = colors.getOrElse("primaryButtonBackgroundPressed", default.primaryButtonBackgroundPressed)
        override def primaryButtonBorder: Color = colors.getOrElse("primaryButtonBorder", default.primaryButtonBorder)
        override def primaryButtonText: Color = colors.getOrElse("primaryButtonText", default.primaryButtonText)
        override def secondaryButtonBackground: Color = colors.getOrElse("secondaryButtonBackground", default.secondaryButtonBackground)
        override def secondaryButtonBackgroundHover: Color = colors.getOrElse("secondaryButtonBackgroundHover", default.secondaryButtonBackgroundHover)
        override def secondaryButtonBackgroundPressed: Color = colors.getOrElse("secondaryButtonBackgroundPressed", default.secondaryButtonBackgroundPressed)
        override def secondaryButtonBorder: Color = colors.getOrElse("secondaryButtonBorder", default.secondaryButtonBorder)
        override def secondaryButtonText: Color = colors.getOrElse("secondaryButtonText", default.secondaryButtonText)
        override def textAreaBackground: Color = colors.getOrElse("textAreaBackground", default.textAreaBackground)
        override def textAreaText: Color = colors.getOrElse("textAreaText", default.textAreaText)
        override def textAreaBorderEditable: Color = colors.getOrElse("textAreaBorderEditable", default.textAreaBorderEditable)
        override def textAreaBorderNoneditable: Color = colors.getOrElse("textAreaBorderNoneditable", default.textAreaBorderNoneditable)
        override def tabbedPaneText: Color = colors.getOrElse("tabbedPaneText", default.tabbedPaneText)
        override def tabbedPaneTextSelected: Color = colors.getOrElse("tabbedPaneTextSelected", default.tabbedPaneTextSelected)
        override def infoIcon: Color = colors.getOrElse("infoIcon", default.infoIcon)
        override def warningIcon: Color = colors.getOrElse("warningIcon", default.warningIcon)
        override def errorIcon: Color = colors.getOrElse("errorIcon", default.errorIcon)
        override def updateIcon: Color = colors.getOrElse("updateIcon", default.updateIcon)
        override def stockBackground: Color = colors.getOrElse("stockBackground", default.stockBackground)
        override def converterBackground: Color = colors.getOrElse("converterBackground", default.converterBackground)
        override def announceX: Color = colors.getOrElse("announceX", default.announceX)
        override def announceXHovered: Color = colors.getOrElse("announceXHovered", default.announceXHovered)
        override def announceXPressed: Color = colors.getOrElse("announceXPressed", default.announceXPressed)
        override def announceRelease: Color = colors.getOrElse("announceRelease", default.announceRelease)
        override def announceAdvisory: Color = colors.getOrElse("announceAdvisory", default.announceAdvisory)
        override def announceEvent: Color = colors.getOrElse("announceEvent", default.announceEvent)
        override def colorPickerOutputBackground: Color = colors.getOrElse("colorPickerOutputBackground", default.colorPickerOutputBackground)
        override def colorPickerCheckmark: Color = colors.getOrElse("colorPickerCheckmark", default.colorPickerCheckmark)
        override def colorPickerCopyHover: Color = colors.getOrElse("colorPickerCopyHover", default.colorPickerCopyHover)
        override def agentMonitorSeparator: Color = colors.getOrElse("agentMonitorSeparator", default.agentMonitorSeparator)

        override def colorizerTheme: ColorizerTheme = new ColorizerTheme {
          override def getColor(tpe: TokenType): Color = {
            tpe match {
              case TokenType.Literal  => colors.getOrElse("constantColor", default.colorizerTheme.getColor(TokenType.Literal))
              case TokenType.Command  => colors.getOrElse("commandColor", default.colorizerTheme.getColor(TokenType.Command))
              case TokenType.Reporter => colors.getOrElse("reporterColor", default.colorizerTheme.getColor(TokenType.Reporter))
              case TokenType.Keyword  => colors.getOrElse("keywordColor", default.colorizerTheme.getColor(TokenType.Keyword))
              case TokenType.Comment  => colors.getOrElse("commentColor", default.colorizerTheme.getColor(TokenType.Comment))
              case _                  => colors.getOrElse("defaultColor", default.colorizerTheme.getColor(null))
            }
          }
        }
      }
    }.toOption
  }

  def saveTheme(theme: ColorTheme): Unit = {
    Files.createDirectories(customRoot)

    val writer = new PrintWriter(customRoot.resolve(s"${theme.name}.theme").toFile)

    writer.println(theme.isDark.toString)

    writer.println(s"widgetText = ${theme.widgetText.getRGB}")
    writer.println(s"widgetTextError = ${theme.widgetTextError.getRGB}")
    writer.println(s"widgetHoverShadow = ${theme.widgetHoverShadow.getRGB}")
    writer.println(s"widgetPreviewCover = ${theme.widgetPreviewCover.getRGB}")
    writer.println(s"widgetPreviewCoverNote = ${theme.widgetPreviewCoverNote.getRGB}")
    writer.println(s"widgetHandle = ${theme.widgetHandle.getRGB}")
    writer.println(s"displayAreaBackground = ${theme.displayAreaBackground.getRGB}")
    writer.println(s"displayAreaText = ${theme.displayAreaText.getRGB}")
    writer.println(s"scrollBarBackground = ${theme.scrollBarBackground.getRGB}")
    writer.println(s"scrollBarForeground = ${theme.scrollBarForeground.getRGB}")
    writer.println(s"scrollBarForegroundHover = ${theme.scrollBarForegroundHover.getRGB}")
    writer.println(s"interfaceBackground = ${theme.interfaceBackground.getRGB}")
    writer.println(s"commandCenterBackground = ${theme.commandCenterBackground.getRGB}")
    writer.println(s"commandCenterText = ${theme.commandCenterText.getRGB}")
    writer.println(s"locationToggleImage = ${theme.locationToggleImage.getRGB}")
    writer.println(s"commandOutputBackground = ${theme.commandOutputBackground.getRGB}")
    writer.println(s"splitPaneDividerBackground = ${theme.splitPaneDividerBackground.getRGB}")
    writer.println(s"speedSliderBarBackground = ${theme.speedSliderBarBackground.getRGB}")
    writer.println(s"speedSliderBarBackgroundFilled = ${theme.speedSliderBarBackgroundFilled.getRGB}")
    writer.println(s"speedSliderThumb = ${theme.speedSliderThumb.getRGB}")
    writer.println(s"speedSliderThumbDisabled = ${theme.speedSliderThumbDisabled.getRGB}")
    writer.println(s"buttonBackground = ${theme.buttonBackground.getRGB}")
    writer.println(s"buttonBackgroundHover = ${theme.buttonBackgroundHover.getRGB}")
    writer.println(s"buttonBackgroundPressed = ${theme.buttonBackgroundPressed.getRGB}")
    writer.println(s"buttonBackgroundPressedHover = ${theme.buttonBackgroundPressedHover.getRGB}")
    writer.println(s"buttonBackgroundDisabled = ${theme.buttonBackgroundDisabled.getRGB}")
    writer.println(s"buttonText = ${theme.buttonText.getRGB}")
    writer.println(s"buttonTextPressed = ${theme.buttonTextPressed.getRGB}")
    writer.println(s"buttonTextDisabled = ${theme.buttonTextDisabled.getRGB}")
    writer.println(s"sliderBackground = ${theme.sliderBackground.getRGB}")
    writer.println(s"sliderBarBackground = ${theme.sliderBarBackground.getRGB}")
    writer.println(s"sliderBarBackgroundFilled = ${theme.sliderBarBackgroundFilled.getRGB}")
    writer.println(s"sliderThumbBorder = ${theme.sliderThumbBorder.getRGB}")
    writer.println(s"sliderThumbBackground = ${theme.sliderThumbBackground.getRGB}")
    writer.println(s"sliderThumbBackgroundPressed = ${theme.sliderThumbBackgroundPressed.getRGB}")
    writer.println(s"switchBackground = ${theme.switchBackground.getRGB}")
    writer.println(s"switchToggle = ${theme.switchToggle.getRGB}")
    writer.println(s"switchToggleBackgroundOn = ${theme.switchToggleBackgroundOn.getRGB}")
    writer.println(s"switchToggleBackgroundOff = ${theme.switchToggleBackgroundOff.getRGB}")
    writer.println(s"chooserBackground = ${theme.chooserBackground.getRGB}")
    writer.println(s"chooserBorder = ${theme.chooserBorder.getRGB}")
    writer.println(s"inputBackground = ${theme.inputBackground.getRGB}")
    writer.println(s"inputBorder = ${theme.inputBorder.getRGB}")
    writer.println(s"viewBackground = ${theme.viewBackground.getRGB}")
    writer.println(s"viewBorder = ${theme.viewBorder.getRGB}")
    writer.println(s"monitorBackground = ${theme.monitorBackground.getRGB}")
    writer.println(s"monitorBorder = ${theme.monitorBorder.getRGB}")
    writer.println(s"plotBackground = ${theme.plotBackground.getRGB}")
    writer.println(s"plotBorder = ${theme.plotBorder.getRGB}")
    writer.println(s"plotMouseBackground = ${theme.plotMouseBackground.getRGB}")
    writer.println(s"plotMouseText = ${theme.plotMouseText.getRGB}")
    writer.println(s"outputBackground = ${theme.outputBackground.getRGB}")
    writer.println(s"outputBorder = ${theme.outputBorder.getRGB}")
    writer.println(s"toolbarBackground = ${theme.toolbarBackground.getRGB}")
    writer.println(s"tabBackground = ${theme.tabBackground.getRGB}")
    writer.println(s"tabBackgroundHover = ${theme.tabBackgroundHover.getRGB}")
    writer.println(s"tabBackgroundSelected = ${theme.tabBackgroundSelected.getRGB}")
    writer.println(s"tabBackgroundError = ${theme.tabBackgroundError.getRGB}")
    writer.println(s"tabText = ${theme.tabText.getRGB}")
    writer.println(s"tabTextSelected = ${theme.tabTextSelected.getRGB}")
    writer.println(s"tabTextError = ${theme.tabTextError.getRGB}")
    writer.println(s"tabBorder = ${theme.tabBorder.getRGB}")
    writer.println(s"tabSeparator = ${theme.tabSeparator.getRGB}")
    writer.println(s"tabCloseButtonBackgroundHover = ${theme.tabCloseButtonBackgroundHover.getRGB}")
    writer.println(s"toolbarText = ${theme.toolbarText.getRGB}")
    writer.println(s"toolbarTextSelected = ${theme.toolbarTextSelected.getRGB}")
    writer.println(s"toolbarControlBackground = ${theme.toolbarControlBackground.getRGB}")
    writer.println(s"toolbarControlBackgroundHover = ${theme.toolbarControlBackgroundHover.getRGB}")
    writer.println(s"toolbarControlBackgroundPressed = ${theme.toolbarControlBackgroundPressed.getRGB}")
    writer.println(s"toolbarControlBorder = ${theme.toolbarControlBorder.getRGB}")
    writer.println(s"toolbarControlBorderSelected = ${theme.toolbarControlBorderSelected.getRGB}")
    writer.println(s"toolbarControlFocus = ${theme.toolbarControlFocus.getRGB}")
    writer.println(s"toolbarToolSelected = ${theme.toolbarToolSelected.getRGB}")
    writer.println(s"toolbarImage = ${theme.toolbarImage.getRGB}")
    writer.println(s"toolbarImageSelected = ${theme.toolbarImageSelected.getRGB}")
    writer.println(s"toolbarImageDisabled = ${theme.toolbarImageDisabled.getRGB}")
    writer.println(s"toolbarSeparator = ${theme.toolbarSeparator.getRGB}")
    writer.println(s"infoBackground = ${theme.infoBackground.getRGB}")
    writer.println(s"infoH1Background = ${theme.infoH1Background.getRGB}")
    writer.println(s"infoH1Color = ${theme.infoH1Color.getRGB}")
    writer.println(s"infoH2Background = ${theme.infoH2Background.getRGB}")
    writer.println(s"infoH2Color = ${theme.infoH2Color.getRGB}")
    writer.println(s"infoH3Color = ${theme.infoH3Color.getRGB}")
    writer.println(s"infoH4Color = ${theme.infoH4Color.getRGB}")
    writer.println(s"infoPColor = ${theme.infoPColor.getRGB}")
    writer.println(s"infoCodeBackground = ${theme.infoCodeBackground.getRGB}")
    writer.println(s"infoCodeText = ${theme.infoCodeText.getRGB}")
    writer.println(s"infoBlockBar = ${theme.infoBlockBar.getRGB}")
    writer.println(s"infoLink = ${theme.infoLink.getRGB}")
    writer.println(s"checkFilled = ${theme.checkFilled.getRGB}")
    writer.println(s"errorLabelText = ${theme.errorLabelText.getRGB}")
    writer.println(s"errorLabelBackground = ${theme.errorLabelBackground.getRGB}")
    writer.println(s"warningLabelText = ${theme.warningLabelText.getRGB}")
    writer.println(s"warningLabelBackground = ${theme.warningLabelBackground.getRGB}")
    writer.println(s"errorHighlight = ${theme.errorHighlight.getRGB}")
    writer.println(s"codeBackground = ${theme.codeBackground.getRGB}")
    writer.println(s"codeLineHighlight = ${theme.codeLineHighlight.getRGB}")
    writer.println(s"codeBracketHighlight = ${theme.codeBracketHighlight.getRGB}")
    writer.println(s"codeSelection = ${theme.codeSelection.getRGB}")
    writer.println(s"codeSeparator = ${theme.codeSeparator.getRGB}")
    writer.println(s"checkboxBackgroundSelected = ${theme.checkboxBackgroundSelected.getRGB}")
    writer.println(s"checkboxBackgroundSelectedHover = ${theme.checkboxBackgroundSelectedHover.getRGB}")
    writer.println(s"checkboxBackgroundUnselected = ${theme.checkboxBackgroundUnselected.getRGB}")
    writer.println(s"checkboxBackgroundUnselectedHover = ${theme.checkboxBackgroundUnselectedHover.getRGB}")
    writer.println(s"checkboxBackgroundDisabled = ${theme.checkboxBackgroundDisabled.getRGB}")
    writer.println(s"checkboxBorder = ${theme.checkboxBorder.getRGB}")
    writer.println(s"checkboxCheck = ${theme.checkboxCheck.getRGB}")
    writer.println(s"menuBarBorder = ${theme.menuBarBorder.getRGB}")
    writer.println(s"menuBackground = ${theme.menuBackground.getRGB}")
    writer.println(s"menuBackgroundHover = ${theme.menuBackgroundHover.getRGB}")
    writer.println(s"menuBorder = ${theme.menuBorder.getRGB}")
    writer.println(s"menuTextHover = ${theme.menuTextHover.getRGB}")
    writer.println(s"menuTextDisabled = ${theme.menuTextDisabled.getRGB}")
    writer.println(s"dialogBackground = ${theme.dialogBackground.getRGB}")
    writer.println(s"dialogBackgroundSelected = ${theme.dialogBackgroundSelected.getRGB}")
    writer.println(s"dialogText = ${theme.dialogText.getRGB}")
    writer.println(s"dialogTextSelected = ${theme.dialogTextSelected.getRGB}")
    writer.println(s"radioButtonBackground = ${theme.radioButtonBackground.getRGB}")
    writer.println(s"radioButtonBackgroundHover = ${theme.radioButtonBackgroundHover.getRGB}")
    writer.println(s"radioButtonSelected = ${theme.radioButtonSelected.getRGB}")
    writer.println(s"radioButtonSelectedHover = ${theme.radioButtonSelectedHover.getRGB}")
    writer.println(s"radioButtonBorder = ${theme.radioButtonBorder.getRGB}")
    writer.println(s"primaryButtonBackground = ${theme.primaryButtonBackground.getRGB}")
    writer.println(s"primaryButtonBackgroundHover = ${theme.primaryButtonBackgroundHover.getRGB}")
    writer.println(s"primaryButtonBackgroundPressed = ${theme.primaryButtonBackgroundPressed.getRGB}")
    writer.println(s"primaryButtonBorder = ${theme.primaryButtonBorder.getRGB}")
    writer.println(s"primaryButtonText = ${theme.primaryButtonText.getRGB}")
    writer.println(s"secondaryButtonBackground = ${theme.secondaryButtonBackground.getRGB}")
    writer.println(s"secondaryButtonBackgroundHover = ${theme.secondaryButtonBackgroundHover.getRGB}")
    writer.println(s"secondaryButtonBackgroundPressed = ${theme.secondaryButtonBackgroundPressed.getRGB}")
    writer.println(s"secondaryButtonBorder = ${theme.secondaryButtonBorder.getRGB}")
    writer.println(s"secondaryButtonText = ${theme.secondaryButtonText.getRGB}")
    writer.println(s"textAreaBackground = ${theme.textAreaBackground.getRGB}")
    writer.println(s"textAreaText = ${theme.textAreaText.getRGB}")
    writer.println(s"textAreaBorderEditable = ${theme.textAreaBorderEditable.getRGB}")
    writer.println(s"textAreaBorderNoneditable = ${theme.textAreaBorderNoneditable.getRGB}")
    writer.println(s"tabbedPaneText = ${theme.tabbedPaneText.getRGB}")
    writer.println(s"tabbedPaneTextSelected = ${theme.tabbedPaneTextSelected.getRGB}")
    writer.println(s"infoIcon = ${theme.infoIcon.getRGB}")
    writer.println(s"warningIcon = ${theme.warningIcon.getRGB}")
    writer.println(s"errorIcon = ${theme.errorIcon.getRGB}")
    writer.println(s"updateIcon = ${theme.updateIcon.getRGB}")
    writer.println(s"stockBackground = ${theme.stockBackground.getRGB}")
    writer.println(s"converterBackground = ${theme.converterBackground.getRGB}")
    writer.println(s"announceX = ${theme.announceX.getRGB}")
    writer.println(s"announceXHovered = ${theme.announceXHovered.getRGB}")
    writer.println(s"announceXPressed = ${theme.announceXPressed.getRGB}")
    writer.println(s"announceRelease = ${theme.announceRelease.getRGB}")
    writer.println(s"announceAdvisory = ${theme.announceAdvisory.getRGB}")
    writer.println(s"announceEvent = ${theme.announceEvent.getRGB}")
    writer.println(s"colorPickerOutputBackground = ${theme.colorPickerOutputBackground.getRGB}")
    writer.println(s"colorPickerCheckmark = ${theme.colorPickerCheckmark.getRGB}")
    writer.println(s"colorPickerCopyHover = ${theme.colorPickerCopyHover.getRGB}")
    writer.println(s"agentMonitorSeparator = ${theme.agentMonitorSeparator.getRGB}")
    writer.println(s"constantColor = ${theme.colorizerTheme.getColor(TokenType.Literal).getRGB}")
    writer.println(s"commandColor = ${theme.colorizerTheme.getColor(TokenType.Command).getRGB}")
    writer.println(s"reporterColor = ${theme.colorizerTheme.getColor(TokenType.Reporter).getRGB}")
    writer.println(s"keywordColor = ${theme.colorizerTheme.getColor(TokenType.Keyword).getRGB}")
    writer.println(s"commentColor = ${theme.colorizerTheme.getColor(TokenType.Comment).getRGB}")
    writer.println(s"defaultColor = ${theme.colorizerTheme.getColor(null).getRGB}")

    writer.close()
  }

  def deleteTheme(theme: ColorTheme): Unit = {
    Files.deleteIfExists(customRoot.resolve(s"${theme.name}.theme"))
  }

  def widgetText(): Color = theme.widgetText
  def widgetTextError(): Color = theme.widgetTextError
  def widgetHoverShadow(): Color = theme.widgetHoverShadow
  def widgetPreviewCover(): Color = theme.widgetPreviewCover
  def widgetPreviewCoverNote(): Color = theme.widgetPreviewCoverNote
  def widgetHandle(): Color = theme.widgetHandle
  def displayAreaBackground(): Color = theme.displayAreaBackground
  def displayAreaText(): Color = theme.displayAreaText
  def scrollBarBackground(): Color = theme.scrollBarBackground
  def scrollBarForeground(): Color = theme.scrollBarForeground
  def scrollBarForegroundHover(): Color = theme.scrollBarForegroundHover
  def interfaceBackground(): Color = theme.interfaceBackground
  def commandCenterBackground(): Color = theme.commandCenterBackground
  def commandCenterText(): Color = theme.commandCenterText
  def locationToggleImage(): Color = theme.locationToggleImage
  def commandOutputBackground(): Color = theme.commandOutputBackground
  def splitPaneDividerBackground(): Color = theme.splitPaneDividerBackground
  def speedSliderBarBackground(): Color = theme.speedSliderBarBackground
  def speedSliderBarBackgroundFilled(): Color = theme.speedSliderBarBackgroundFilled
  def speedSliderThumb(): Color = theme.speedSliderThumb
  def speedSliderThumbDisabled(): Color = theme.speedSliderThumbDisabled
  def buttonBackground(): Color = theme.buttonBackground
  def buttonBackgroundHover(): Color = theme.buttonBackgroundHover
  def buttonBackgroundPressed(): Color = theme.buttonBackgroundPressed
  def buttonBackgroundPressedHover(): Color = theme.buttonBackgroundPressedHover
  def buttonBackgroundDisabled(): Color = theme.buttonBackgroundDisabled
  def buttonText(): Color = theme.buttonText
  def buttonTextPressed(): Color = theme.buttonTextPressed
  def buttonTextDisabled(): Color = theme.buttonTextDisabled
  def sliderBackground(): Color = theme.sliderBackground
  def sliderBarBackground(): Color = theme.sliderBarBackground
  def sliderBarBackgroundFilled(): Color = theme.sliderBarBackgroundFilled
  def sliderThumbBorder(): Color = theme.sliderThumbBorder
  def sliderThumbBackground(): Color = theme.sliderThumbBackground
  def sliderThumbBackgroundPressed(): Color = theme.sliderThumbBackgroundPressed
  def switchBackground(): Color = theme.switchBackground
  def switchToggle(): Color = theme.switchToggle
  def switchToggleBackgroundOn(): Color = theme.switchToggleBackgroundOn
  def switchToggleBackgroundOff(): Color = theme.switchToggleBackgroundOff
  def chooserBackground(): Color = theme.chooserBackground
  def chooserBorder(): Color = theme.chooserBorder
  def inputBackground(): Color = theme.inputBackground
  def inputBorder(): Color = theme.inputBorder
  def viewBackground(): Color = theme.viewBackground
  def viewBorder(): Color = theme.viewBorder
  def monitorBackground(): Color = theme.monitorBackground
  def monitorBorder(): Color = theme.monitorBorder
  def plotBackground(): Color = theme.plotBackground
  def plotBorder(): Color = theme.plotBorder
  def plotMouseBackground(): Color = theme.plotMouseBackground
  def plotMouseText(): Color = theme.plotMouseText
  def outputBackground(): Color = theme.outputBackground
  def outputBorder(): Color = theme.outputBorder
  def toolbarBackground(): Color = theme.toolbarBackground
  def tabBackground(): Color = theme.tabBackground
  def tabBackgroundHover(): Color = theme.tabBackgroundHover
  def tabBackgroundSelected(): Color = theme.tabBackgroundSelected
  def tabBackgroundError(): Color = theme.tabBackgroundError
  def tabText(): Color = theme.tabText
  def tabTextSelected(): Color = theme.tabTextSelected
  def tabTextError(): Color = theme.tabTextError
  def tabBorder(): Color = theme.tabBorder
  def tabSeparator(): Color = theme.tabSeparator
  def tabCloseButtonBackgroundHover(): Color = theme.tabCloseButtonBackgroundHover
  def toolbarText(): Color = theme.toolbarText
  def toolbarTextSelected(): Color = theme.toolbarTextSelected
  def toolbarControlBackground(): Color = theme.toolbarControlBackground
  def toolbarControlBackgroundHover(): Color = theme.toolbarControlBackgroundHover
  def toolbarControlBackgroundPressed(): Color = theme.toolbarControlBackgroundPressed
  def toolbarControlBorder(): Color = theme.toolbarControlBorder
  def toolbarControlBorderSelected(): Color = theme.toolbarControlBorderSelected
  def toolbarControlFocus(): Color = theme.toolbarControlFocus
  def toolbarToolSelected(): Color = theme.toolbarToolSelected
  def toolbarImage(): Color = theme.toolbarImage
  def toolbarImageSelected(): Color = theme.toolbarImageSelected
  def toolbarImageDisabled(): Color = theme.toolbarImageDisabled
  def toolbarSeparator(): Color = theme.toolbarSeparator
  def infoBackground(): Color = theme.infoBackground
  def infoH1Background(): Color = theme.infoH1Background
  def infoH1Color(): Color = theme.infoH1Color
  def infoH2Background(): Color = theme.infoH2Background
  def infoH2Color(): Color = theme.infoH2Color
  def infoH3Color(): Color = theme.infoH3Color
  def infoH4Color(): Color = theme.infoH4Color
  def infoPColor(): Color = theme.infoPColor
  def infoCodeBackground(): Color = theme.infoCodeBackground
  def infoCodeText(): Color = theme.infoCodeText
  def infoBlockBar(): Color = theme.infoBlockBar
  def infoLink(): Color = theme.infoLink
  def checkFilled(): Color = theme.checkFilled
  def errorLabelText(): Color = theme.errorLabelText
  def errorLabelBackground(): Color = theme.errorLabelBackground
  def warningLabelText(): Color = theme.warningLabelText
  def warningLabelBackground(): Color = theme.warningLabelBackground
  def errorHighlight(): Color = theme.errorHighlight
  def codeBackground(): Color = theme.codeBackground
  def codeLineHighlight(): Color = theme.codeLineHighlight
  def codeBracketHighlight(): Color = theme.codeBracketHighlight
  def codeSelection(): Color = theme.codeSelection
  def codeSeparator(): Color = theme.codeSeparator
  def checkboxBackgroundSelected(): Color = theme.checkboxBackgroundSelected
  def checkboxBackgroundSelectedHover(): Color = theme.checkboxBackgroundSelectedHover
  def checkboxBackgroundUnselected(): Color = theme.checkboxBackgroundUnselected
  def checkboxBackgroundUnselectedHover(): Color = theme.checkboxBackgroundUnselectedHover
  def checkboxBackgroundDisabled(): Color = theme.checkboxBackgroundDisabled
  def checkboxBorder(): Color = theme.checkboxBorder
  def checkboxCheck(): Color = theme.checkboxCheck
  def menuBarBorder(): Color = theme.menuBarBorder
  def menuBackground(): Color = theme.menuBackground
  def menuBackgroundHover(): Color = theme.menuBackgroundHover
  def menuBorder(): Color = theme.menuBorder
  def menuTextHover(): Color = theme.menuTextHover
  def menuTextDisabled(): Color = theme.menuTextDisabled
  def dialogBackground(): Color = theme.dialogBackground
  def dialogBackgroundSelected(): Color = theme.dialogBackgroundSelected
  def dialogText(): Color = theme.dialogText
  def dialogTextSelected(): Color = theme.dialogTextSelected
  def radioButtonBackground(): Color = theme.radioButtonBackground
  def radioButtonBackgroundHover(): Color = theme.radioButtonBackgroundHover
  def radioButtonSelected(): Color = theme.radioButtonSelected
  def radioButtonSelectedHover(): Color = theme.radioButtonSelectedHover
  def radioButtonBorder(): Color = theme.radioButtonBorder
  def primaryButtonBackground(): Color = theme.primaryButtonBackground
  def primaryButtonBackgroundHover(): Color = theme.primaryButtonBackgroundHover
  def primaryButtonBackgroundPressed(): Color = theme.primaryButtonBackgroundPressed
  def primaryButtonBorder(): Color = theme.primaryButtonBorder
  def primaryButtonText(): Color = theme.primaryButtonText
  def secondaryButtonBackground(): Color = theme.secondaryButtonBackground
  def secondaryButtonBackgroundHover(): Color = theme.secondaryButtonBackgroundHover
  def secondaryButtonBackgroundPressed(): Color = theme.secondaryButtonBackgroundPressed
  def secondaryButtonBorder(): Color = theme.secondaryButtonBorder
  def secondaryButtonText(): Color = theme.secondaryButtonText
  def textAreaBackground(): Color = theme.textAreaBackground
  def textAreaText(): Color = theme.textAreaText
  def textAreaBorderEditable(): Color = theme.textAreaBorderEditable
  def textAreaBorderNoneditable(): Color = theme.textAreaBorderNoneditable
  def tabbedPaneText(): Color = theme.tabbedPaneText
  def tabbedPaneTextSelected(): Color = theme.tabbedPaneTextSelected
  def infoIcon(): Color = theme.infoIcon
  def warningIcon(): Color = theme.warningIcon
  def errorIcon(): Color = theme.errorIcon
  def updateIcon(): Color = theme.updateIcon
  def stockBackground(): Color = theme.stockBackground
  def converterBackground(): Color = theme.converterBackground
  def commentColor(): Color = theme.colorizerTheme.getColor(TokenType.Comment)
  def commandColor(): Color = theme.colorizerTheme.getColor(TokenType.Command)
  def reporterColor(): Color = theme.colorizerTheme.getColor(TokenType.Reporter)
  def keywordColor(): Color = theme.colorizerTheme.getColor(TokenType.Keyword)
  def constantColor(): Color = theme.colorizerTheme.getColor(TokenType.Literal)
  def defaultColor(): Color = theme.colorizerTheme.getColor(null)
  def announceX(): Color = theme.announceX
  def announceXHovered(): Color = theme.announceXHovered
  def announceXPressed(): Color = theme.announceXPressed
  def announceRelease(): Color = theme.announceRelease
  def announceAdvisory(): Color = theme.announceAdvisory
  def announceEvent(): Color = theme.announceEvent
  def colorPickerOutputBackground(): Color = theme.colorPickerOutputBackground
  def colorPickerCheckmark(): Color = theme.colorPickerCheckmark
  def colorPickerCopyHover(): Color = theme.colorPickerCopyHover
  def agentMonitorSeparator(): Color = theme.agentMonitorSeparator
}

abstract class ColorTheme(val name: String, val isDark: Boolean, val permanent: Boolean) {
  protected val ClassicLavender = new Color(188, 188, 230)
  protected val ClassicLightGreen = new Color(130, 188, 183)
  protected val ClassicDarkGreen = new Color(65, 94, 91)
  protected val ClassicOrange = new Color(200, 103, 103)
  protected val ClassicBeige = new Color(225, 225, 182)

  protected val LightBlue = new Color(207, 229, 255)
  protected val MediumBlue = new Color(6, 112, 237)
  protected val MediumBlue2 = new Color(0, 102, 227)
  protected val MediumBlue3 = new Color(0, 92, 217)
  protected val MediumBlue4 = new Color(0, 72, 197)
  protected val DarkBlue = new Color(0, 54, 117)
  protected val White2 = new Color(245, 245, 245)
  protected val LightGray = new Color(238, 238, 238)
  protected val LightGray1 = new Color(232, 232, 232)
  protected val LightGray2 = new Color(215, 215, 215)
  protected val MediumGray = new Color(175, 175, 175)
  protected val LightGrayOutline = new Color(120, 120, 120)
  protected val LightGrayOutline2 = new Color(100, 100, 100)
  protected val DarkGray = new Color(79, 79, 79)
  protected val BlueGray = new Color(70, 70, 76)
  protected val MediumBlueGray = new Color(60, 60, 65)
  protected val DarkBlueGray = new Color(45, 45, 54)
  protected val DarkBlueGray2 = new Color(35, 35, 44)
  protected val DarkBlueGray3 = new Color(25, 25, 34)
  protected val DarkBlueGray4 = new Color(10, 10, 20)
  protected val LightRed = new Color(251, 96, 85)
  protected val AlmostBlack = new Color(22, 22, 22)

  def widgetText: Color
  def widgetTextError: Color
  def widgetHoverShadow: Color
  def widgetPreviewCover: Color
  def widgetPreviewCoverNote: Color
  def widgetHandle: Color
  def displayAreaBackground: Color
  def displayAreaText: Color
  def scrollBarBackground: Color
  def scrollBarForeground: Color
  def scrollBarForegroundHover: Color
  def interfaceBackground: Color
  def commandCenterBackground: Color
  def commandCenterText: Color
  def locationToggleImage: Color
  def commandOutputBackground: Color
  def splitPaneDividerBackground: Color
  def speedSliderBarBackground: Color
  def speedSliderBarBackgroundFilled: Color
  def speedSliderThumb: Color
  def speedSliderThumbDisabled: Color
  def buttonBackground: Color
  def buttonBackgroundHover: Color
  def buttonBackgroundPressed: Color
  def buttonBackgroundPressedHover: Color
  def buttonBackgroundDisabled: Color
  def buttonText: Color
  def buttonTextPressed: Color
  def buttonTextDisabled: Color
  def sliderBackground: Color
  def sliderBarBackground: Color
  def sliderBarBackgroundFilled: Color
  def sliderThumbBorder: Color
  def sliderThumbBackground: Color
  def sliderThumbBackgroundPressed: Color
  def switchBackground: Color
  def switchToggle: Color
  def switchToggleBackgroundOn: Color
  def switchToggleBackgroundOff: Color
  def chooserBackground: Color
  def chooserBorder: Color
  def inputBackground: Color
  def inputBorder: Color
  def viewBackground: Color
  def viewBorder: Color
  def monitorBackground: Color
  def monitorBorder: Color
  def plotBackground: Color
  def plotBorder: Color
  def plotMouseBackground: Color
  def plotMouseText: Color
  def outputBackground: Color
  def outputBorder: Color
  def toolbarBackground: Color
  def tabBackground: Color
  def tabBackgroundHover: Color
  def tabBackgroundSelected: Color
  def tabBackgroundError: Color
  def tabText: Color
  def tabTextSelected: Color
  def tabTextError: Color
  def tabBorder: Color
  def tabSeparator: Color
  def tabCloseButtonBackgroundHover: Color
  def toolbarText: Color
  def toolbarTextSelected: Color
  def toolbarControlBackground: Color
  def toolbarControlBackgroundHover: Color
  def toolbarControlBackgroundPressed: Color
  def toolbarControlBorder: Color
  def toolbarControlBorderSelected: Color
  def toolbarControlFocus: Color
  def toolbarToolSelected: Color
  def toolbarImage: Color
  def toolbarImageSelected: Color
  def toolbarImageDisabled: Color
  def toolbarSeparator: Color
  def infoBackground: Color
  def infoH1Background: Color
  def infoH1Color: Color
  def infoH2Background: Color
  def infoH2Color: Color
  def infoH3Color: Color
  def infoH4Color: Color
  def infoPColor: Color
  def infoCodeBackground: Color
  def infoCodeText: Color
  def infoBlockBar: Color
  def infoLink: Color
  def checkFilled: Color
  def errorLabelText: Color
  def errorLabelBackground: Color
  def warningLabelText: Color
  def warningLabelBackground: Color
  def errorHighlight: Color
  def codeBackground: Color
  def codeLineHighlight: Color
  def codeBracketHighlight: Color
  def codeSelection: Color
  def codeSeparator: Color
  def checkboxBackgroundSelected: Color
  def checkboxBackgroundSelectedHover: Color
  def checkboxBackgroundUnselected: Color
  def checkboxBackgroundUnselectedHover: Color
  def checkboxBackgroundDisabled: Color
  def checkboxBorder: Color
  def checkboxCheck: Color
  def menuBarBorder: Color
  def menuBackground: Color
  def menuBackgroundHover: Color
  def menuBorder: Color
  def menuTextHover: Color
  def menuTextDisabled: Color
  def dialogBackground: Color
  def dialogBackgroundSelected: Color
  def dialogText: Color
  def dialogTextSelected: Color
  def radioButtonBackground: Color
  def radioButtonBackgroundHover: Color
  def radioButtonSelected: Color
  def radioButtonSelectedHover: Color
  def radioButtonBorder: Color
  def primaryButtonBackground: Color
  def primaryButtonBackgroundHover: Color
  def primaryButtonBackgroundPressed: Color
  def primaryButtonBorder: Color
  def primaryButtonText: Color
  def secondaryButtonBackground: Color
  def secondaryButtonBackgroundHover: Color
  def secondaryButtonBackgroundPressed: Color
  def secondaryButtonBorder: Color
  def secondaryButtonText: Color
  def textAreaBackground: Color
  def textAreaText: Color
  def textAreaBorderEditable: Color
  def textAreaBorderNoneditable: Color
  def tabbedPaneText: Color
  def tabbedPaneTextSelected: Color
  def infoIcon: Color
  def warningIcon: Color
  def errorIcon: Color
  def updateIcon: Color
  def stockBackground: Color
  def converterBackground: Color
  def announceX: Color
  def announceXHovered: Color
  def announceXPressed: Color
  def announceRelease: Color
  def announceAdvisory: Color
  def announceEvent: Color
  def colorPickerOutputBackground: Color
  def colorPickerCheckmark: Color
  def colorPickerCopyHover: Color
  def agentMonitorSeparator: Color

  def colorizerTheme: ColorizerTheme
}

object ClassicTheme extends ColorTheme(I18N.gui.get("menu.tools.themesManager.classic"), false, true) {
  override def widgetText: Color = Color.BLACK
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(255, 255, 255, 100)
  override def widgetPreviewCoverNote: Color = new Color(175, 175, 175, 75)
  override def widgetHandle: Color = DarkGray
  override def displayAreaBackground: Color = Color.WHITE
  override def displayAreaText: Color = Color.BLACK
  override def scrollBarBackground: Color = LightGray
  override def scrollBarForeground: Color = MediumGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = Color.WHITE
  override def commandCenterBackground: Color = LightGray
  override def commandCenterText: Color = Color.BLACK
  override def locationToggleImage: Color = Color.BLACK
  override def commandOutputBackground: Color = Color.WHITE
  override def splitPaneDividerBackground: Color = MediumGray
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = ClassicLavender
  override def buttonBackgroundHover: Color = ClassicLavender
  override def buttonBackgroundPressed: Color = Color.BLACK
  override def buttonBackgroundPressedHover: Color = Color.BLACK
  override def buttonBackgroundDisabled: Color = ClassicLavender
  override def buttonText: Color = Color.BLACK
  override def buttonTextPressed: Color = ClassicLavender
  override def buttonTextDisabled: Color = Color.BLACK
  override def sliderBackground: Color = ClassicLightGreen
  override def sliderBarBackground: Color = ClassicDarkGreen
  override def sliderBarBackgroundFilled: Color = ClassicDarkGreen
  override def sliderThumbBorder: Color = ClassicOrange
  override def sliderThumbBackground: Color = ClassicOrange
  override def sliderThumbBackgroundPressed: Color = ClassicOrange
  override def switchBackground: Color = ClassicLightGreen
  override def switchToggle: Color = ClassicOrange
  override def switchToggleBackgroundOn: Color = ClassicDarkGreen
  override def switchToggleBackgroundOff: Color = ClassicDarkGreen
  override def chooserBackground: Color = ClassicLightGreen
  override def chooserBorder: Color = ClassicDarkGreen
  override def inputBackground: Color = ClassicLightGreen
  override def inputBorder: Color = ClassicDarkGreen
  override def viewBackground: Color = MediumGray
  override def viewBorder: Color = Color.BLACK
  override def monitorBackground: Color = ClassicBeige
  override def monitorBorder: Color = MediumGray
  override def plotBackground: Color = ClassicBeige
  override def plotBorder: Color = MediumGray
  override def plotMouseBackground: Color = ClassicBeige
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = ClassicBeige
  override def outputBorder: Color = MediumGray
  override def toolbarBackground: Color = LightGray
  override def tabBackground: Color = Color.WHITE
  override def tabBackgroundHover: Color = LightGray
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.BLACK
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = MediumGray
  override def tabSeparator: Color = MediumGray
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.BLACK
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = Color.WHITE
  override def toolbarControlBackgroundHover: Color = White2
  override def toolbarControlBackgroundPressed: Color = LightGray2
  override def toolbarControlBorder: Color = MediumGray
  override def toolbarControlBorderSelected: Color = InterfaceColors.Transparent
  override def toolbarControlFocus: Color = MediumBlue
  override def toolbarToolSelected: Color = MediumBlue
  override def toolbarImage: Color = new Color(85, 87, 112)
  override def toolbarImageSelected: Color = Color.WHITE
  override def toolbarImageDisabled: Color = new Color(100, 100, 100, 64)
  override def toolbarSeparator: Color = MediumGray
  override def infoBackground: Color = Color.WHITE
  override def infoH1Background: Color = new Color(209, 208, 255)
  override def infoH1Color: Color = new Color(19, 13, 134)
  override def infoH2Background: Color = new Color(211, 231, 255)
  override def infoH2Color: Color = new Color(0, 90, 200)
  override def infoH3Color: Color = new Color(88, 88, 88)
  override def infoH4Color: Color = new Color(115, 115, 115)
  override def infoPColor: Color = Color.BLACK
  override def infoCodeBackground: Color = LightGray
  override def infoCodeText: Color = Color.BLACK
  override def infoBlockBar: Color = new Color(96, 96, 96)
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = Color.WHITE
  override def codeLineHighlight: Color = new Color(255, 255, 204)
  override def codeBracketHighlight: Color = new Color(200, 200, 255)
  override def codeSelection: Color = new Color(200, 200, 255)
  override def codeSeparator: Color = LightGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = Color.WHITE
  override def checkboxBackgroundUnselectedHover: Color = White2
  override def checkboxBackgroundDisabled: Color = MediumGray
  override def checkboxBorder: Color = MediumGray
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = LightGray2
  override def menuBackground: Color = Color.WHITE
  override def menuBackgroundHover: Color = MediumBlue
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = White2
  override def dialogBackgroundSelected: Color = MediumBlue
  override def dialogText: Color = Color.BLACK
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = Color.WHITE
  override def radioButtonBackgroundHover: Color = White2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = MediumGray
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = Color.WHITE
  override def secondaryButtonBackgroundHover: Color = White2
  override def secondaryButtonBackgroundPressed: Color = LightGray2
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.BLACK
  override def textAreaBackground: Color = Color.WHITE
  override def textAreaText: Color = Color.BLACK
  override def textAreaBorderEditable: Color = MediumGray
  override def textAreaBorderNoneditable: Color = LightGray
  override def tabbedPaneText: Color = Color.BLACK
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = DarkGray
  override def announceXHovered: Color = LightGrayOutline
  override def announceXPressed: Color = MediumGray
  override def announceRelease: Color = new Color(237, 205, 255)
  override def announceAdvisory: Color = new Color(255, 194, 154)
  override def announceEvent: Color = new Color(108, 252, 221)
  override def colorPickerOutputBackground: Color = new Color(125, 125, 125)
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(197, 197, 197)
  override def agentMonitorSeparator: Color = MediumGray

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Classic
}

object LightTheme extends ColorTheme(I18N.gui.get("menu.tools.themesManager.light"), false, true) {
  override def widgetText: Color = new Color(53, 54, 74)
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(255, 255, 255, 100)
  override def widgetPreviewCoverNote: Color = new Color(175, 175, 175, 75)
  override def widgetHandle: Color = DarkGray
  override def displayAreaBackground: Color = Color.WHITE
  override def displayAreaText: Color = Color.BLACK
  override def scrollBarBackground: Color = LightGray
  override def scrollBarForeground: Color = MediumGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = Color.WHITE
  override def commandCenterBackground: Color = LightGray
  override def commandCenterText: Color = new Color(53, 54, 74)
  override def locationToggleImage: Color = Color.BLACK
  override def commandOutputBackground: Color = Color.WHITE
  override def splitPaneDividerBackground: Color = MediumGray
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = MediumBlue
  override def buttonBackgroundHover: Color = new Color(31, 134, 255)
  override def buttonBackgroundPressed: Color = new Color(0, 52, 115)
  override def buttonBackgroundPressedHover: Color = new Color(0, 35, 77)
  override def buttonBackgroundDisabled: Color = new Color(222, 222, 222)
  override def buttonText: Color = Color.WHITE
  override def buttonTextPressed: Color = Color.WHITE
  override def buttonTextDisabled: Color = new Color(115, 115, 115)
  override def sliderBackground: Color = LightBlue
  override def sliderBarBackground: Color = MediumGray
  override def sliderBarBackgroundFilled: Color = MediumBlue
  override def sliderThumbBorder: Color = MediumBlue
  override def sliderThumbBackground: Color = Color.WHITE
  override def sliderThumbBackgroundPressed: Color = MediumBlue
  override def switchBackground: Color = LightBlue
  override def switchToggle: Color = Color.WHITE
  override def switchToggleBackgroundOn: Color = MediumBlue
  override def switchToggleBackgroundOff: Color = MediumGray
  override def chooserBackground: Color = LightBlue
  override def chooserBorder: Color = MediumBlue
  override def inputBackground: Color = LightBlue
  override def inputBorder: Color = MediumBlue
  override def viewBackground: Color = MediumGray
  override def viewBorder: Color = Color.BLACK
  override def monitorBackground: Color = LightGray1
  override def monitorBorder: Color = MediumGray
  override def plotBackground: Color = LightGray1
  override def plotBorder: Color = MediumGray
  override def plotMouseBackground: Color = LightGray
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = LightGray1
  override def outputBorder: Color = MediumGray
  override def toolbarBackground: Color = LightGray
  override def tabBackground: Color = Color.WHITE
  override def tabBackgroundHover: Color = LightGray
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.BLACK
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = MediumGray
  override def tabSeparator: Color = MediumGray
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.BLACK
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = Color.WHITE
  override def toolbarControlBackgroundHover: Color = White2
  override def toolbarControlBackgroundPressed: Color = LightGray2
  override def toolbarControlBorder: Color = MediumGray
  override def toolbarControlBorderSelected: Color = InterfaceColors.Transparent
  override def toolbarControlFocus: Color = MediumBlue
  override def toolbarToolSelected: Color = MediumBlue
  override def toolbarImage: Color = new Color(85, 87, 112)
  override def toolbarImageSelected: Color = Color.WHITE
  override def toolbarImageDisabled: Color = new Color(100, 100, 100, 64)
  override def toolbarSeparator: Color = MediumGray
  override def infoBackground: Color = Color.WHITE
  override def infoH1Background: Color = new Color(209, 208, 255)
  override def infoH1Color: Color = new Color(19, 13, 134)
  override def infoH2Background: Color = new Color(211, 231, 255)
  override def infoH2Color: Color = new Color(0, 90, 200)
  override def infoH3Color: Color = new Color(88, 88, 88)
  override def infoH4Color: Color = new Color(115, 115, 115)
  override def infoPColor: Color = Color.BLACK
  override def infoCodeBackground: Color = LightGray
  override def infoCodeText: Color = Color.BLACK
  override def infoBlockBar: Color = new Color(96, 96, 96)
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = Color.WHITE
  override def codeLineHighlight: Color = new Color(255, 255, 204)
  override def codeBracketHighlight: Color = new Color(200, 200, 255)
  override def codeSelection: Color = new Color(200, 200, 255)
  override def codeSeparator: Color = LightGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = Color.WHITE
  override def checkboxBackgroundUnselectedHover: Color = White2
  override def checkboxBackgroundDisabled: Color = MediumGray
  override def checkboxBorder: Color = MediumGray
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = LightGray2
  override def menuBackground: Color = Color.WHITE
  override def menuBackgroundHover: Color = MediumBlue
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = White2
  override def dialogBackgroundSelected: Color = MediumBlue
  override def dialogText: Color = Color.BLACK
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = Color.WHITE
  override def radioButtonBackgroundHover: Color = White2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = MediumGray
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = Color.WHITE
  override def secondaryButtonBackgroundHover: Color = White2
  override def secondaryButtonBackgroundPressed: Color = LightGray2
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.BLACK
  override def textAreaBackground: Color = Color.WHITE
  override def textAreaText: Color = Color.BLACK
  override def textAreaBorderEditable: Color = MediumGray
  override def textAreaBorderNoneditable: Color = LightGray
  override def tabbedPaneText: Color = Color.BLACK
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = DarkGray
  override def announceXHovered: Color = LightGrayOutline
  override def announceXPressed: Color = MediumGray
  override def announceRelease: Color = new Color(237, 205, 255)
  override def announceAdvisory: Color = new Color(255, 194, 154)
  override def announceEvent: Color = new Color(108, 202, 251)
  override def colorPickerOutputBackground: Color = new Color(125, 125, 125)
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(197, 197, 197)
  override def agentMonitorSeparator: Color = MediumGray

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Light
}

object DarkTheme extends ColorTheme(I18N.gui.get("menu.tools.themesManager.dark"), true, true) {
  override def widgetText: Color = Color.WHITE
  override def widgetTextError: Color = LightRed
  override def widgetHoverShadow: Color = new Color(75, 75, 75)
  override def widgetPreviewCover: Color = new Color(0, 0, 0, 85)
  override def widgetPreviewCoverNote: Color = new Color(100, 100, 100, 75)
  override def widgetHandle: Color = MediumGray
  override def displayAreaBackground: Color = Color.BLACK
  override def displayAreaText: Color = Color.WHITE
  override def scrollBarBackground: Color = new Color(40, 40, 40)
  override def scrollBarForeground: Color = DarkGray
  override def scrollBarForegroundHover: Color = LightGrayOutline
  override def interfaceBackground: Color = AlmostBlack
  override def commandCenterBackground: Color = BlueGray
  override def commandCenterText: Color = Color.WHITE
  override def locationToggleImage: Color = Color.WHITE
  override def commandOutputBackground: Color = DarkBlueGray
  override def splitPaneDividerBackground: Color = new Color(204, 204, 204)
  override def speedSliderBarBackground: Color = MediumGray
  override def speedSliderBarBackgroundFilled: Color = MediumBlue
  override def speedSliderThumb: Color = MediumBlue
  override def speedSliderThumbDisabled: Color = MediumGray
  override def buttonBackground: Color = MediumBlue
  override def buttonBackgroundHover: Color = new Color(42, 140, 255)
  override def buttonBackgroundPressed: Color = new Color(0, 58, 127)
  override def buttonBackgroundPressedHover: Color = new Color(0, 68, 149)
  override def buttonBackgroundDisabled: Color = new Color(213, 213, 213)
  override def buttonText: Color = Color.WHITE
  override def buttonTextPressed: Color = Color.WHITE
  override def buttonTextDisabled: Color = new Color(154, 154, 154)
  override def sliderBackground: Color = DarkBlue
  override def sliderBarBackground: Color = Color.BLACK
  override def sliderBarBackgroundFilled: Color = MediumBlue
  override def sliderThumbBorder: Color = MediumBlue
  override def sliderThumbBackground: Color = Color.WHITE
  override def sliderThumbBackgroundPressed: Color = MediumBlue
  override def switchBackground: Color = DarkBlue
  override def switchToggle: Color = Color.WHITE
  override def switchToggleBackgroundOn: Color = MediumBlue
  override def switchToggleBackgroundOff: Color = Color.BLACK
  override def chooserBackground: Color = DarkBlue
  override def chooserBorder: Color = MediumBlue
  override def inputBackground: Color = DarkBlue
  override def inputBorder: Color = MediumBlue
  override def viewBackground: Color = LightGrayOutline2
  override def viewBorder: Color = LightGrayOutline
  override def monitorBackground: Color = DarkGray
  override def monitorBorder: Color = LightGrayOutline
  override def plotBackground: Color = DarkGray
  override def plotBorder: Color = LightGrayOutline
  override def plotMouseBackground: Color = LightGray
  override def plotMouseText: Color = Color.BLACK
  override def outputBackground: Color = DarkGray
  override def outputBorder: Color = LightGrayOutline
  override def toolbarBackground: Color = BlueGray
  override def tabBackground: Color = DarkBlueGray
  override def tabBackgroundHover: Color = DarkBlueGray2
  override def tabBackgroundSelected: Color = MediumBlue
  override def tabBackgroundError: Color = LightRed
  override def tabText: Color = Color.WHITE
  override def tabTextSelected: Color = Color.WHITE
  override def tabTextError: Color = LightRed
  override def tabBorder: Color = LightGrayOutline
  override def tabSeparator: Color = LightGrayOutline
  override def tabCloseButtonBackgroundHover: Color = new Color(0, 0, 0, 64)
  override def toolbarText: Color = Color.WHITE
  override def toolbarTextSelected: Color = Color.WHITE
  override def toolbarControlBackground: Color = DarkBlueGray
  override def toolbarControlBackgroundHover: Color = DarkBlueGray2
  override def toolbarControlBackgroundPressed: Color = DarkBlueGray3
  override def toolbarControlBorder: Color = LightGrayOutline
  override def toolbarControlBorderSelected: Color = LightGrayOutline
  override def toolbarControlFocus: Color = Color.WHITE
  override def toolbarToolSelected: Color = MediumBlue2
  override def toolbarImage: Color = new Color(168, 170, 194)
  override def toolbarImageSelected: Color = LightGray2
  override def toolbarImageDisabled: Color = new Color(150, 150, 150, 64)
  override def toolbarSeparator: Color = LightGrayOutline
  override def infoBackground: Color = AlmostBlack
  override def infoH1Background: Color = new Color(10, 0, 199)
  override def infoH1Color: Color = new Color(205, 202, 255)
  override def infoH2Background: Color = new Color(0, 80, 177)
  override def infoH2Color: Color = new Color(221, 237, 255)
  override def infoH3Color: Color = new Color(173, 183, 196)
  override def infoH4Color: Color = new Color(173, 183, 196)
  override def infoPColor: Color = Color.WHITE
  override def infoCodeBackground: Color = new Color(67, 67, 67)
  override def infoCodeText: Color = Color.WHITE
  override def infoBlockBar: Color = MediumGray
  override def infoLink: Color = new Color(0, 110, 240)
  override def checkFilled: Color = new Color(0, 173, 90)
  override def errorLabelText: Color = Color.WHITE
  override def errorLabelBackground: Color = LightRed
  override def warningLabelText: Color = Color.WHITE
  override def warningLabelBackground: Color = new Color(255, 160, 0)
  override def errorHighlight: Color = LightRed
  override def codeBackground: Color = AlmostBlack
  override def codeLineHighlight: Color = new Color(35, 35, 35)
  override def codeBracketHighlight: Color = DarkGray
  override def codeSelection: Color = DarkGray
  override def codeSeparator: Color = DarkGray
  override def checkboxBackgroundSelected: Color = MediumBlue
  override def checkboxBackgroundSelectedHover: Color = MediumBlue2
  override def checkboxBackgroundUnselected: Color = DarkBlueGray
  override def checkboxBackgroundUnselectedHover: Color = DarkBlueGray2
  override def checkboxBackgroundDisabled: Color = LightGrayOutline
  override def checkboxBorder: Color = LightGrayOutline
  override def checkboxCheck: Color = Color.WHITE
  override def menuBarBorder: Color = MediumBlueGray
  override def menuBackground: Color = BlueGray
  override def menuBackgroundHover: Color = new Color(6, 112, 237, 128)
  override def menuBorder: Color = MediumGray
  override def menuTextHover: Color = Color.WHITE
  override def menuTextDisabled: Color = MediumGray
  override def dialogBackground: Color = BlueGray
  override def dialogBackgroundSelected: Color = new Color(6, 112, 237, 128)
  override def dialogText: Color = Color.WHITE
  override def dialogTextSelected: Color = Color.WHITE
  override def radioButtonBackground: Color = DarkBlueGray
  override def radioButtonBackgroundHover: Color = DarkBlueGray2
  override def radioButtonSelected: Color = MediumBlue
  override def radioButtonSelectedHover: Color = MediumBlue2
  override def radioButtonBorder: Color = LightGrayOutline
  override def primaryButtonBackground: Color = MediumBlue
  override def primaryButtonBackgroundHover: Color = MediumBlue3
  override def primaryButtonBackgroundPressed: Color = MediumBlue4
  override def primaryButtonBorder: Color = MediumBlue
  override def primaryButtonText: Color = Color.WHITE
  override def secondaryButtonBackground: Color = DarkBlueGray
  override def secondaryButtonBackgroundHover: Color = DarkBlueGray3
  override def secondaryButtonBackgroundPressed: Color = DarkBlueGray4
  override def secondaryButtonBorder: Color = MediumGray
  override def secondaryButtonText: Color = Color.WHITE
  override def textAreaBackground: Color = DarkBlueGray
  override def textAreaText: Color = Color.WHITE
  override def textAreaBorderEditable: Color = LightGray2
  override def textAreaBorderNoneditable: Color = LightGrayOutline
  override def tabbedPaneText: Color = Color.WHITE
  override def tabbedPaneTextSelected: Color = Color.WHITE
  override def infoIcon: Color = new Color(50, 150, 200)
  override def warningIcon: Color = new Color(220, 170, 50)
  override def errorIcon: Color = new Color(220, 50, 50)
  override def updateIcon: Color = new Color(240, 91, 0)
  override def stockBackground: Color = ClassicBeige
  override def converterBackground: Color = ClassicLightGreen
  override def announceX: Color = White2
  override def announceXHovered: Color = MediumGray
  override def announceXPressed: Color = DarkGray
  override def announceRelease: Color = new Color(137, 105, 155)
  override def announceAdvisory: Color = new Color(155, 94, 54)
  override def announceEvent: Color = new Color(8, 152, 121)
  override def colorPickerOutputBackground: Color = DarkBlueGray
  override def colorPickerCheckmark: Color = new Color(62, 184, 79)
  override def colorPickerCopyHover: Color = new Color(57, 57, 57)
  override def agentMonitorSeparator: Color = LightGray2

  override def colorizerTheme: ColorizerTheme = ColorizerTheme.Dark
}

trait ThemeSync {
  def syncTheme(): Unit
}
