// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{Token,TokenizerInterface,TokenType,VersionHistory}
import VersionHistory._  // olderThan* methods

// AutoConverter1 handles easy conversions that don't require parsing.
// AutoConverter2 handles hard conversions that do.
// This class was automatically converted from Scala to Java using a program called jatran.  I did
// some hand cleaning up of the code, but not that much, so beware. - ST 12/10/08
object AutoConverter1 {
  val clearAllAndResetTicks =
    ";; (for this model to work with NetLogo's new plotting features,\n" +
    "  ;; __clear-all-and-reset-ticks should be replaced with clear-all at\n" +
    "  ;; the beginning of your setup procedure and reset-ticks at the end\n" +
    "  ;; of the procedure.)\n" +
    "  __clear-all-and-reset-ticks"
}
class AutoConverter1(implicit tokenizer:TokenizerInterface) {
  def convert(originalSource:String, subprogram:Boolean, reporter:Boolean, version:String):String = {
    var source = originalSource
    if(source.trim.length == 0) return source
    if(olderThan20alpha1(version))
      source = convert(source, CONVERSIONS1)
    if(olderThan20beta5(version))
      source = convert(source, CONVERSIONS2)
    if(olderThan21beta3(version))
      source = convert(source, CONVERSIONS3)
    if(olderThan22pre3(version))
      source = convert(source, CONVERSIONS4)
    if(olderThan30pre5(version))
      source = convert(source, CONVERSIONS5)
    if(olderThan30beta1(version))
      source = convert(source, CONVERSIONS6)
    if(olderThan30beta2(version))
      source = convert(source, CONVERSIONS7)
    if(olderThan31pre1(version)) {
      source = convertBreeds(source)
      source = convertScreenEdge(source)
      source = convert(source, CONVERSIONS8)
    }
    if(olderThan31pre2(version))
      source = convert(source, CONVERSIONS9)
    if(olderThan31beta5(version))
      source = convert(source, CONVERSIONS11)
    if(olderThan32pre2(version)) {
      source = convert(source, CONVERSIONS13)
      source = convertCreateCustomBreed(source)
    }
    if(olderThan32pre3(version))
      source = convertNsum(source)
    if(olderThan3DPreview3(version)) {
      // do every thing since the branch
      source = convert(source, CONVERSIONS4)
      source = convert(source, CONVERSIONS5)
      source = convert(source, CONVERSIONS6)
      source = convert(source, CONVERSIONS7)
      source = convertBreeds(source)
      source = convertScreenEdge(source)
      source = convert(source, CONVERSIONS8)
      source = convert(source, CONVERSIONS9)
      source = convert(source, CONVERSIONS10)
      source = convert(source, CONVERSIONS11)
      source = convert(source, CONVERSIONS12)
      source = convert(source, CONVERSIONS13)
      source = convertCreateCustomBreed(source)
    }
    if(olderThan32pre4(version) || olderThan3DPreview4(version)) {
      source = convert(source, CONVERSIONS14)
      source = convertDashOf(source)
    }
    if(olderThan32pre5(version) || olderThan3DPreview4(version))
      source = convertOtherBreedHere(source)
    if(olderThan40pre1(version) || olderThan3DPreview4(version))
      source = convert(source, CONVERSIONS15)
    if(olderThan40pre3(version) || olderThan3DPreview4(version))
      source = convertLocals(source)
    if(olderThan40pre4(version) || olderThan3DPreview4(version))
      source = convert(source, CONVERSIONS16)
    if(olderThan40alpha3(version) || olderThan3DPreview5(version))
      source = convert(source, CONVERSIONS17)
    if(olderThan40beta2(version) || olderThan3DPreview5(version)) {
      source = convert(source, CONVERSIONS18)
      source = convertExtensionNames(source)
    }
    if(olderThan40beta4(version))
      source = convert(source, CONVERSIONS19)
    if(olderThan40beta5(version))
      source = convert(source, CONVERSIONS20)

    if(olderThan42pre1(version))
      source = convert(source, CONVERSIONS21)
    if(olderThan42pre5(version))
      source = convert(source, CONVERSIONS22)
    if(olderThan52(version))
      source = convert(source, CONVERSIONS23)

    source
  }
  private def convert(source:String,conversions:Map[String,String]) = {
    val tokens = tokenizer.tokenizeRobustly(source)
    val buf = new StringBuilder(source)
    var offset = 0
    for(token <- tokens; replacement <- conversions.get(token.name.toLowerCase())) {
      buf.delete(token.startPos + offset, token.endPos + offset)
      buf.insert(token.startPos + offset, replacement)
      offset += replacement.length - token.name.length
    }
    buf.toString
  }
  private def convertLocals(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator
    val buf:StringBuilder = new StringBuilder(source)
    var offset:Int = 0
    while(tokens.hasNext) {
      var token:Token = tokens.next()
      if(token.name.equalsIgnoreCase("locals")) {
        val start:Int = token.startPos
        var replacement:String = ""
        token = tokens.next()
        while(!token.name.equals("]")) {
          if(!token.name.equals("["))
            replacement += "let " + token.name + " 0\n  "
          token = tokens.next()
        }
        val end:Int = token.endPos
        buf.delete(start + offset, end + offset)
        buf.insert(start + offset, replacement)
        offset += replacement.length - (end - start)
      }
    }
    buf.toString
  }
  private def convertCreateCustomBreed(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator
    val buf = new StringBuilder(source)
    var offset = 0
    while(tokens.hasNext) {
      val token = tokens.next()
      val name = token.name.toUpperCase
      if(name.startsWith("CREATE-CUSTOM-") || name.startsWith("CCT-")) {
        val prefixLength = if(name.startsWith("CREATE-CUSTOM-")) 14 else 4
        val replacement = "create-" + token.name.substring(prefixLength)
        val start = token.startPos + offset
        val end = token.endPos + offset
        buf.delete(start, end)
        buf.insert(start, replacement)
        offset += replacement.length - (end - start)
      }
    }
    buf.toString
  }
  private def convertOtherBreedHere(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator
    val buf = new StringBuilder(source)
    while(tokens.hasNext) {
      val token = tokens.next()
      val name = token.name.toUpperCase
      if(name.startsWith("OTHER-") && name.endsWith("-HERE")) {
        buf.setCharAt(token.startPos + 5, ' ')
      }
    }
    buf.toString
  }
  private def convertScreenEdge(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    val buf = new StringBuilder(source)
    var offset = 0
    var lastToken = tokens.head
    var lastToken2 = tokens.head
    while(tokens.hasNext) {
      val token = tokens.next()
      if(token.name.startsWith("screen-edge-")) {
        var replacement:String = null
        var start = 0
        var end = 0
        // negative screen-edge
        if(lastToken.name == "-") {
          // parenthesis means that it's negative not subtracting so don't need to add the '+' sign.
          if(lastToken2.name == "(") {
            replacement = "screen-min-" + token.name.substring(12)
            // if it's enclosed we can get rid of them; perhaps this is unnecessary but we have to
            // check for them anyway so we might as well be neat.
            if(tokens.head.name == ")") {
              start = lastToken2.startPos + offset
              end = tokens.head.endPos + offset
            }
            else {
              start = lastToken.startPos + offset
              end = token.endPos + offset
            }
          }
          else {
            replacement = "+ screen-min-" + token.name.substring(12)
            start = lastToken.startPos + offset
            end = token.endPos + offset
          }
        }
        // subtracting from 0 also means it's screen min
        else if(lastToken2.name == "0" && lastToken.name == "-") {
          replacement = "screen-min-" + token.name.substring(12)
          start = lastToken2.startPos + offset
          end = token.endPos + offset
        }
        else {
          // even if I've not covered every single case above a simple replace with edge -> max will
          // not break old models
          replacement = "screen-max-" + token.name.substring(12)
          start = token.startPos + offset
          end = token.endPos + offset
        }
        buf.delete(start,end)
        buf.insert(start,replacement)
        offset += replacement.length - (end - start)
      }
      lastToken2 = lastToken
      lastToken = token
    }
    buf.toString
  }
  // We need to handle this a special case because it is more complicated than a simple string
  // replacement - jrn 8/8/05
  private def convertBreeds(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    val buf:StringBuilder = new StringBuilder(source)
    var offset:Int = 0
    while(tokens.hasNext) {
      var token:Token = tokens.next()
      if(token.tyype == TokenType.IDENT && token.value.asInstanceOf[String] .equals("BREEDS")) {
        val breeds = new collection.mutable.ArrayBuffer[String]
        val start:Int = token.startPos
        token = tokens.head
        if(token.tyype == TokenType.OPEN_BRACKET) {
          tokens.next()
          token = tokens.next()
          while(token.tyype != TokenType.CLOSE_BRACKET) {
            breeds += token.name
            token = tokens.next()
          }
          // we do it this way to remove all the spaces too
          buf.delete(start, token.endPos)
          var i = 0
          while(i < breeds.size) {
            var replacement:String = "breed [ " + breeds(i) + " ]\n"
            buf.insert(start + offset, replacement)
            offset += replacement.length
            i += 1
          }
        }
      }
    }
    buf.toString
  }
  // We need to handle this a special case because it is more complicated than a simple string
  // replacement - jrn 8/8/05
  private def convertExtensionNames(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator.buffered
    val buf:StringBuilder = new StringBuilder(source)
    var offset:Int = 0
    while(tokens.hasNext) {
      var token:Token = tokens.next()
      if(token.tyype == TokenType.IDENT && token.value.asInstanceOf[String] .equals("__EXTENSIONS")) {
        val extensions = new collection.mutable.ArrayBuffer[String]
        val start:Int = token.startPos
        token = tokens.head
        if(token.tyype == TokenType.OPEN_BRACKET) {
          tokens.next()
          token = tokens.next()
          while(token.tyype != TokenType.CLOSE_BRACKET) {
            extensions += token.name
            token = tokens.next()
          }
          // we do it this way to remove all the spaces too
          buf.delete(start, token.endPos)
          var replacement:String = "extensions [ "
          var i:Int = 0
          while(i < extensions.size) {
            var name:String = extensions(i)
            // trim off trailing .jar and double quote
            if(name.endsWith(".jar\"")) name = name.substring(0, name.length - 5)
            // trim off leading double quote
            if(name.startsWith("\"")) name = name.substring(1)
            replacement += name + " "
            i += 1
          }
          replacement += "]"
          buf.insert(start + offset, replacement)
          offset += replacement.length
        }
      }
    }
    buf.toString
  }
  // We need to handle this a special case because it is more complicated than a simple string
  // replacement - ST 6/22/06
  private def convertNsum(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator
    val buf:StringBuilder = new StringBuilder(source)
    var offset:Int = 0
    while(tokens.hasNext) {
      var token:Token = tokens.next()
      if(token.tyype == TokenType.IDENT && (token.value.asInstanceOf[String] .equals("NSUM") || token.value.asInstanceOf[String] .equals("NSUM4"))) {
        val neighbors:String = if(token.value.asInstanceOf[String] .equals("NSUM")) "neighbors" else "neighbors4"
        var start:Int = token.startPos + offset
        var end:Int = token.endPos + offset
        val replacement:String = "sum values-from " + neighbors
        buf.delete(start, end)
        buf.insert(start, replacement)
        offset += replacement.length - (end - start)
        // The logic here won't work if the user put parentheses around the variable name, but let's
        // just cross our fingers and hope almost nobody did that... - ST 6/22/06
        token = tokens.next()
        start = token.startPos + offset
        end = token.endPos + offset
        buf.insert(start, "[")
        buf.insert(end + 1, "]")
        offset += 2
      }
    }
    buf.toString
  }
  // We need to handle this a special case because it is more complicated than a simple string
  // replacement - ST 6/28/06
  private def convertDashOf(source:String):String = {
    val tokens = tokenizer.tokenizeRobustly(source).iterator
    val buf:StringBuilder = new StringBuilder(source)
    var offset:Int = 0
    while(tokens.hasNext) {
      var token:Token = tokens.next()
      if(token.tyype == TokenType.IDENT && token.value.asInstanceOf[String] .endsWith("-OF")) {
        var name:String = token.name
        name = name.substring(0, name.length - 3)
        val start:Int = token.startPos + offset
        val end:Int = token.endPos + offset
        val replacement:String = "[" + name + "] of"
        buf.delete(start, end)
        buf.insert(start, replacement)
        offset += replacement.length - (end - start)
      }
    }
    buf.toString
  }
  private val CONVERSIONS1 = Map("pc" -> "pcolor",
                                 "pc-of" -> "[pcolor] of",
                                 "histogram" -> "histogram-from",
                                 "set-plot-pen" -> "create-temporary-plot-pen",
                                 "random" -> "random-or-random-float")
  private val CONVERSIONS2 = Map("any" -> "any?",
                                 "user-yes-or-no" -> "user-yes-or-no?")
  private val CONVERSIONS3 = Map("cc" -> "clear-output")
  private val CONVERSIONS4 = Map("pen-down?" -> "pen-mode != \"up\"")
  private val CONVERSIONS5 = Map("cg" -> "cp ct",
                                 "clear-graphics" -> "cp ct",
                                 "export-graphics" -> "export-view",
                                 "movie-grab-graphics" -> "movie-grab-view")
  private val CONVERSIONS6 = Map("stamp" -> "set pcolor")
  private val CONVERSIONS7 = Map("get-date-and-time" -> "date-and-time")
  private val CONVERSIONS8 = Map("screen-max-x" -> "max-pxcor",
                                 "screen-min-x" -> "min-pxcor",
                                 "screen-max-y" -> "max-pycor",
                                 "screen-min-y" -> "min-pycor",
                                 "screen-size-x" -> "world-width",
                                 "screen-size-y" -> "world-height",
                                 "no-label" -> "\"\"")
  private val CONVERSIONS9 = Map("random-one-of" -> "one-of",
                                 "random-n-of" -> "n-of")
  private val CONVERSIONS10 = Map("screen-min-z" -> "min-pzcor",
                                  "screen-max-z" -> "max-pzcor",
                                  "screen-size-z" -> "world-depth",
                                  "shape3d" -> "shape",
                                  "observe" -> "reset-perspective")
  private val CONVERSIONS11 = Map("user-choose-directory" -> "user-directory",
                                  "user-choose-file" -> "user-file",
                                  "user-choose-new-file" -> "user-new-file",
                                  "user-choice" -> "user-one-of")
  private val CONVERSIONS12 = Map("left" -> "set heading heading -",
                                  "right" -> "set heading heading +",
                                  "left3d" -> "left",
                                  "right3d" -> "right",
                                  "pitch-up" -> "set pitch pitch +",
                                  "pitch-down" -> "set pitch pitch -",
                                  "pitch-up3d" -> "tilt-up",
                                  "pitch-down3d" -> "tilt-down",
                                  "diffuse3d" -> "diffuse",
                                  "at-points3d" -> "at-points",
                                  "set-gaze-xyz" -> "facexyz",
                                  "patch-ahead3d" -> "patch-ahead",
                                  "screen-size-z" -> "world-depth",
                                  "patch-left3d-and-ahead" -> "patch-left-and-ahead",
                                  "patch-right3d-and-ahead" -> "patch-right-and-ahead",
                                  "patch3d" -> "patch",
                                  "patch-at3d" -> "patch-at",
                                  "turtles-at3d" -> "turtles-at")
  private val CONVERSIONS13 = Map("cct" -> "crt",
                                  "create-custom-turtles" -> "crt")
  private val CONVERSIONS14 = Map("histogram-list" -> "histogram",
                                  "random-int-or-float" -> "random-or-random-float")
  private val CONVERSIONS15 = Map("showturtle" -> "show-turtle",
                                  "hideturtle" -> "hide-turtle",
                                  "__both-ends" -> "both-ends",
                                  "__create-link-from" -> "create-link-from",
                                  "__create-links-from" -> "create-links-from",
                                  "__create-link-to" -> "create-link-to",
                                  "__create-links-to" -> "create-links-to",
                                  "__create-link-with" -> "create-link-with",
                                  "__create-links-with" -> "create-links-with",
                                  "__end1" -> "end1",
                                  "__end2" -> "end2",
                                  "__in-link-neighbor?" -> "in-link-neighbor?",
                                  "__in-link-neighbors" -> "in-link-neighbors",
                                  "__in-link-from" -> "in-link-from",
                                  "__is-link?" -> "is-link?",
                                  "__layout-circle" -> "layout-circle",
                                  "__layout-magspring" -> "layout-magspring",
                                  "__layout-radial" -> "layout-radial",
                                  "__layout-spring" -> "layout-spring",
                                  "__layout-tutte" -> "layout-tutte",
                                  "__link-neighbor?" -> "link-neighbor?",
                                  "__link-neighbors" -> "link-neighbors",
                                  "__link-with" -> "link-with",
                                  "__my-links" -> "my-links",
                                  "__my-in-links" -> "my-in-links",
                                  "__my-out-links" -> "my-out-links",
                                  "__other-end" -> "other-end",
                                  "__out-link-neighbor?" -> "out-link-neighbor?",
                                  "__out-link-neighbors" -> "out-link-neighbors",
                                  "__out-link-to" -> "out-link-to",
                                  "__remove-link-from" -> "remove-link-from",
                                  "__remove-links-from" -> "remove-links-from",
                                  "__remove-link-to" -> "remove-link-to",
                                  "__remove-links-to" -> "remove-links-to",
                                  "__remove-link-with" -> "remove-link-with",
                                  "__remove-links-with" -> "remove-links-with")
  private val CONVERSIONS16 = Map("plot-point" -> "plotxy",
                                  "message" -> "user-message",
                                  "showturtle" -> "show-turtle",
                                  "hideturtle" -> "hide-turtle",
                                  "face-no-wrap" -> "face-nowrap",
                                  "facexy-no-wrap" -> "facexy-nowrap",
                                  "distance-no-wrap" -> "distance-nowrap",
                                  "distancexy-no-wrap" -> "distancexy-nowrap",
                                  "in-cone-no-wrap" -> "in-cone-nowrap",
                                  "in-radius-no-wrap" -> "in-radius-nowrap",
                                  "towards-no-wrap" -> "towards-nowrap",
                                  "towardsxy-no-wrap" -> "towardsxy-nowrap",
                                  "agent?" -> "is-agent?",
                                  "agentset?" -> "is-agentset?",
                                  "boolean?" -> "is-boolean?",
                                  "list?" -> "is-list?",
                                  "number?" -> "is-number?",
                                  "patch?" -> "is-patch?",
                                  "string?" -> "is-string?",
                                  "turtle?" -> "is-turtle?")
  private val CONVERSIONS17 = Map("output" -> "report",
                                  "patch-agentset?" -> "is-patch-set?",
                                  "is-patch-agentset?" -> "is-patch-set?",
                                  "turtle-agentset?" -> "is-turtle-set?",
                                  "is-turtle-agentset?" -> "is-turtle-set?",
                                  "is-link-agentset?" -> "is-link-set?")
  private val CONVERSIONS18 = Map("ppd" -> "plot-pen-down",
                                  "ppu" -> "plot-pen-up",
                                  "__hubnet-ppd" -> "__hubnet-plot-pen-down",
                                  "__hubnet-ppu" -> "__hubnet-plot-pen-up")
  private val CONVERSIONS19 = Map("rgb" -> "approximate-rgb",
                                  "hsb" -> "approximate-hsb")
  private val CONVERSIONS20 = Map("__tie" -> "tie",
                                  "__untie" -> "untie")

  private val CONVERSIONS21 = Map("setup-plots" -> "my-setup-plots",
                                  "update-plots" -> "my-update-plots")

  private val CONVERSIONS22 = Map("clear-all" -> AutoConverter1.clearAllAndResetTicks,
                                  "ca" -> AutoConverter1.clearAllAndResetTicks)

  private val CONVERSIONS23 = Map("hsb" -> "__hsb-old",
                                  "extract-hsb" -> "__extract-hsb-old",
                                  "approximate-hsb" -> "__approximate-hsb-old")
}
