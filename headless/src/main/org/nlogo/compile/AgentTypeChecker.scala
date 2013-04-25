// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// The purpose of this class is to determine which portions of the
// code are usable by which agent types.  For example "fd 1" is
// turtle-only, but "print 2 + 2" could be any of the four agent
// types.
//
// How it works exactly gets rather complicated.  The algorithm has
// three basic parts:
//
//  (1) agent type restrictions propagate forward and downwards within
//      procedures.
//
//      EXAMPLE OF FORWARD PROPAGATION: "fd 1 sprout 1" fails to
//      to check because once we've seen "fd 1", all subsequent
//      commands at that level must be turtle-only too.
//
//      EXAMPLE OF DOWNWARD PROPAGATION: "hatch 1 [ sprout 1 ]" fails
//      to check because hatch knows that code inside it must be
//      turtle-only, but sprout is patch-only.
//
//  (2) restrictions then propagate back up to determine which
//      agent types are permitted to call each entire procedure.
//
//      EXAMPLE: from "to foo fd 1 end" we conclude that FOO is
//      a turtle procedure, because fd is turtle-only and that
//      information propagates up to its enclosing procedure.
//
//  (3) propagation of restriction between procedures.
//      initially all procedures are assumed to be OTPL, but
//      once one procedure is found to be restricted, that
//      may enable us to prove that other procedures are
//      more restrictive as well.
//
//      EXAMPLE: if we have "to foo fd 1 end" and
//      "to bar foo end", then once we realize foo is
//      turtle-only, we can also conclude bar is turtle-only.
//
// SUBTLETY INVOLVING PARTS 1 AND 2:
// We have to be careful not to allow inappropriate propagation of
// restrictions up or down through command block or reporter block
// boundaries.  For example, once we see "ask", the commands inside
// are not subject to restrictions from above (except in the argument
// to ASK if its type is known), and restrictions discovered inside
// the ASK do not propagate forward to commands after the ASK or
// upward to the enclosing procedure.  Note that only some blocks
// behave this way; for example the command block in a WHILE behaves
// normally; it does not behave like ASK.  Here's how we implement
// the two cases.  When we hit something like WHILE, we keep using the
// same AgentTypeCheckerVisitor object.  But when we hit something like ASK,
// we don't let the current AgentTypeCheckerVisitor descend inside the ASK;
// rather, we make a new one and then discard it when we're done
// analzying the ASK.
//
// SUBTLETY INVOLVING PART 3: In order to propagate this information
// fully, we have to do multiple passes.  At each pass, we analyze all
// of the procedures.  If we discover any additional restrictions
// during that pass, then we must do another pass in order to
// propagate those new restrictions.  Once two passes in a row give
// the same answer for all the procedures, we can stop.
//
// Whew!  Got all that?

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Instruction, Procedure }
import org.nlogo.prim.{ _call, _callreport, _task }
import org.nlogo.parse, parse.Fail._

private class AgentTypeChecker(defs: Seq[parse.ProcedureDefinition]) {

  def check() {
    def usables = defs.map(_.procedure.usableBy).toList
    val oldUsables = usables
    for(procdef <- defs)
      procdef.accept(new AgentTypeCheckerVisitor(procdef.procedure, "OTPL"))
    if(usables != oldUsables) check()
  }

  class AgentTypeCheckerVisitor(currentProcedure: Procedure, var usableBy: String) extends parse.DefaultAstVisitor {
    // usableBy is "var" because it's where we accumulate our result.
    // it starts out as OTPL and the more code we see the more restricted
    // it may grow.  The use of mutable state in this way is characteristic
    // of the Visitor pattern.

    override def visitProcedureDefinition(procdef: parse.ProcedureDefinition) {
      super.visitProcedureDefinition(procdef)
      // after we've seen the whole procedure, store the result there
      procdef.procedure.usableBy = usableBy
    }
    // visitStatement and visitReporterApp are clones of each other

    override def visitStatement(stmt: parse.Statement) {
      val c = stmt.command
      usableBy = typeCheck(currentProcedure, c, usableBy)
      if(c.syntax.blockAgentClassString != null)
        chooseVisitorAndContinue(c.syntax.blockAgentClassString, stmt.args)
      else
        super.visitStatement(stmt)
      c.agentClassString = usableBy
    }

    // visitStatement and visitReporterApp are clones of each other
    override def visitReporterApp(app: parse.ReporterApp) {
      val r = app.reporter
      usableBy = typeCheck(currentProcedure, r, usableBy)
      if(r.isInstanceOf[_task])
        app.args.head.accept(new AgentTypeCheckerVisitor(currentProcedure, "OTPL"))
      else if(r.syntax.blockAgentClassString != null)
        chooseVisitorAndContinue(r.syntax.blockAgentClassString, app.args)
      else
        super.visitReporterApp(app)
      r.agentClassString = usableBy
    }

    private def chooseVisitorAndContinue(blockAgentClassString: String, exps: Seq[parse.Expression]) {
      for(exp <- exps) {
        exp.accept(
          exp match {
            case _: parse.CommandBlock | _: parse.ReporterBlock =>
              val argsAgentClassString =
                if(blockAgentClassString != "?") blockAgentClassString
                else exps match {
                  case Seq(app: parse.ReporterApp, _*) => getReportedAgentType(app)
                  case _ => "-TPL"
                }
              new AgentTypeCheckerVisitor(currentProcedure, argsAgentClassString)
            case _ => this } ) }
    }

    def getReportedAgentType(app: parse.ReporterApp): String = {
      app.reporter.syntax.ret match {
        case Syntax.TurtleType | Syntax.TurtlesetType => "-T--"
        case Syntax.PatchType  | Syntax.PatchsetType  => "--P-"
        case Syntax.LinkType   | Syntax.LinksetType   => "---L"
        // This is kludgy.  We assume the agent type is the same as that
        // reported by the first argument to the command. ("with" and "at-points"
        // are examples of this, also "one-of".)   Careful, this assumption
        // could break someday. - ST 12/8/02, 12/15/05, 2/21/08
        case Syntax.AgentType  | Syntax.AgentsetType  =>
          app.args match {
            case Seq(app: parse.ReporterApp, _*) => getReportedAgentType(app)
            case _ => "-TPL"
          }
        case _ => "-TPL"
      }
    }

    def typeCheck(currentProcedure: Procedure, instruction: Instruction, usableBy: String): String = {
      // Check if dealing with a procedure or a primitive
      val calledProcedure: Option[Procedure] =
        instruction match {
          case c: _call => Some(c.procedure)
          case cr: _callreport => Some(cr.procedure)
          case _ => None
        }
      if(calledProcedure.isDefined &&
         (calledProcedure.get.usableBy == null ||
          (calledProcedure.get.usableBy.indexOf('?') != -1 && calledProcedure.get != currentProcedure)))
        usableBy + "?"
      else {
        val instructionUsableBy =
          if(calledProcedure.isDefined) calledProcedure.get.usableBy
          else instruction.syntax.agentClassString
        val result = combineRestrictions(usableBy, instructionUsableBy)
        if(result == "----") {
          val name = instruction.tokenLimitingType.name
          exception("You can't use " + name + " in " + usableByToEnglish(usableBy, true) +
                    " context, because " + name + " is " + usableByToEnglish(instructionUsableBy, false) +
                    "-only.", instruction.tokenLimitingType)
        }
        result
      }
    }

    // This is basically an "and" operation:
    //   OTP- and -TPL equals -TP-
    //   OT-- and --PL equals ----
    // and so on.
    def combineRestrictions(usableBy1: String, usableBy2: String): String =
      usableBy1.map(c => if(c != '-' && usableBy2.indexOf(c) != -1) c
                         else '-')

    // for error message generation
    def usableByToEnglish(usableBy: String, addAOrAn: Boolean): String = {
      val abbreviations = Map('O' -> "observer", 'T' -> "turtle",
                              'P' -> "patch", 'L' -> "link")
      val english = usableBy.filter(_ != '-').map(abbreviations(_)).mkString("/")
      if(!addAOrAn) english
      else if(english.charAt(0) == 'o') "an " + english
      else "a " + english
    }

  }
}
