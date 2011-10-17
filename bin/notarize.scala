#!/bin/sh
JAVA_OPTS=-Dfile.encoding=UTF-8 exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@"
!# 
// Local Variables:
// mode: scala
// End:

/// Adds legal and academic notices to procedures and/or info tabs of all models.
///
/// Operates on models directory inside current directory.  Normally this is run from release.sh.
/// It can be run from the top-level directory for testing purposes (since running all of release.sh
/// takes a long time), but when run that way it will make hundreds of changes you should be careful
/// not to commit to Subversion.
///
/// To determine the contents of the notices, it looks in the dist/legal.txt file, also relative to
/// the current directory.  (bin/release.sh makes a symlink so dist/legal.txt can be found in the
/// temporary working directory where the release is being assembled.)
///
/// The NetLogo version comes from resources/system/version.txt; ditto on release.sh and symlinking.

import Scripting.{ read, slurp, shell }
import java.io.File

// Bomb if previews are missing?

val requirePreviews = args.nonEmpty && args(0).trim == "1"

// Read version.txt, legal.txt, models directory.

val netlogo = read("resources/system/version.txt").next  // for example "NetLogo 7.8"
def addSuffix(path: String) =
  path + ".nlogo" + (if(path.startsWith("3D/")) "3d"
                     else "")
val legal = Map() ++ {
  val format = "(.*?): (.*)".r
  for{line <- read("dist/legal.txt")
      if !line.startsWith("#") && !line.trim.isEmpty
      format(model, spec) = line.trim}
  yield (addSuffix(model), spec)
}      
val paths = shell("find models -name *.nlogo -o -name *.nlogo3d")
             .map(_.drop("models/".size))
             .toSeq

// Check legal.txt has no missing or bogus entries.

val missingEntries = paths.filter(!legal.contains(_))
require(missingEntries.isEmpty,
        "missing from legal.txt:\n" + missingEntries.mkString("\n"))
val bogusEntries = legal.keys.filter(!paths.contains(_)).toSeq
require(bogusEntries.isEmpty,
        "listed in legal.txt but not found:\n" + bogusEntries.mkString("\n"))

// Handle each model.

var missingPreviews = false
for(path <- paths) {
  val preview = path.reverse.dropWhile(_ != '.').reverse + "png"
  if(!new java.io.File("models/" + preview).exists) {
    println("MISSING PREVIEW: " + preview)
    missingPreviews = true
  }
  val munged = munge(path)
  new java.io.PrintStream(
    new java.io.FileOutputStream(new File("models/" + path)))
  .print(munged)
}

require(!(requirePreviews && missingPreviews), "missing previews")

def validateYear(y:Int) {
  require(y >= 1996 && y <= 2011,
          "invalid year: " + y)
}

lazy val validKeywords = List("MIT", "Wilensky", "specialCE", "MAC",
                              "Steiner", "Stroup", "3D", "NIELS")

def munge(path: String): String = {
  def require(requirement:Boolean, message: => String) = Predef.require(requirement, message + " ("+path+")")
  val (year, year2, keywords, cite) = {
    val format = """(\d\d\d\d)( \d\d\d\d)?(( \w+)*)( Cite: .*)?""".r
    val format(y1, y2, keys, _, cite) = legal(path)
    (y1.toInt,
     if(y2 == null) None else Some(y2.trim.toInt),
     if(keys == null) List() else keys.split("""\s""").map(_.trim).filter(!_.isEmpty).toList,
     if(cite == null) "" else cite.drop(" Cite: ".size).toString)
  }
  validateYear(year)
  year2.foreach(validateYear)
  require(keywords.forall(validKeywords.contains(_)), "invalid keyword found")
  if(keywords.contains("specialCE"))
    require(path.startsWith("Code Examples/"),
            "specialCE keyword is only for code examples")
  // if it's in the hubnet dir then we tack on hubnet a bunch of places
  val isHubNet =
    path.startsWith("HubNet Activities/")
  val name =
    path.reverse.dropWhile(_ != '.').tail.takeWhile(_ != '/').reverse.mkString
  val compressedname =
    (if(isHubNet) "HubNet" else "") + name.replaceAll(" ", "")
  val netlogohubnet =
    if(isHubNet) "NetLogo HubNet"
    else "NetLogo"
  val basename =
    if(name.endsWith(" 3D"))
      name.replaceFirst(" 3D$", "")
    else name
  val sections =
    slurp("models/" + path).split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n", -1).toList
  def copyright = {
    if(keywords.contains("Steiner"))
      "Copyright " + year + " Uri Wilensky. Some rights reserved. Includes code by James P. Steiner. "
    else if(keywords.contains("Stroup"))
      "Copyright " + year + " Uri Wilensky and Walter Stroup. All rights reserved."
    else if(keywords.contains("NIELS"))
      "Copyright " + year + " Pratim Sengupta and Uri Wilensky. All rights reserved."
    else if(path.containsSlice("Code Examples/") && !keywords.contains("specialCE"))
      "Public Domain"
    else
      "Copyright " + year + " Uri Wilensky. All rights reserved."
  }
  def mungeCode(path: String, code: String) = {
    require(code == code.trim + "\n",
            path + ": extra whitespace at beginning or end of Code tab")
    code + "\n\n" +
      (if(path.containsSlice("Code Examples/") && !keywords.contains("specialCE")) {
        require(!year2.isDefined, "can't specify two years for code examples")
        "; " + copyright + ":\n" +
        "; To the extent possible under law, Uri Wilensky has waived all\n" +
        "; copyright and related or neighboring rights to this Code Example.\n"
      }
      else
        "; " + copyright + "\n" + "; The full copyright notice is in the Info tab.\n"
     )
  }
  def mungeInfo(path: String, info: String) = {
    require(info == info.trim + "\n",
            path + ": extra whitespace at beginning or end of info tab")
    val lines = info.split('\n').toList
    // The "_.endsWith" check (repeated twice) is too loose.  Both times it should check for exact equality.
    // I'm leaving the loose check in for now because we haven't been over all the info tabs manually yet.
    // Once that's done, the tighter check should be reinstated. - ST 10/21/10
    require(lines.filter(_ == "## CREDITS AND REFERENCES").size == 1,
            "there must be exactly one CREDITS AND REFERENCES line in the info tab")
    require(lines.dropWhile(_ != "## CREDITS AND REFERENCES").drop(1)
            .forall(line => !line.startsWith("#")),
            "CREDITS AND REFERENCES must be the last header (line starting with #) in the info tab")
    (removeBlankCredits(lines.mkString("", "\n", "\n")) + "\n\n" +
      howToCiteSection + "\n\n" +
      copyrightSection)
  }
  def removeBlankCredits(info: String): String = {
    val divider = "## CREDITS AND REFERENCES"
    if(info.trim.endsWith(divider))
      info.take(info.indexOf(divider)).trim + "\n"
    else info
  }
  def howToCiteSection = {
    val builder = new StringBuilder
    builder.append("## HOW TO CITE\n")
    builder.append("If you mention this model in an academic publication, we ask that you ")
    builder.append("include these citations for the model itself and for the NetLogo software:  \n")
    builder.append("- ")
    if(!cite.isEmpty)
      builder.append(cite + " and Wilensky, U. (" + year + ").  " + netlogohubnet + " " + name + " model.  ")
    else if(keywords.contains("Stroup"))
      builder.append("Wilensky, U. and Stroup, W. (" + year + "). " + netlogohubnet + " " + name + " model.  ")
    else
      builder.append("Wilensky, U. (" + year + ").  " + netlogohubnet + " " + name + " model.  ")
    builder.append("http://ccl.northwestern.edu/netlogo/models/" + compressedname + ".  ")
    builder.append("Center for Connected Learning and Computer-Based Modeling, ")
    builder.append("Northwestern University, Evanston, IL.  \n")
    builder.append("- Wilensky, U. (1999). NetLogo. ")
    builder.append("http://ccl.northwestern.edu/netlogo/. ")
    builder.append("Center for Connected Learning and ")
    builder.append("Computer-Based Modeling, Northwestern University, Evanston, IL.  \n")
    builder.append("\n\n")
    builder.append("In other publications, please use:  \n")
    builder.append("- ")
    builder.append(copyright + " See ")
    builder.append("http://ccl.northwestern.edu/netlogo/models/" + compressedname + " ")
    builder.append("for terms of use.  \n")
    if(keywords.contains("NIELS")) {
      builder.append("\n\n")
      builder.append("To refer to the NIELS curriculum as a whole in academic publications, please use: ")
      builder.append("Sengupta, P. and Wilensky, U. (2008). NetLogo NIELS curriculum. ")
      builder.append("http://ccl.northwestern.edu/NIELS. ")
      builder.append("Center for Connected Learning and Computer-Based Modeling, Northwestern University, Evanston, IL.  \n")
    }
    builder.toString
  }
  def copyrightSection = {
    val builder = new StringBuilder
    builder.append("## COPYRIGHT NOTICE\n")
    if(keywords.contains("Steiner")) {
      builder.append(copyright + "\n")
      builder.append("\n")
      builder.append("This work is licensed under the Creative Commons ")
      builder.append("Attribution-NonCommercial-ShareAlike 2.5 License.  To view a copy of ")
      builder.append("this license, visit http://creativecommons.org/licenses/by-nc-sa/2.5/ ")
      builder.append("or send a letter to Creative Commons, 559 Nathan Abbott Way, ")
      builder.append("Stanford, California 94305, USA.\n")
      builder.append("\n")
    }
    else {
      builder.append(copyright + "\n")
      builder.append("\n")
      builder.append("Permission to use, modify or redistribute this model is hereby granted, ")
      builder.append("provided that both of the following requirements are followed:  \n")
      builder.append("a) this copyright notice is included.  \n")
      builder.append("b) this model will not be redistributed for profit without permission ")
      if(keywords.contains("Stroup")) {
        builder.append("from the copyright holders. ")
        builder.append("Contact the copyright holders for appropriate licenses for redistribution for ")
      }
      else if(keywords.contains("NIELS")) {
        builder.append("from Pratim Sengupta and Uri Wilensky. ")
        builder.append("Contact Uri Wilensky for appropriate licenses for redistribution for ")
      }
      else {
        builder.append("from Uri Wilensky. ")
        builder.append("Contact Uri Wilensky for appropriate licenses for redistribution for ")
      }
      builder.append("profit.\n")
      builder.append("\n")
    }
    if(keywords.contains("3D"))
      builder.append("This is a 3D version of the 2D model " + basename + ".\n\n")
    if(year2.isDefined) {
      builder.append("This model was created as part of the project: CONNECTED MATHEMATICS: ")
      builder.append("MAKING SENSE OF COMPLEX PHENOMENA THROUGH BUILDING OBJECT-BASED PARALLEL ")
      builder.append("MODELS (OBPML).  The project gratefully acknowledges the support of the ")
      builder.append("National Science Foundation (Applications of Advanced Technologies ")
      builder.append("Program) -- grant numbers RED #9552950 and REC #9632612.\n")
      builder.append("\n")
    }
    else if(keywords.contains("MAC")) {
      builder.append("This model and associated activities and materials were created as ")
      builder.append("part of the project: MODELING ACROSS THE CURRICULUM.  The project ")
      builder.append("gratefully acknowledges the support of the National Science Foundation, ")
      builder.append("the National Institute of Health, and the Department of Education ")
      builder.append("(IERI program) -- grant number REC #0115699.")
      if(year <= 2004) {
        builder.append(" Additional support ")
        builder.append("was provided through the projects: PARTICIPATORY SIMULATIONS: ")
        builder.append("NETWORK-BASED DESIGN FOR SYSTEMS LEARNING IN CLASSROOMS and/or ")
        builder.append("INTEGRATED SIMULATION AND MODELING ENVIRONMENT -- NSF (REPP & ROLE ")
        builder.append("programs) grant numbers REC #9814682 and REC-0126227.")
      }
      builder.append("\n\n")
    }
    else if(year <= 2004) {
      if(path.startsWith("HubNet Activities/") &&
         !path.startsWith("HubNet Activities/Code Examples/"))
        builder.append("This activity and associated models and materials were created as part of the projects: ")
      else
        builder.append("This model was created as part of the projects: ")
      builder.append("PARTICIPATORY SIMULATIONS: NETWORK-BASED DESIGN FOR SYSTEMS LEARNING IN ")
      builder.append("CLASSROOMS and/or INTEGRATED SIMULATION AND MODELING ENVIRONMENT. ")
      builder.append("The project gratefully acknowledges the support of the ")
      builder.append("National Science Foundation (REPP & ROLE programs) -- ")
      builder.append("grant numbers REC #9814682 and REC-0126227.\n")
      builder.append("\n")
    }
    if(keywords.contains("MIT")) {
      require(year2.isDefined,"MIT keyword requires specifying two years")
      builder.append("This model was developed at the MIT Media Lab using CM StarLogo.  ")
      builder.append("See Resnick, M. (1994) \"Turtles, Termites and Traffic Jams: Explorations in Massively Parallel Microworlds.\"  ")
      builder.append("Cambridge, MA: MIT Press.  Adapted to StarLogoT, " + year + ", ")
      builder.append("as part of the Connected Mathematics Project.\n\n")
    }
    if(keywords.contains("Wilensky")) {
      require(year2.isDefined,"Wilensky keyword requires specifying two years")
      builder.append("This model was developed at the MIT Media Lab using CM StarLogo.  ")
      builder.append("See Wilensky, U. (1993). Thesis - Connected Mathematics: Building Concrete Relationships with Mathematical Knowledge. ")
      builder.append("Adapted to StarLogoT, " + year + ", as part of the Connected Mathematics Project.  ")
      builder.append("Adapted to NetLogo, " + year2.get + ", as part of the Participatory Simulations Project.\n\n")
    }
    if(year2.isDefined) {
      require(!keywords.contains("MAC"),
              "the MAC keyword is not supported for converted StarLogoT models")
      if(year2.get <= 2004) {
        builder.append("This model was converted to NetLogo as part of the projects: ")
        builder.append("PARTICIPATORY SIMULATIONS: NETWORK-BASED DESIGN FOR SYSTEMS LEARNING ")
        builder.append("IN CLASSROOMS and/or INTEGRATED SIMULATION AND MODELING ENVIRONMENT. ")
        builder.append("The project gratefully acknowledges the support of the ")
        builder.append("National Science Foundation (REPP & ROLE programs) -- ")
        builder.append("grant numbers REC #9814682 and REC-0126227. ")
      }
      builder.append("Converted from StarLogoT to NetLogo, " + year2.get + ".\n")
      builder.append("\n")
    }
    if(keywords.contains("NIELS")) {
      builder.append("To use this model for academic or commercial research, please ")
      builder.append("contact Pratim Sengupta at <pratim.sengupta@vanderbilt.edu> or Uri ")
      builder.append("Wilensky at <uri@northwestern.edu> for a mutual agreement prior to usage.\n\n")
    }
    builder.toString
  }
  val newCode = mungeCode(path, sections(0))
  val newInfo = if(path.containsSlice("Code Examples/")) sections(2)
                else mungeInfo(path, sections(2))
  val newSections = newCode :: sections(1) :: newInfo :: sections.drop(3)
  newSections.mkString("@#$#@#$#@\n")
}
