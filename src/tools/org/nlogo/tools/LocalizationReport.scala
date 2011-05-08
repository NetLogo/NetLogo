package org.nlogo.tools

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
    if (args.length == 3) testLocalization(new Locale(args(0), args(1)), args(3).toBoolean)
    if (args.length == 2) testLocalization(new Locale(args(0), args(1)), false)
    else {
      println("Usage: testLocalization <language> <country> <verbose(boolean, optional)>")
      println("executing testLocalization('es','MX', false)")
      println()
      testLocalization(new Locale("es", "MX"), false)
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
            Some("Value must be one of: [\"Hello\" 5 [1 2 3]]"))
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
        def printWrapped[A](seq: {def size:Int}, message: String)(f: => Unit) {
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
          val exists = arg.bundle.keys(locale).contains(kaa.key)
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
                case e => Exploded(e)
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

      val keysMissingFromOtherLanguageFile = arg.bundle.keys(english) -- arg.bundle.keys(otherLanguageLocale)
      val extraKeysInOtherLanguageFile = arg.bundle.keys(otherLanguageLocale) -- arg.bundle.keys(english)

      Report(
        title, 
        keysGood, keysExploded, keysWrong, keysInTestMissingFromEnglishOrOtherLanguage,
        keysMissingFromOtherLanguageFile, extraKeysInOtherLanguageFile)
    }

    buildReport("Report for GUI_Strings", guiKeysAndArgsAndExpectations).printReport()
    buildReport("Report for Errors", errorKeysAndArgsAndExpectations).printReport()
  }
}
