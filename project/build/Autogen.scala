import sbt._
import sbt.FileUtilities.{readString, write => writeFile}

trait Autogen { this: DefaultProject =>

  val nlogoPackage = "src" / "main" / "org" / "nlogo"
  val autogenFolder = "project" / "build" / "autogen"

  lazy val autogen = task{ None } dependsOn(autogenLexers, autogenEvents)

  lazy val cleanAutogenFiles = task {
    for(f <- javaLexers ::: events ) f.asFile.delete
    None
  }

  private val flexFiles = List(autogenFolder / "ImportLexer.flex", autogenFolder / "TokenLexer.flex")
  private val javaLexers = List(nlogoPackage / "agent" / "ImportLexer.java", nlogoPackage / "lex" / "TokenLexer.java")
  private val events = List( nlogoPackage / "app" / "Events.java", nlogoPackage / "window" / "Events.java")


  // this used to be broken into two tasks, but jflex doesnt seem to be threadsafe
  // so we have to run them serially, which means we have to generate them both each time. -JC 6/8/10
  private lazy val autogenLexers = fileTask(javaLexers from flexFiles) {
    def flex(ppackage: String, kind: String) {
      log.info("creating autogen/" + kind + ".java")
      JFlex.Main.main(Array("--quiet", (autogenFolder / (kind + ".flex")).asFile.toString))
      log.info("creating src/main/org/nlogo/" + ppackage + "/" + kind + ".java")
      write(nlogoPackage / ppackage / (kind + ".java"),
        read(autogenFolder / "warning.txt") + read(autogenFolder / (kind + ".java")))
      (autogenFolder / (kind + ".java")).asFile.delete()
    }
    flex("agent", "ImportLexer")
    flex("lex", "TokenLexer")
    None
  }


  private lazy val autogenEvents = fileTask(events from (autogenFolder / "events.txt")) {
    def events(ppackage: String) {

      val file = nlogoPackage / ppackage / "Events.java"
      log.info("creating: " + file)

      var codeString = ""
      def append(s: String) = codeString += (s + "\n")

      val (access, qualify) = ppackage match {
        case "window" => ("public ", "")
        case "app" => ("", "org.nlogo.window.")
      }

      append(read(autogenFolder / "warning.txt"))

      append("package org.nlogo." + ppackage + " ;")
      append("\n")
      append(access + "final strictfp class Events")
      append("{")
      append("    // not instantiable")
      append("    private Events() { throw new IllegalStateException() ; }")
      append("\n")

      for{line <- read(autogenFolder/"events.txt").split("\n")
          if !line.trim.isEmpty // skip blank lines
          if !line.startsWith("#") // skip comment lines
          if line.startsWith(ppackage)} // skip unless in right package
        {
          val splitt = line.split("-").filter(!_.isEmpty)
          val shortName = splitt(0).split('.')(1).trim
          val fieldString = splitt.drop(1).mkString("-")

          val name = shortName + "Event"
          append("    " + access + "static strictfp class " + name + " extends " + qualify + "Event")
          append("    {")
          val fields = fieldString.split(""" \s*-\s* """).filter(!_.isEmpty).map(_.trim)
          if (!fields.isEmpty) {
            for (field <- fields)
              append("        " + access + "final " + field + " ;")
            append("        " + access + name + fields.mkString("( ", " , ", " )"))
            append("        {")
            for (field <- fields) {
              val variable = field.split("""\s+""").drop(1)(0)
              append("            this." + variable + " = " + variable + " ;")
            }
            append("        }")
          }
          append("        @Override public void beHandledBy( " + qualify + "Event.Handler handler )")
          append("        {")
          append("            ( (Handler) handler ).handle( this ) ;")
          append("        }")
          append("        " + access + "interface Handler extends " + qualify + "Event.Handler")
          append("        {")
          append("            void handle( " + name + " e ) ;")
          append("        }")
          append("    }")
        }
      append("}")

      write(file, codeString)
    }
    events("app")
    events("window")
    None
  }


  // couple of simple utilities
  private def read(path: Path) = readString(path.asFile, log).right.get
  private def write(path: Path, contents: String) {writeFile(path.asFile, contents, log)}
}
