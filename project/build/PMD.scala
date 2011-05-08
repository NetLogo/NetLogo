import sbt._

trait PMD extends DefaultProject {

  lazy val pmd = task { run(core) }
  lazy val pmd1 = task { args => task { run(args) } }

  ///
  /// CONFIGURATION
  ///

  // these should pass with no problems found (one per line, easy to comment/uncomment)
  private val core = List(
    "basic",
    "braces",
    "clone",
    "controversial",
    "design",
    "finalizers",
    "strictexception",
    "strings",
    "unusedcode",
    "migrating_to_15" // "imports" -- let's just fix imports from time to time in Eclipse, rather than get yelled at by Bitten - ST 8/8/08
    // "typeresolution" -- causes internal PMD errors. does give useful info also though - ST 3/10/08
    )

  // these rulesets would take some work to pass: "codesize", "coupling", "javabeans",
  // "logging-java", "naming", "optimizations", "sunsecure"

  // the following causes some of pmd's complaints to be ignored.  you can exempt an entire file from
  // a ruleset using exempt().  you can also ignore a particular rule in a ruleset for all files,
  // using ignore().

  // Note that there is another method of ignoring complaints, which is to add a "// NOPMD" comment to
  // the line of code which is generating the complaint.  Unfortunately this turns off all rulesets
  // for that line, rather than being able to specify a particular ruleset or rule, but hey, it's only
  // for one line of code, so it's pretty localized.

  exempt("braces",
    "agent/ImportLexer",
    "lex/TokenLexer",
    "swing/VTextIcon",
    "agent/Gamma",
    "util/MersenneTwisterFast")
  ignore("clone",
    "clone\\(\\) method should throw CloneNotSupportedException")
  ignore("controversial",
    "A method should have only one exit point",
    "Assigning an Object to null is a code smell",
    "Each class should declare at least one constructor",
    "It is a good practice to call super\\(\\) in a constructor",
    "Use bitwise inversion to invert boolean values",
    "Use explicit scoping instead of the default package private level",
    "Found 'DD'-anomaly",
    "Found 'DU'-anomaly",
    "Found 'UR'-anomaly",
    "Avoid using final local variables, turn them into fields",
    "The use of native code is not recommended.",
    "Do not use the short type")
  exempt("controversial",
    "agent/ImportLexer",
    "agent/ImportLexerOld",
    "lex/TokenLexer",
    "util/MersenneTwisterFast")
  ignore("design",
    "A switch with less than 3 branches is inefficient, use a if statement instead.",
    "Caught exception is rethrown, original stack trace may be lost",
    "An empty method in an abstract class should be abstract instead",
    "Document empty method",
    "Avoid calls to overridable methods during construction",
    "Avoid if \\(x != y\\) ..; else ..;",
    "Avoid reassigning parameters",
    "When doing a String.toLowerCase\\(\\)/toUpperCase\\(\\) call, use a Locale",
    "Position literals first in String comparisons",
    "Class cannot be instantiated and does not provide any static methods",
    "Use block level rather than method level synchronization",
    "Overridable method '.*' called during object construction",
    "Use equals\\(\\) to compare object references.",
    "This abstract class does not have any abstract methods",
    "Ensure that resources like this Statement object are closed after use", // what is PMD smoking
    "Overridable constructor called during object construction",
    "Deeply nested if..then statements are hard to read")
  exempt("design",
    "agent/ImportLexer",
    "agent/ImportLexerOld",
    "lex/TokenLexer")
  ignore("imports",
    "Too many static imports may lead to messy code")
  exempt("strictexception",
    "agent/ImportLexer",
    "agent/ImportLexerOld",
    "lex/TokenLexer",
    "util/MersenneTwisterFast")
  ignore("strings",
    "The String literal .* appears .* times in this file")

  ///
  /// IMPLEMENTATION
  ///

  private lazy val ignored = new collection.mutable.HashMap[String, Set[String]]
  private def ignore(ruleset: String, rules: String*) {
    ignored(ruleset) = Set() ++ rules
  }

  private lazy val exempted = new collection.mutable.HashMap[String, Set[String]]
  private def exempt(ruleset: String, files: String*) {
    exempted(ruleset) = Set() ++ files
  }

  // maybe it's possible to get the output of PMD without restoring to capturing stdout
  // like this, but oh well - ST 3/14/11
  private def captureOutput(body: => Unit): Iterator[String] = {
    val old = System.out
    val stream = new java.io.ByteArrayOutputStream
    System.setOut(new java.io.PrintStream(stream))
    try { body }
    finally System.setOut(old)
    stream.toString("UTF-8").split("\n").elements
  }
  
  private def violations(src: String, ruleset: String): Iterator[String] = {
    import net.sourceforge.pmd.PMD
    val args = Array(src, "text", "rulesets/" + ruleset + ".xml", "-targetjdk", "1.5")
    for {
      line <- captureOutput(PMD.main(args))
      if !line.matches("""\s*""") // skip empty lines
      if !ignored.getOrElse(ruleset, Set()).exists(rule => line.matches(".*" + rule + ".*"))
      if !exempted.getOrElse(ruleset, Set()).exists(path => line.indexOf("src/main/org/nlogo/" + path + ".java") != -1)
      if line != "No problems found!"
      if line.indexOf("rule violation suppressed by //NOPMD") == -1
      if line.indexOf("rule violation suppressed by Annotation") == -1
    } yield line.replaceFirst("\t", ":") // Emacs compilation-mode wants colon not tab
  }

  private def run(args: Seq[String]): Option[String] = {
    val errors = new collection.mutable.ArrayBuffer[String]
    for (ruleset <- if (args.isEmpty) core
                    else args.toList) {
      log.info("### " + ruleset)
      errors ++= violations("src", ruleset)
    }
    if (errors.isEmpty) None
    else Some(errors.mkString("\n"))
  }

}
