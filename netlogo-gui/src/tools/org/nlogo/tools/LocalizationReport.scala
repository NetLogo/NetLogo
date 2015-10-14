// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

import java.util.Locale
import org.nlogo.api.I18N
import org.nlogo.api.I18N.BundleKind

/**
 * this code is a bit messy, but its working.
 * one thing that is missing:
 *   it might be nice to test the english version of every key
 *   we arent doing that right now.
 *   just to make sure it doesnt' completely bomb.
 */
object LocalizationReport {
  def main(args: Array[String]) {
    if (args.length == 1) testLocalization(new Locale(args(0)), false)
    if (args.length == 2) testLocalization(new Locale(args(0), args(1)), false)
    if (args.length == 3) testLocalization(new Locale(args(0), args(1)), args(3).toBoolean)
    else {
      println("Usage: testLocalization <language(optional, default es)> <country(optional)> <verbose(boolean, optional)>")
      println("executing testLocalization('es', false)")
      println()
      testLocalization(new Locale("es"), false)
    }
  }

  // these classes are used to build up our tests
  // they contain:
  //   the bundle (gui, errors, maybe more later, or maybe we just end up with one big bundle)
  //   the key (which we expect to be in the bundle file)
  //   the arguments that are going to be used when building the result
  //     ex: if the key has the value "my name is {1}", then this would have one argument ("Josh")
  //   and what we expect to get back from the english translation
  //     this is optional right now. it might be too tedious to make it mandatory.
  case class KeyAndArgs(key: String, args: AnyRef*)
  type KeysAndArgsAndExpectations = List[(KeyAndArgs, Option[String])]
  case class BundleAndKeysAndArgsAndExpectations(bundle: BundleKind, kaaaes: KeysAndArgsAndExpectations)

  // here is the actual test data
  // this is for the strings in the GUI_Strings files.
  val guiKeysAndArgsAndExpectations = BundleAndKeysAndArgsAndExpectations(I18N.gui, List(
    (KeyAndArgs("common.turtle"), Some("turtle")),
    (KeyAndArgs("common.patch"), Some("patch")),
    (KeyAndArgs("common.link"), Some("link")),
    (KeyAndArgs("common.turtles"), Some("turtles")),
    (KeyAndArgs("common.patches"), Some("patches")),
    (KeyAndArgs("common.links"), Some("links"))

    // just some intentional errors for demonstration
    // (KeyAndArgs("common.turtle"), Some("tortoise")),
    // (KeyAndArgs("common.wefwefwef"), None)
  ))

  // this is for the strings in the Error files.
  val errorKeysAndArgsAndExpectations = BundleAndKeysAndArgsAndExpectations(I18N.errors, List(
  (KeyAndArgs("org.nlogo.agent.Agent.wrongTypeOnSetError", "turtle", "XCOR", "number", "foo"),
            Some("can't set turtle variable XCOR to non-number foo")),
  (KeyAndArgs("org.nlogo.agent.ImportPatchColors.unsupportedImageFormat", "image.jpg"),
            Some("The following file is not in a supported image format: image.jpg")),
  (KeyAndArgs("org.nlogo.agent.ChooserConstraint.invalidValue", "[\"Hello\" 5 [1 2 3]]"),
            Some("Value must be one of: [\"Hello\" 5 [1 2 3]]")),
  //Dhrumil's Additions
  (KeyAndArgs("org.nlogo.prim._lessthan.cantUseLessthanOnDifferentArgs","a turtle","a patch"),
            Some("The < operator can only be used on two numbers, two strings, or two agents of the same type, but not on a turtle and a patch.")),
  (KeyAndArgs("org.nlogo.agent.Agent.notADoubleVariable", "2"),
            Some("2 is not a double variable.")),
  (KeyAndArgs("org.nlogo.agent.Agent.breedDoesNotOwnVariable", "sheep", "cheese"),
            Some("sheep breed does not own variable cheese")),
  (KeyAndArgs("org.nlogo.agent.Agent.shapeUndefined", "POTATO"),
            Some("\" POTATO \" is not a currently defined shape.")),
  (KeyAndArgs("org.nlogo.agent.Protractor.noHeadingFromPointToSelf", "0.0", "0.0"),
            Some("No heading is defined from a point (0.0,0.0) to that same point.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.noNegativeRadius", "IN-CONE"),
            Some("IN-CONE cannot take a negative radius.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.noNegativeAngle", "IN-CONE"),
            Some("IN-CONE cannot take a negative angle.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.noAngleGreaterThan360","IN-CONE"),
            Some("IN-CONE cannot take an angle greater than 360.")),
  (KeyAndArgs("org.nlogo.prim.etc._atpoints.invalidListOfPoints","[[0 0 0]]"),
            Some("Invalid list of points: [[0 0 0]]")),
  (KeyAndArgs("org.nlogo.prim.etc._setxy.pointOutsideWorld", "0", "1"),
            Some("The point [ 0 , 1 ] is outside of the boundaries of the world and wrapping is not permitted in one or both directions.")),
  (KeyAndArgs("org.nlogo.prim.etc._sqrt.squareRootIsImaginary", "-20"),
            Some("The square root of -20 is an imaginary number.")),
  (KeyAndArgs("org.nlogo.prim.etc._standarddeviation.needListGreaterThanOneItem", "[1]"),
            Some("Can't find the standard deviation of a list without at least two numbers: [1]")),
  (KeyAndArgs("org.nlogo.prim.etc._setDefaultShape.notADefinedTurtleShape", "borgwestern"),
            Some("\"borgwestern\" is not a currently defined turtle shape.")),
  (KeyAndArgs("org.nlogo.prim._greaterthan.cannotCompareParameters", "a turtle", "a patch"),
            Some("The > operator can only be used on two numbers, two strings, or two agents of the same type, but not on a turtle and a patch.")),
  (KeyAndArgs("org.nlogo.prim._max.cantFindMaxOfListWithNoNumbers", "[]"),
            Some("Can't find the maximum of a list with no numbers: []")),
  (KeyAndArgs("org.nlogo.prim._mean.cantFindMeanOfNonNumbers", "true", "TRUE/FALSE"),
            Some("Can't find the mean of a list that contains non-numbers : true is a TRUE/FALSE.")),
  (KeyAndArgs("org.nlogo.prim._min.cantFindMinOfListWithNoNumbers", "[]"),
            Some("Can't find the minimum of a list with no numbers: []")),
  (KeyAndArgs("org.nlogo.prim._greaterorequal.cannotCompareParameters", "a turtle", "a patch"),
            Some("The >= operator can only be used on two numbers, two strings, or two agents of the same type, but not on a turtle and a patch.")),
  (KeyAndArgs("org.nlogo.prim._lessorequal.cannotCompareParameters", "a turtle", "a patch"),
            Some("The <= operator can only be used on two numbers, two strings, or two agents of the same type, but not on a turtle and a patch.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.cantTakeLogarithmOf", "-3"),
            Some("Can't take logarithm of -3.")),
  (KeyAndArgs("org.nlogo.prim.etc._log.notAValidBase", "-3"),
            Some("-3 isn't a valid base for a logarithm.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.emptyListInput", "ONE-OF"),
            Some("ONE-OF got an empty list as input.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.emptyStringInput", "BUTFIRST"),
            Some("BUTFIRST got an empty String as input.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.firstInputCantBeNegative", "N-OF"),
            Some("First input to N-OF can't be negative.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.requestMoreItemsThanInList", "4", "3"),
            Some("Requested 4 random items from a list of length 3.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.negativeIndex", "-1"),
            Some("-1 isn't greater than or equal to zero.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.indexExceedsListSize", "5","[1 2 3 4]","4"),
            Some("Can't find element 5 of the list [1 2 3 4] which is only of length 4.")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.indexExceedsStringSize", "5","ohno","4"),
            Some("Can't find element 5 of the String ohno which is only of length 4.")),
  (KeyAndArgs("org.nlogo.prim.$common.paramOutOfBounds", "3.0"),
            Some("3.0 is not in the range 0.0 to 1.0")),
  (KeyAndArgs("org.nlogo.prim.etc._linkset.invalidListInputs", "LINK-SET", "[2]", "2"),
            Some("List inputs to LINK-SET must only contain link, link agentset, or list elements.  The list [2] contained 2 which is NOT a link or link agentset.")),
  (KeyAndArgs("org.nlogo.prim.etc._linkset.invalidLAgentsetTypeInputToList", "LINK-SET", "[turtles]", "turtle"),
            Some("List inputs to LINK-SET must only contain link, link agentset, or list elements.  The list [turtles] contained a different type agentset: turtle.")),
  (KeyAndArgs("org.nlogo.prim.etc.median.cantFindMedianOfListWithNoNumbers", "[]"),
            Some("Can't find the median of a list with no numbers: [].")),
  (KeyAndArgs("org.nlogo.prim.etc._sublist.startIsLessThanZero", "-2"),
            Some("-2 is less than zero.")),
  (KeyAndArgs("org.nlogo.prim.etc._sublist.endIsLessThanStart", "2", "4"),
            Some("2 is less than 4.")),
  (KeyAndArgs("org.nlogo.prim.etc._sublist.endIsGreaterThanListSize","4", "2"),
            Some("4 is greater than the length of the input list (2).")),
  (KeyAndArgs("org.nlogo.prim.etc._substring.endIsGreaterThanListSize","33","hello","5"),
            Some("33 is too big for hello, which is only of length 5.")),
  (KeyAndArgs("org.nlogo.prim.etc._patchset.listInputNonPatch", "PATCH-SET", "[(turtle 1) (turtle 2)]", "(turtle 1)"),
            Some("List inputs to PATCH-SET must only contain patch, patch agentset, or list elements.  The list [(turtle 1) (turtle 2)] contained (turtle 1) which is NOT a patch or patch agentset.")),
  (KeyAndArgs("org.nlogo.prim.etc._patchset.listInputNonPatchAgentset", "PATCH-SET", "[(patch 1 1) turtles]", "turtles"),
            Some("List inputs to PATCH-SET must only contain patch, patch agentset, or list elements.  The list [(patch 1 1) turtles] contained a different type agentset: turtles.")),
  (KeyAndArgs("org.nlogo.prim.etc._turtleset.incorrectInputType", "TURTLE-SET", "[(patch 1 1) turtles]", "(patch 1 1)"),
            Some("List inputs to TURTLE-SET must only contain turtle or turtle agentset elements.  The list [(patch 1 1) turtles] contained (patch 1 1) which is NOT a turtle or turtle agentset.")),
  (KeyAndArgs("org.nlogo.prim.etc._variance.listMustHaveMoreThanOneNumber","[1]"),
            Some("Can't find the variance of a list without at least two numbers: [1].")),
  (KeyAndArgs("org.nlogo.prim.$common.withExpectedBooleanValue", "(turtle 0)", "(turtle 1)"),
            Some("WITH expected a true/false value from (turtle 0), but got (turtle 1) instead.")),
  (KeyAndArgs("org.nlogo.prim.$common.expectedBooleanValue", "COUNT", "(turtle 0)", "(turtle 1)"),
            Some("COUNT expected a true/false value from (turtle 0), but got (turtle 1) instead.")),
  (KeyAndArgs("org.nlogo.prim.etc._turtleset.listInputsMustBeTurtleOrTurtleAgentset", "TURTLE-SET", "[(patch 1 1) turtles]", "(patch 1 1)"),
            Some("List inputs to TURTLE-SET must only contain turtle or turtle agentset elements.  The list [(patch 1 1) turtles] contained a different type agentset: (patch 1 1).")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.notThatManyAgentsExist", "4", "2"),
            Some("Requested 4 random agents from a set of only 2 agents.")),
  (KeyAndArgs("org.nlogo.prim.etc._otherend.incorrectLink", "(turtle 1)", "(link 7 8)"),
            Some("(turtle 1) is not linked by (link 7 8).")),
  (KeyAndArgs("org.nlogo.prim.etc.$common.syntaxError", "Nothing named REM has been defined error while observer running RUN"),
            Some("Syntax Error: Nothing named REM has been defined error while observer running RUN")),
  (KeyAndArgs("org.nlogo.prim.etc._stop.notAllowedInsideToReport", "STOP"),
            Some("STOP is not allowed inside TO-REPORT.")),
  (KeyAndArgs("org.nlogo.prim.$common.noSumOfListWithNonNumbers", "A", "String"),
            Some("Can't find the sum of a list that contains non-numbers A is a String."))
  ))

  // this is the main function of this class. it runs everything.
  def testLocalization(otherLanguageLocale: Locale, verbose: Boolean) {
    val english = new Locale("en", "US")
    trait TranslationStatus
    case class Good(result: String) extends TranslationStatus
    case class Exploded(e: Throwable) extends TranslationStatus
    case class Wrong(result: String) extends TranslationStatus
    case class Missing(key: String) extends TranslationStatus
    type TranslationReport = ((KeyAndArgs, Option[String]), TranslationStatus, TranslationStatus)

    // A report contains all the data needed to print out a report
    // as long as the ability to print it.
    case class Report(title:String,
                      keysGood: List[TranslationReport],
                      keysExploded: List[TranslationReport],
                      keysWrong: List[TranslationReport],
                      keysInTestMissingFromEnglishOrOtherLanguage: List[TranslationReport],
                      keysInEnglishFileMissingFromOtherLanguageFile: Set[String],
                      extraKeysInOtherLangugeFileMissingFromEnglishFile: Set[String]) {
      // print all the data in this report.
      // the code isn't that difficult to follow
      def printReport() {
        def printWrapped[A](seq: scala.collection.TraversableOnce[A], message: String)(f: => Unit) {
          if(seq.size > 0 || verbose){
            println("======= " + seq.size + " " + message + " =======")
            f
            println()
          }
        }
        def printKeys(message: String, keys: List[TranslationReport]) {
          printWrapped(keys, message){
            for (((kaa, exp), engRes, otherRes) <- keys) {
              println("translated '" + kaa.key + "' with args: (" + kaa.args.mkString(",") + ")")
              if (exp.isDefined) println("\texpected english: " + exp.get)
              println("\t(en,US): " + engRes)
              println("\t" + (otherLanguageLocale.getLanguage, otherLanguageLocale.getCountry) + ": " + otherRes)
            }
          }
        }

        val totalErrors = keysExploded.size +
                keysWrong.size +
                keysInTestMissingFromEnglishOrOtherLanguage.size +
                keysInEnglishFileMissingFromOtherLanguageFile.size +
                extraKeysInOtherLangugeFileMissingFromEnglishFile.size
        println("==============================================================")
        println(title + " (" + totalErrors + " errors)")
        println("==============================================================\n")
        if (verbose) {printKeys("KEYS TRANSLATED SUCCESSFULLY", keysGood)}
        printKeys("KEYS TRANSLATED WITH ERROR", keysExploded)
        printKeys("KEYS TRANSLATED INCORRECTLY", keysWrong)
        printKeys("KEYS IN TEST BUT MISSING FROM FILES", keysInTestMissingFromEnglishOrOtherLanguage)

        printWrapped(keysInEnglishFileMissingFromOtherLanguageFile,
          "ENGLISH KEYS MISSING FROM (" + otherLanguageLocale + ")"){
          keysInEnglishFileMissingFromOtherLanguageFile.foreach(k => println("\t" + k))
        }

        printWrapped(extraKeysInOtherLangugeFileMissingFromEnglishFile,
          "EXTRA KEYS (IN ENGLISH FILE BUT MISSING FROM (" + otherLanguageLocale + ")"){
          extraKeysInOtherLangugeFileMissingFromEnglishFile.foreach(k => println("\t" + k))
        }
      }
    }

    def buildReport(title: String, arg: BundleAndKeysAndArgsAndExpectations) = {
      def resultsForSingleTestKey(kaa: KeyAndArgs, expectedEnglishResult: Option[String]) = {
        def testKey(locale: Locale, kaa: KeyAndArgs) = {
          val exists = arg.bundle.keys(locale).toSeq.contains(kaa.key)
          // if the key is not in the file, well, then its missing. simple as that.
          if (!exists) Missing(kaa.key)
          else {
            // otherwise, we go ahead and try to get the value
            // by applying the aruments
            arg.bundle.withLanguage(locale){
              try {
                val translation = arg.bundle.getN(kaa.key, kaa.args: _*)
                // check to see if the result contains any unapploed arguments (this form {N})
                // if it does...well, thats funny. either we didnt apply enough arguments/
                // or something else just went wrong, but no exception was thrown.
                val funny = (0 to 10).exists(i => translation.contains("{" + i + "}"))
                // if its not funny, its good.
                if (funny) Wrong(translation) else Good(translation)
              } catch {
                // an exception was thrown trying to get the value of this key.
                case e: Throwable => Exploded(e)
              }
            }
          }
        }
        // get the english value for the key (with its aruments filled in, if it has any)
        val englishResult = {
          val e = testKey(english, kaa)
          e match {
          // for english, check the results against what we expected
          // (but only if we decided to put in an expected value for this key)
            case Good(t) => if (expectedEnglishResult.isDefined && t != expectedEnglishResult.get) Wrong(t) else e
            case _ => e
          }
        }
        // get the value for the key for the other language (with its aruments filled in, if it has any)
        val otherLanguageResult = testKey(otherLanguageLocale, kaa)
        // we return all the information we have
        ((kaa, expectedEnglishResult), englishResult, otherLanguageResult)
      }

      val resultsForEachTestKey =
        for ((kaa, expectedEnglishResult) <- arg.kaaaes) yield resultsForSingleTestKey(kaa, expectedEnglishResult)

      // every translation that passed for both english and the other language
      val keysGood = resultsForEachTestKey.collect{ case g@(_, Good(_), Good(_)) => g }
      // every translation that threw an exception in either enlish or the other language
      val keysExploded = resultsForEachTestKey.collect{
        case ex@(_, Exploded(e), _) => ex
        case ex@(_, _, Exploded(e)) => ex
      }
      // every translation (in either enlish or the other language) that after processing still had
      // {N} in it, for N=0..10
      // OR
      // if the expected english was not equal to the actual english result
      val keysWrong = resultsForEachTestKey.collect {
        case w@(_, Wrong(t), _) => w
        case w@(_, _, Wrong(t)) => w
      }
      // these are the ones that were specified in the test
      // it could be that they are missing from the english file
      // but were specified in the test
      val keysInTestMissingFromEnglishOrOtherLanguage = resultsForEachTestKey.collect {
        case m@(_, Missing(_), _) => m
        case m@(_, _, Missing(_)) => m
      }

      val keysMissingFromOtherLanguageFile: Set[String] =
        arg.bundle.keys(english).collect{case s: String => s}.toSet.filterNot(arg.bundle.keys(otherLanguageLocale).toSet.contains)
      val extraKeysInOtherLanguageFile: Set[String] =
        arg.bundle.keys(otherLanguageLocale).collect{case s: String => s}.toSet.filterNot(arg.bundle.keys(english).toList.contains)

      Report(
        title,
        keysGood, keysExploded, keysWrong, keysInTestMissingFromEnglishOrOtherLanguage,
        keysMissingFromOtherLanguageFile, extraKeysInOtherLanguageFile)
    }

    buildReport("Report for GUI_Strings", guiKeysAndArgsAndExpectations).printReport()
    buildReport("Report for Errors", errorKeysAndArgsAndExpectations).printReport()
  }
}
