// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

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

import org.nlogo.core,
  core.{AstVisitor, Fail, Instruction,
        ProcedureDefinition, ReporterApp, Statement, Syntax,
        prim, CommandBlock, Expression, ReporterBlock},
    prim.{_call, _callreport, _commandlambda, _reporterlambda },
    Fail._

class AgentTypeChecker(defs: Seq[ProcedureDefinition]) {
  def check(): Unit = {
    def usables = defs.map(_.procedure.agentClassString).toList
    val oldUsables = usables
    for(procdef <- defs)
      new AgentTypeCheckerVisitor("OTPL").visitProcedureDefinition(procdef)
    if(usables != oldUsables) check()
  }

  class AgentTypeCheckerVisitor(var agentClassString: String) extends AstVisitor {
    // agentClassString is "var" because it's where we accumulate our result.
    // it starts out as OTPL and the more code we see the more restricted
    // it may grow.  The use of mutable state in this way is characteristic
    // of the Visitor pattern.

    override def visitProcedureDefinition(procdef: ProcedureDefinition) {
      super.visitProcedureDefinition(procdef)
      // after we've seen the whole procedure, store the result there
      procdef.procedure.agentClassString = agentClassString
    }

    // visitStatement and visitReporterApp are clones of each other
    override def visitStatement(stmt: Statement) {
      val c = stmt.command
      agentClassString = typeCheck(c, agentClassString)

      val nonReferentialArgs = (stmt.args zip c.syntax.right).collect {
        case (arg, argType) if ! Syntax.compatible(Syntax.ReferenceType, argType) => arg
      }
      if(c.syntax.blockAgentClassString.isDefined)
        chooseVisitorAndContinue(c.syntax.blockAgentClassString.get, nonReferentialArgs)
      else
        nonReferentialArgs.foreach(visitExpression)
      c.agentClassString = agentClassString
    }

    // visitStatement and visitReporterApp are clones of each other
    override def visitReporterApp(app: ReporterApp) {
      val r = app.reporter
      agentClassString = typeCheck(r, agentClassString)

      val visitor = new AgentTypeCheckerVisitor("OTPL")
      if (r.isInstanceOf[_commandlambda] || r.isInstanceOf[_reporterlambda])
        visitor.visitExpression(app.args.head)
      else if(r.syntax.blockAgentClassString.isDefined)
        chooseVisitorAndContinue(r.syntax.blockAgentClassString.get, app.args)
      else
        super.visitReporterApp(app)

      r match {
        case (_: _commandlambda | _: _reporterlambda) =>
          r.blockAgentClassString = Some(visitor.agentClassString)
        case _ =>
          r.agentClassString = agentClassString
      }
    }

    private def chooseVisitorAndContinue(blockAgentClassString: String, exps: Seq[Expression]) {
      exps.foreach { exp =>
        val visitor = exp match {
          case _: CommandBlock | _: ReporterBlock =>
            val argsAgentClassString =
              if (blockAgentClassString != "?") blockAgentClassString
              else exps match {
                case Seq(app: ReporterApp, _*) => getReportedAgentType(app)
                case _ => "-TPL"
              }
            new AgentTypeCheckerVisitor(argsAgentClassString)
          case _ => this
        }
        visitor.visitExpression(exp)
      }
    }

    private def getReportedAgentType(app: ReporterApp): String = {
      app.reporter.syntax.ret match {
        case Syntax.TurtleType | Syntax.TurtlesetType => "-T--"
        case Syntax.PatchType | Syntax.PatchsetType => "--P-"
        case Syntax.LinkType | Syntax.LinksetType => "---L"
        // This is kludgy.  We assume the agent type is the same as that
        // reported by the first argument to the command. ("with" and "at-points"
        // are examples of this, also "one-of".)   Careful, this assumption
        // could break someday. - ST 12/8/02, 12/15/05, 2/21/08
        case Syntax.AgentType | Syntax.AgentsetType =>
          app.args match {
            case Seq(app: ReporterApp, _*) => getReportedAgentType(app)
            case _ => "-TPL"
          }
        case _ => "-TPL"
      }
    }

    private def typeCheck(coreInstruction: Instruction, agentClassString: String): String = {
      // Check if dealing with a procedure or a primitive
      coreInstruction match {
        case _call(proc) =>
          checkSatisfiable(coreInstruction, agentClassString, proc.agentClassString)
        case _callreport(proc) =>
          checkSatisfiable(coreInstruction, agentClassString, proc.agentClassString)
        case _ =>
          checkSatisfiable(coreInstruction, agentClassString, coreInstruction.agentClassString)
      }
    }

    private def checkSatisfiable(coreInstruction: Instruction, agentClassString: String, instructionClassString: String): String = {
      val classString = combineRestrictions(agentClassString, instructionClassString)
      if (classString == "----") {
        val name = coreInstruction.token.text.toUpperCase
        exception(
          s"""|You can't use $name in ${aOrAn(agentName(agentClassString))} context,
              | because $name is ${agentName(instructionClassString)}-only.""".stripMargin.replaceAll("\n", ""),
          coreInstruction.token)
      }
      classString
    }

    // This is basically an "and" operation:
    //   OTP- and -TPL equals -TP-
    //   OT-- and --PL equals ----
    // and so on.
    private def combineRestrictions(agentClassString1: String, agentClassString2: String): String =
      agentClassString1.map(c => if(c != '-' && agentClassString2.indexOf(c) != -1) c
                         else '-')

    private def aOrAn(s: String): String = {
      if (s.charAt(0) == 'o') s"an $s"
      else s"a $s"
    }

    // for error message generation
    private def agentName(agentClassString: String): String = {
      val abbreviations = Map('O' -> "observer", 'T' -> "turtle",
                              'P' -> "patch", 'L' -> "link")
      agentClassString.filter(_ != '-').map(abbreviations(_)).mkString("/")
    }
  }
}
