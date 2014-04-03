// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Syntax._

object Syntaxes {
  val syntaxes = A.syntaxes ++ B.syntaxes
}

object A {
  val syntaxes = Map[String, Syntax](
    ("_breed", Syntax.reporterSyntax(
      ret = AgentsetType)),
    ("_return", Syntax.commandSyntax()),
    ("_goto", Syntax.commandSyntax()),
    ("_unaryminus", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_countwith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = NumberType, agentClassString = "OTPL", blockAgentClassString = "?")),
    ("_countotherwith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = NumberType, agentClassString = "OTPL", blockAgentClassString = "?")),
    ("_repeatinternal", Syntax.commandSyntax()),
    ("_repeatlocalinternal", Syntax.commandSyntax()),
    ("_repeatlocal", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType))),
    ("_returnreport", Syntax.commandSyntax()),
    ("_fd1", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_fdinternal", Syntax.commandSyntax(
      right = List(WildcardType), switches = true)),
    ("_crtfast", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "O---", switches = true)),
    ("_crofast", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "O---", switches = true)),
    ("_sproutfast", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "--P-", switches = true)),
    ("_setobservervariable", Syntax.commandSyntax(
      right = List(WildcardType), switches = true)),
    ("_setpatchvariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "-TP-", switches = true)),
    ("_setturtlevariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "-T--", switches = true)),
    ("_setlinkvariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "---L", switches = true)),
    ("_setbreedvariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "-T--", switches = true)),
    ("_breedhere", Syntax.reporterSyntax(
      ret = TurtlesetType, agentClassString = "-TP-")),
    ("_setturtleorlinkvariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "-T-L", switches = true)),
    ("_turtlevariableof", Syntax.reporterSyntax(
      right = List(TurtleType | TurtlesetType), ret = WildcardType)),
    ("_breedvariableof", Syntax.reporterSyntax(
      right = List(TurtleType | TurtlesetType), ret = WildcardType)),
    ("_patchvariableof", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType | TurtlesetType | PatchsetType), ret = WildcardType)),
    ("_patchvariabledouble", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "-TP-")),
    ("_setprocedurevariable", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_randomconst", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_constdouble", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_constlist", Syntax.reporterSyntax(
      ret = ListType)),
    ("_constboolean", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_conststring", Syntax.reporterSyntax(
      ret = StringType)),
    ("_nobody", Syntax.reporterSyntax(
      ret = NobodyType)),
    ("_observervariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType)),
    ("_letvariable", Syntax.reporterSyntax(
      ret = WildcardType)),
    ("_patchvariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "-TP-")),
    ("_turtleorlinkvariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "-T-L")),
    ("_turtlevariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "-T--")),
    ("_linkvariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "---L")),
    ("_breedvariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "-T--")),
    ("_turtlevariabledouble", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "-T--")),
    ("_taskvariable", Syntax.reporterSyntax(
      ret = WildcardType)),
    ("_procedurevariable", Syntax.reporterSyntax(
      ret = WildcardType)),
    ("_commandtask", Syntax.reporterSyntax(
      ret = CommandTaskType)),
    ("_reportertask", Syntax.reporterSyntax(
      ret = ReporterTaskType)),
    ("_breedsingular", Syntax.reporterSyntax(
      right = List(NumberType), ret = TurtleType | NobodyType)),
    ("_patchwest", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patcheast", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchnorth", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchsouth", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchne", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchnw", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchse", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_patchsw", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_countother", Syntax.reporterSyntax(
      right = List(AgentsetType), ret = NumberType, agentClassString = "-TPL")),
    ("_otherwith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = AgentsetType, agentClassString = "OTPL", blockAgentClassString = "?")),
    ("_recursefast", Syntax.commandSyntax()),
    ("_anywith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = BooleanType, agentClassString = "OTPL", blockAgentClassString = "?")),
    ("_anyotherwith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = BooleanType, agentClassString = "OTPL", blockAgentClassString = "?")),
    ("_nsum", Syntax.reporterSyntax(
      right = List(ReferenceType), ret = NumberType, agentClassString = "-TP-")),
    ("_nsum4", Syntax.reporterSyntax(
      right = List(ReferenceType), ret = NumberType, agentClassString = "-TP-")),
    ("_linkbreed", Syntax.reporterSyntax(
      ret = LinksetType)),
    ("_setletvariable", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_breedon", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType | TurtlesetType | PatchsetType), ret = TurtlesetType)),
    ("_turtleorlinkvariableof", Syntax.reporterSyntax(
      right = List(LinkType | LinksetType | TurtleType | TurtlesetType), ret = WildcardType)),
    ("_isbreed", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_hatchfast", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--", switches = true)),
    ("_linkbreedvariable", Syntax.reporterSyntax(
      ret = WildcardType | ReferenceType, agentClassString = "---L")),
    ("_linkbreedsingular", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = LinkType | NobodyType)),
    ("_linkvariableof", Syntax.reporterSyntax(
      right = List(LinkType | LinksetType), ret = WildcardType)),
    ("_setlinkbreedvariable", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "---L", switches = true)),
    ("_waitinternal", Syntax.commandSyntax(
      switches = true)),
    ("_oneofwith", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = AgentType | NobodyType, blockAgentClassString = "?")),
    ("_patchhereinternal", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-TP-")),
    ("_anyother", Syntax.reporterSyntax(
      right = List(AgentsetType), ret = BooleanType, agentClassString = "-TPL")),
    ("_breedat", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = TurtlesetType, agentClassString = "-TP-")),
    ("_linkbreedvariableof", Syntax.reporterSyntax(
      right = List(LinkType | LinksetType), ret = WildcardType))
  )
}

object B {
  val syntaxes = Map[String, Syntax](
    ("_abs", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_acos", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_all", Syntax.reporterSyntax(
      right = List(AgentsetType, BooleanBlockType), ret = BooleanType, blockAgentClassString = "?")),
    ("_and", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 6, left = BooleanType, right = List(BooleanType), ret = BooleanType)),
    ("_any", Syntax.reporterSyntax(
      right = List(AgentsetType), ret = BooleanType)),
    ("_approximatehsb", Syntax.reporterSyntax(
      right = List(NumberType, NumberType, NumberType), ret = NumberType)),
    ("_approximatergb", Syntax.reporterSyntax(
      right = List(NumberType, NumberType, NumberType), ret = NumberType)),
    ("_asin", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_ask", Syntax.commandSyntax(
      right = List(AgentsetType | AgentType, CommandBlockType), blockAgentClassString = "?", switches = true)),
    ("_askconcurrent", Syntax.commandSyntax(
      right = List(AgentsetType, CommandBlockType), blockAgentClassString = "?", switches = true)),
    ("_atan", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_atpoints", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = TurtlesetType | PatchsetType, right = List(ListType), ret = AgentsetType)),
    ("_autoplot", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_autoplotoff", Syntax.commandSyntax()),
    ("_autoploton", Syntax.commandSyntax()),
    ("_basecolors", Syntax.reporterSyntax(
      ret = ListType)),
    ("_beep", Syntax.commandSyntax()),
    ("_behaviorspacerunnumber", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_bench", Syntax.commandSyntax(
      right = List(NumberType, NumberType), agentClassString = "O---")),
    ("_bk", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--")),
    ("_boom", Syntax.reporterSyntax(
      ret = WildcardType)),
    ("_bothends", Syntax.reporterSyntax(
      ret = AgentsetType, agentClassString = "---L")),
    ("_butfirst", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = StringType | ListType)),
    ("_butlast", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = StringType | ListType)),
    ("_canmove", Syntax.reporterSyntax(
      right = List(NumberType), ret = BooleanType, agentClassString = "-T--")),
    ("_carefully", Syntax.commandSyntax(
      right = List(CommandBlockType, CommandBlockType))),
    ("_ceil", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_changetopology", Syntax.commandSyntax(
      right = List(BooleanType, BooleanType))),
    ("_checksum", Syntax.reporterSyntax(
      ret = StringType, agentClassString = "O---")),
    ("_checksyntax", Syntax.reporterSyntax(
      right = List(StringType), ret = StringType)),
    ("_clearall", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearallandresetticks", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearallplots", Syntax.commandSyntax()),
    ("_cleardrawing", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearglobals", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearlinks", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearoutput", Syntax.commandSyntax()),
    ("_clearpatches", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearplot", Syntax.commandSyntax()),
    ("_clearticks", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_clearturtles", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_cos", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_count", Syntax.reporterSyntax(
      right = List(AgentsetType), ret = NumberType)),
    ("_createlinkfrom", Syntax.commandSyntax(
      right = List(TurtleType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createlinksfrom", Syntax.commandSyntax(
      right = List(TurtlesetType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createlinksto", Syntax.commandSyntax(
      right = List(TurtlesetType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createlinkswith", Syntax.commandSyntax(
      right = List(TurtlesetType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createlinkto", Syntax.commandSyntax(
      right = List(TurtleType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createlinkwith", Syntax.commandSyntax(
      right = List(TurtleType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "---L", switches = true)),
    ("_createorderedturtles", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType | OptionalType), agentClassString = "O---", blockAgentClassString = "-T--", switches = true)),
    ("_createtemporaryplotpen", Syntax.commandSyntax(
      right = List(StringType))),
    ("_createturtles", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType | OptionalType), agentClassString = "O---", blockAgentClassString = "-T--", switches = true)),
    ("_dateandtime", Syntax.reporterSyntax(
      ret = StringType)),
    ("_die", Syntax.commandSyntax(
      agentClassString = "-T-L", switches = true)),
    ("_diffuse", Syntax.commandSyntax(
      right = List(ReferenceType, NumberType), agentClassString = "O---", switches = true)),
    ("_diffuse4", Syntax.commandSyntax(
      right = List(ReferenceType, NumberType), agentClassString = "O---", switches = true)),
    ("_display", Syntax.commandSyntax(
      switches = true)),
    ("_distance", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType), ret = NumberType, agentClassString = "-TP-")),
    ("_distancenowrap", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType), ret = NumberType, agentClassString = "-TP-")),
    ("_distancexy", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType, agentClassString = "-TP-")),
    ("_distancexynowrap", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType, agentClassString = "-TP-")),
    ("_div", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 2, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_done", Syntax.commandSyntax()),
    ("_downhill", Syntax.commandSyntax(
      right = List(ReferenceType), agentClassString = "-T--", switches = true)),
    ("_downhill4", Syntax.commandSyntax(
      right = List(ReferenceType), agentClassString = "-T--", switches = true)),
    ("_dump", Syntax.reporterSyntax(
      ret = StringType, agentClassString = "O---")),
    ("_dump1", Syntax.reporterSyntax(
      ret = StringType)),
    ("_dumpextensionprims", Syntax.reporterSyntax(
      ret = StringType)),
    ("_dumpextensions", Syntax.reporterSyntax(
      ret = StringType)),
    ("_dx", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "-T--")),
    ("_dy", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "-T--")),
    ("_empty", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = BooleanType)),
    ("_equal", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 5, left = WildcardType, right = List(WildcardType), ret = BooleanType)),
    ("_error", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_errormessage", Syntax.reporterSyntax(
      ret = StringType)),
    ("_every", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType), switches = true)),
    ("_exp", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_experimentstepend", Syntax.commandSyntax(
      agentClassString = "O---")),
    ("_exportdrawing", Syntax.commandSyntax(
      right = List(StringType))),
    ("_exportinterface", Syntax.commandSyntax(
      right = List(StringType))),
    ("_exportoutput", Syntax.commandSyntax(
      right = List(StringType))),
    ("_exportplot", Syntax.commandSyntax(
      right = List(StringType, StringType))),
    ("_exportplots", Syntax.commandSyntax(
      right = List(StringType))),
    ("_exportview", Syntax.commandSyntax(
      right = List(StringType))),
    ("_exportworld", Syntax.commandSyntax(
      right = List(StringType))),
    ("_extracthsb", Syntax.reporterSyntax(
      right = List(NumberType | ListType), ret = ListType)),
    ("_extractrgb", Syntax.reporterSyntax(
      right = List(NumberType), ret = ListType)),
    ("_face", Syntax.commandSyntax(
      right = List(TurtleType | PatchType), agentClassString = "-T--", switches = true)),
    ("_facenowrap", Syntax.commandSyntax(
      right = List(TurtleType | PatchType), agentClassString = "-T--", switches = true)),
    ("_facexy", Syntax.commandSyntax(
      right = List(NumberType, NumberType), agentClassString = "-T--", switches = true)),
    ("_facexynowrap", Syntax.commandSyntax(
      right = List(NumberType, NumberType), agentClassString = "-T--", switches = true)),
    ("_fd", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--")),
    ("_fileatend", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_fileclose", Syntax.commandSyntax()),
    ("_filecloseall", Syntax.commandSyntax()),
    ("_filedelete", Syntax.commandSyntax(
      right = List(StringType))),
    ("_fileexists", Syntax.reporterSyntax(
      right = List(StringType), ret = BooleanType)),
    ("_fileflush", Syntax.commandSyntax()),
    ("_fileopen", Syntax.commandSyntax(
      right = List(StringType))),
    ("_fileprint", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_fileread", Syntax.reporterSyntax(
      ret = ReadableType)),
    ("_filereadchars", Syntax.reporterSyntax(
      right = List(NumberType), ret = StringType)),
    ("_filereadline", Syntax.reporterSyntax(
      ret = StringType)),
    ("_fileshow", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_filetype", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_filewrite", Syntax.commandSyntax(
      right = List(ReadableType))),
    ("_filter", Syntax.reporterSyntax(
      right = List(ReporterTaskType, ListType), ret = ListType)),
    ("_first", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = WildcardType)),
    ("_floor", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_follow", Syntax.commandSyntax(
      right = List(TurtleType), agentClassString = "O---", switches = true)),
    ("_followme", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_foreach", Syntax.commandSyntax(
      right = List(ListType | RepeatableType, CommandTaskType), defaultOption = Some(2))),
    ("_foreverbuttonend", Syntax.commandSyntax(
      switches = true)),
    ("_fput", Syntax.reporterSyntax(
      right = List(WildcardType, ListType), ret = ListType)),
    ("_git", Syntax.commandSyntax(
      right = List(StringType), agentClassString = "O---")),
    ("_greaterorequal", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 4, left = AgentType | NumberType | StringType, right = List(AgentType | NumberType | StringType), ret = BooleanType)),
    ("_greaterthan", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 4, left = AgentType | NumberType | StringType, right = List(AgentType | NumberType | StringType), ret = BooleanType)),
    ("_hatch", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType | OptionalType), agentClassString = "-T--", blockAgentClassString = "-T--", switches = true)),
    ("_hidelink", Syntax.commandSyntax(
      agentClassString = "---L", switches = true)),
    ("_hideturtle", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_histogram", Syntax.commandSyntax(
      right = List(ListType))),
    ("_home", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_hsb", Syntax.reporterSyntax(
      right = List(NumberType, NumberType, NumberType), ret = ListType)),
    ("_if", Syntax.commandSyntax(
      right = List(BooleanType, CommandBlockType))),
    ("_ifelse", Syntax.commandSyntax(
      right = List(BooleanType, CommandBlockType, CommandBlockType))),
    ("_ifelsevalue", Syntax.reporterSyntax(
      right = List(BooleanType, ReporterBlockType, ReporterBlockType), ret = WildcardType)),
    ("_ignore", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_importdrawing", Syntax.commandSyntax(
      right = List(StringType), agentClassString = "O---", switches = true)),
    ("_importpatchcolors", Syntax.commandSyntax(
      right = List(StringType), agentClassString = "O---", switches = true)),
    ("_importpcolorsrgb", Syntax.commandSyntax(
      right = List(StringType), agentClassString = "O---", switches = true)),
    ("_importworld", Syntax.commandSyntax(
      right = List(StringType), agentClassString = "O---", switches = true)),
    ("_incone", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = TurtlesetType | PatchsetType, right = List(NumberType, NumberType), ret = TurtlesetType | PatchsetType, blockAgentClassString = "-T--")),
    ("_inconenowrap", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = AgentsetType, right = List(NumberType, NumberType), ret = AgentsetType, blockAgentClassString = "-T--")),
    ("_inlinkfrom", Syntax.reporterSyntax(
      right = List(AgentType), ret = AgentType, agentClassString = "-T--")),
    ("_inlinkneighbor", Syntax.reporterSyntax(
      right = List(AgentType), ret = BooleanType, agentClassString = "-T--")),
    ("_inlinkneighbors", Syntax.reporterSyntax(
      ret = AgentsetType, agentClassString = "-T--")),
    ("_inradius", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = TurtlesetType | PatchsetType, right = List(NumberType), ret = TurtlesetType | PatchsetType, agentClassString = "-TP-")),
    ("_inradiusnowrap", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = AgentsetType, right = List(NumberType), ret = AgentsetType, agentClassString = "-TP-")),
    ("_inspect", Syntax.commandSyntax(
      right = List(AgentType))),
    ("_int", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_isagent", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isagentset", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isboolean", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_iscommandtask", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isdirectedlink", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_islink", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_islinkset", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_islist", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isnumber", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_ispatch", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_ispatchset", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isreportertask", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isstring", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isturtle", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isturtleset", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_isundirectedlink", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_item", Syntax.reporterSyntax(
      right = List(NumberType, StringType | ListType), ret = WildcardType)),
    ("_jump", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--", switches = true)),
    ("_last", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = WildcardType)),
    ("_layoutcircle", Syntax.commandSyntax(
      right = List(ListType | TurtlesetType, NumberType), switches = true)),
    ("_layoutradial", Syntax.commandSyntax(
      right = List(TurtlesetType, LinksetType, TurtleType), switches = true)),
    ("_layoutspring", Syntax.commandSyntax(
      right = List(TurtlesetType, LinksetType, NumberType, NumberType, NumberType), switches = true)),
    ("_layouttutte", Syntax.commandSyntax(
      right = List(TurtlesetType, LinksetType, NumberType), switches = true)),
    ("_left", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--", switches = true)),
    ("_length", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = NumberType)),
    ("_lessorequal", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 4, left = AgentType | NumberType | StringType, right = List(AgentType | NumberType | StringType), ret = BooleanType)),
    ("_lessthan", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 4, left = AgentType | NumberType | StringType, right = List(AgentType | NumberType | StringType), ret = BooleanType)),
    ("_let", Syntax.commandSyntax(
      right = List(WildcardType, WildcardType))),
    ("_link", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NobodyType | LinkType)),
    ("_linkcode", Syntax.commandSyntax(
      agentClassString = "---L")),
    ("_linkheading", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "---L")),
    ("_linklength", Syntax.reporterSyntax(
      ret = NumberType, agentClassString = "---L")),
    ("_linkneighbor", Syntax.reporterSyntax(
      right = List(AgentType), ret = BooleanType, agentClassString = "-T--")),
    ("_linkneighbors", Syntax.reporterSyntax(
      ret = AgentsetType, agentClassString = "-T--")),
    ("_links", Syntax.reporterSyntax(
      ret = LinksetType)),
    ("_linkset", Syntax.reporterSyntax(
      right = List(ListType | LinksetType | NobodyType | LinkType | RepeatableType), ret = LinksetType, defaultOption = Some(1), minimumOption = Some(0))),
    ("_linkshapes", Syntax.reporterSyntax(
      ret = ListType)),
    ("_linkwith", Syntax.reporterSyntax(
      right = List(AgentType), ret = LinkType, agentClassString = "-T--")),
    ("_list", Syntax.reporterSyntax(
      right = List(WildcardType | RepeatableType), ret = ListType, defaultOption = Some(2), minimumOption = Some(0))),
    ("_ln", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_log", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_loop", Syntax.commandSyntax(
      right = List(CommandBlockType))),
    ("_lput", Syntax.reporterSyntax(
      right = List(WildcardType, ListType), ret = ListType)),
    ("_makepreview", Syntax.commandSyntax(
      agentClassString = "O---")),
    ("_map", Syntax.reporterSyntax(
      right = List(ReporterTaskType, ListType | RepeatableType), ret = ListType, defaultOption = Some(2))),
    ("_max", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_maxnof", Syntax.reporterSyntax(
      right = List(NumberType, AgentsetType, NumberBlockType), ret = AgentsetType, blockAgentClassString = "?")),
    ("_maxoneof", Syntax.reporterSyntax(
      right = List(AgentsetType, NumberBlockType), ret = AgentType, blockAgentClassString = "?")),
    ("_maxpxcor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_maxpycor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_mean", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_median", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_member", Syntax.reporterSyntax(
      right = List(WildcardType, AgentsetType | StringType | ListType), ret = BooleanType)),
    ("_min", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_minnof", Syntax.reporterSyntax(
      right = List(NumberType, AgentsetType, NumberBlockType), ret = AgentsetType, blockAgentClassString = "?")),
    ("_minoneof", Syntax.reporterSyntax(
      right = List(AgentsetType, NumberBlockType), ret = AgentType, blockAgentClassString = "?")),
    ("_minpxcor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_minpycor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_minus", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 3, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_mkdir", Syntax.commandSyntax(
      right = List(StringType))),
    ("_mod", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 2, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_modes", Syntax.reporterSyntax(
      right = List(ListType), ret = ListType)),
    ("_mousedown", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_mouseinside", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_mousexcor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_mouseycor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_moveto", Syntax.commandSyntax(
      right = List(TurtleType | PatchType), agentClassString = "-T--", switches = true)),
    ("_mult", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 2, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_myinlinks", Syntax.reporterSyntax(
      ret = LinksetType, agentClassString = "-T--")),
    ("_mylinks", Syntax.reporterSyntax(
      ret = LinksetType, agentClassString = "-T--")),
    ("_myoutlinks", Syntax.reporterSyntax(
      ret = LinksetType, agentClassString = "-T--")),
    ("_myself", Syntax.reporterSyntax(
      ret = AgentType, agentClassString = "-TPL")),
    ("_nanotime", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_neighbors", Syntax.reporterSyntax(
      ret = PatchsetType, agentClassString = "-TP-")),
    ("_neighbors4", Syntax.reporterSyntax(
      ret = PatchsetType, agentClassString = "-TP-")),
    ("_netlogoapplet", Syntax.reporterSyntax(
      ret = BooleanType)),
    ("_netlogoversion", Syntax.reporterSyntax(
      ret = StringType)),
    ("_newseed", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_nodisplay", Syntax.commandSyntax()),
    ("_nof", Syntax.reporterSyntax(
      right = List(NumberType, AgentsetType | ListType), ret = AgentsetType | ListType)),
    ("_nolinks", Syntax.reporterSyntax(
      ret = LinksetType)),
    ("_nopatches", Syntax.reporterSyntax(
      ret = PatchsetType)),
    ("_not", Syntax.reporterSyntax(
      right = List(BooleanType), ret = BooleanType)),
    ("_notequal", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 5, left = WildcardType, right = List(WildcardType), ret = BooleanType)),
    ("_noturtles", Syntax.reporterSyntax(
      ret = TurtlesetType)),
    ("_nvalues", Syntax.reporterSyntax(
      right = List(NumberType, ReporterTaskType), ret = ListType)),
    ("_observercode", Syntax.commandSyntax(
      agentClassString = "O---")),
    ("_of", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 1, left = ReporterBlockType, right = List(AgentsetType | AgentType), ret = WildcardType, isRightAssociative = true, blockAgentClassString = "?")),
    ("_oneof", Syntax.reporterSyntax(
      right = List(AgentsetType | ListType), ret = WildcardType)),
    ("_or", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 6, left = BooleanType, right = List(BooleanType), ret = BooleanType)),
    ("_other", Syntax.reporterSyntax(
      right = List(AgentsetType), ret = AgentsetType)),
    ("_otherend", Syntax.reporterSyntax(
      ret = AgentType, agentClassString = "-T-L")),
    ("_outlinkneighbor", Syntax.reporterSyntax(
      right = List(AgentType), ret = BooleanType, agentClassString = "-T--")),
    ("_outlinkneighbors", Syntax.reporterSyntax(
      ret = AgentsetType, agentClassString = "-T--")),
    ("_outlinkto", Syntax.reporterSyntax(
      right = List(AgentType), ret = AgentType, agentClassString = "-T--")),
    ("_outputprint", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_outputshow", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_outputtype", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_outputwrite", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_patch", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NobodyType | PatchType)),
    ("_patchahead", Syntax.reporterSyntax(
      right = List(NumberType), ret = PatchType, agentClassString = "-T--")),
    ("_patchat", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NobodyType | PatchType, agentClassString = "-TP-")),
    ("_patchatheadinganddistance", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = PatchType, agentClassString = "-TP-")),
    ("_patchcode", Syntax.commandSyntax(
      agentClassString = "--P-")),
    ("_patchcol", Syntax.reporterSyntax(
      right = List(NumberType), ret = PatchsetType)),
    ("_patches", Syntax.reporterSyntax(
      ret = PatchsetType)),
    ("_patchhere", Syntax.reporterSyntax(
      ret = PatchType, agentClassString = "-T--")),
    ("_patchleftandahead", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = PatchType, agentClassString = "-T--")),
    ("_patchrightandahead", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = PatchType, agentClassString = "-T--")),
    ("_patchrow", Syntax.reporterSyntax(
      right = List(NumberType), ret = PatchsetType)),
    ("_patchset", Syntax.reporterSyntax(
      right = List(ListType | PatchsetType | NobodyType | PatchType | RepeatableType), ret = PatchsetType, defaultOption = Some(1), minimumOption = Some(0))),
    ("_patchsize", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_pendown", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_penerase", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_penup", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_plot", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_plotname", Syntax.reporterSyntax(
      ret = StringType)),
    ("_plotpendown", Syntax.commandSyntax()),
    ("_plotpenexists", Syntax.reporterSyntax(
      right = List(StringType), ret = BooleanType)),
    ("_plotpenhide", Syntax.commandSyntax()),
    ("_plotpenreset", Syntax.commandSyntax()),
    ("_plotpenshow", Syntax.commandSyntax()),
    ("_plotpenup", Syntax.commandSyntax()),
    ("_plotxmax", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_plotxmin", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_plotxy", Syntax.commandSyntax(
      right = List(NumberType, NumberType))),
    ("_plotymax", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_plotymin", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_plus", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 3, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_position", Syntax.reporterSyntax(
      right = List(WildcardType, StringType | ListType), ret = NumberType | BooleanType)),
    ("_pow", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 1, left = NumberType, right = List(NumberType), ret = NumberType)),
    ("_precision", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_print", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_processors", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_pwd", Syntax.commandSyntax(
      agentClassString = "O---")),
    ("_random", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_randomexponential", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_randomfloat", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_randomgamma", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_randomnormal", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_randompoisson", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_randompxcor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_randompycor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_randomseed", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_randomstate", Syntax.reporterSyntax(
      ret = StringType)),
    ("_randomxcor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_randomycor", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_readfromstring", Syntax.reporterSyntax(
      right = List(StringType), ret = ReadableType)),
    ("_reduce", Syntax.reporterSyntax(
      right = List(ReporterTaskType, ListType), ret = WildcardType)),
    ("_reloadextensions", Syntax.commandSyntax(
      switches = true)),
    ("_remainder", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_remove", Syntax.reporterSyntax(
      right = List(WildcardType, StringType | ListType), ret = StringType | ListType)),
    ("_removeduplicates", Syntax.reporterSyntax(
      right = List(ListType), ret = ListType)),
    ("_removeitem", Syntax.reporterSyntax(
      right = List(NumberType, StringType | ListType), ret = StringType | ListType)),
    ("_repeat", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType))),
    ("_replaceitem", Syntax.reporterSyntax(
      right = List(NumberType, StringType | ListType, WildcardType), ret = StringType | ListType)),
    ("_report", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_resetperspective", Syntax.commandSyntax(
      switches = true)),
    ("_resetticks", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_resettimer", Syntax.commandSyntax()),
    ("_resizeworld", Syntax.commandSyntax(
      right = List(NumberType, NumberType, NumberType, NumberType), agentClassString = "O---", switches = true)),
    ("_reverse", Syntax.reporterSyntax(
      right = List(StringType | ListType), ret = StringType | ListType)),
    ("_rgb", Syntax.reporterSyntax(
      right = List(NumberType, NumberType, NumberType), ret = ListType)),
    ("_ride", Syntax.commandSyntax(
      right = List(TurtleType), agentClassString = "O---", switches = true)),
    ("_rideme", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_right", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--", switches = true)),
    ("_round", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_run", Syntax.commandSyntax(
      right = List(StringType | CommandTaskType, WildcardType | RepeatableType), defaultOption = Some(1))),
    ("_runresult", Syntax.reporterSyntax(
      right = List(StringType | ReporterTaskType, WildcardType | RepeatableType), ret = WildcardType, defaultOption = Some(1))),
    ("_scalecolor", Syntax.reporterSyntax(
      right = List(NumberType, NumberType, NumberType, NumberType), ret = NumberType)),
    ("_self", Syntax.reporterSyntax(
      ret = AgentType, agentClassString = "-TPL")),
    ("_sentence", Syntax.reporterSyntax(
      right = List(WildcardType | RepeatableType), ret = ListType, defaultOption = Some(2), minimumOption = Some(0))),
    ("_set", Syntax.commandSyntax(
      right = List(WildcardType, WildcardType))),
    ("_setcurdir", Syntax.commandSyntax(
      right = List(StringType))),
    ("_setcurrentplot", Syntax.commandSyntax(
      right = List(StringType))),
    ("_setcurrentplotpen", Syntax.commandSyntax(
      right = List(StringType))),
    ("_setdefaultshape", Syntax.commandSyntax(
      right = List(TurtlesetType | LinksetType, StringType), agentClassString = "O---")),
    ("_sethistogramnumbars", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_setlinethickness", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "-T--", switches = true)),
    ("_setpatchsize", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "O---", switches = true)),
    ("_setplotpencolor", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_setplotpeninterval", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_setplotpenmode", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_setplotxrange", Syntax.commandSyntax(
      right = List(NumberType, NumberType))),
    ("_setplotyrange", Syntax.commandSyntax(
      right = List(NumberType, NumberType))),
    ("_setupplots", Syntax.commandSyntax()),
    ("_setxy", Syntax.commandSyntax(
      right = List(NumberType, NumberType), agentClassString = "-T--", switches = true)),
    ("_shadeof", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = BooleanType)),
    ("_shapes", Syntax.reporterSyntax(
      ret = ListType)),
    ("_show", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_showlink", Syntax.commandSyntax(
      agentClassString = "---L", switches = true)),
    ("_showturtle", Syntax.commandSyntax(
      agentClassString = "-T--", switches = true)),
    ("_shuffle", Syntax.reporterSyntax(
      right = List(ListType), ret = ListType)),
    ("_sin", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_sort", Syntax.reporterSyntax(
      right = List(AgentsetType | ListType), ret = ListType)),
    ("_sortby", Syntax.reporterSyntax(
      right = List(ReporterTaskType, AgentsetType | ListType), ret = ListType, blockAgentClassString = "?")),
    ("_sorton", Syntax.reporterSyntax(
      right = List(ReporterBlockType, AgentsetType), ret = ListType, blockAgentClassString = "?")),
    ("_sprout", Syntax.commandSyntax(
      right = List(NumberType, CommandBlockType | OptionalType), agentClassString = "--P-", blockAgentClassString = "-T--", switches = true)),
    ("_sqrt", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_stacktrace", Syntax.reporterSyntax(
      ret = StringType)),
    ("_stamp", Syntax.commandSyntax(
      agentClassString = "-T-L", switches = true)),
    ("_stamperase", Syntax.commandSyntax(
      agentClassString = "-T-L", switches = true)),
    ("_standarddeviation", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_stderr", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_stdout", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_stop", Syntax.commandSyntax()),
    ("_subject", Syntax.reporterSyntax(
      ret = AgentType)),
    ("_sublist", Syntax.reporterSyntax(
      right = List(ListType, NumberType, NumberType), ret = ListType)),
    ("_substring", Syntax.reporterSyntax(
      right = List(StringType, NumberType, NumberType), ret = StringType)),
    ("_subtractheadings", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType)),
    ("_sum", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_tan", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_task", Syntax.reporterSyntax(
      right = List(CommandTaskType | ReporterTaskType), ret = CommandTaskType | ReporterTaskType)),
    ("_thunkdidfinish", Syntax.commandSyntax()),
    ("_tick", Syntax.commandSyntax(
      agentClassString = "O---", switches = true)),
    ("_tickadvance", Syntax.commandSyntax(
      right = List(NumberType), agentClassString = "O---", switches = true)),
    ("_ticks", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_tie", Syntax.commandSyntax(
      agentClassString = "---L", switches = true)),
    ("_timer", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_tostring", Syntax.reporterSyntax(
      right = List(WildcardType), ret = StringType)),
    ("_towards", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType), ret = NumberType, agentClassString = "-TP-")),
    ("_towardsnowrap", Syntax.reporterSyntax(
      right = List(TurtleType | PatchType), ret = NumberType, agentClassString = "-TP-")),
    ("_towardsxy", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType, agentClassString = "-TP-")),
    ("_towardsxynowrap", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = NumberType, agentClassString = "-TP-")),
    ("_turtle", Syntax.reporterSyntax(
      right = List(NumberType), ret = NobodyType | TurtleType)),
    ("_turtlecode", Syntax.commandSyntax(
      agentClassString = "-T--")),
    ("_turtles", Syntax.reporterSyntax(
      ret = TurtlesetType)),
    ("_turtlesat", Syntax.reporterSyntax(
      right = List(NumberType, NumberType), ret = TurtlesetType, agentClassString = "-TP-")),
    ("_turtleset", Syntax.reporterSyntax(
      right = List(ListType | TurtlesetType | NobodyType | TurtleType | RepeatableType), ret = TurtlesetType, defaultOption = Some(1), minimumOption = Some(0))),
    ("_turtleshere", Syntax.reporterSyntax(
      ret = TurtlesetType, agentClassString = "-TP-")),
    ("_turtleson", Syntax.reporterSyntax(
      right = List(AgentsetType | AgentType), ret = TurtlesetType)),
    ("_type", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_untie", Syntax.commandSyntax(
      agentClassString = "---L", switches = true)),
    ("_updatemonitor", Syntax.commandSyntax(
      right = List(WildcardType), agentClassString = "O---", switches = true)),
    ("_updateplots", Syntax.commandSyntax()),
    ("_uphill", Syntax.commandSyntax(
      right = List(ReferenceType), agentClassString = "-T--", switches = true)),
    ("_uphill4", Syntax.commandSyntax(
      right = List(ReferenceType), agentClassString = "-T--", switches = true)),
    ("_userdirectory", Syntax.reporterSyntax(
      ret = BooleanType | StringType)),
    ("_userfile", Syntax.reporterSyntax(
      ret = BooleanType | StringType)),
    ("_userinput", Syntax.reporterSyntax(
      right = List(WildcardType), ret = StringType)),
    ("_usermessage", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_usernewfile", Syntax.reporterSyntax(
      ret = BooleanType | StringType)),
    ("_useroneof", Syntax.reporterSyntax(
      right = List(WildcardType, ListType), ret = WildcardType)),
    ("_useryesorno", Syntax.reporterSyntax(
      right = List(WildcardType), ret = BooleanType)),
    ("_variance", Syntax.reporterSyntax(
      right = List(ListType), ret = NumberType)),
    ("_wait", Syntax.commandSyntax(
      right = List(NumberType))),
    ("_watch", Syntax.commandSyntax(
      right = List(AgentType), agentClassString = "O---", switches = true)),
    ("_watchme", Syntax.commandSyntax(
      agentClassString = "-TPL", switches = true)),
    ("_while", Syntax.commandSyntax(
      right = List(BooleanBlockType, CommandBlockType))),
    ("_with", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = AgentsetType, right = List(BooleanBlockType), ret = AgentsetType, blockAgentClassString = "?")),
    ("_withlocalrandomness", Syntax.commandSyntax(
      right = List(CommandBlockType))),
    ("_withmax", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = AgentsetType, right = List(NumberBlockType), ret = AgentsetType, blockAgentClassString = "?")),
    ("_withmin", Syntax.reporterSyntax(
      precedence = NormalPrecedence + 2, left = AgentsetType, right = List(NumberBlockType), ret = AgentsetType, blockAgentClassString = "?")),
    ("_withoutinterruption", Syntax.commandSyntax(
      right = List(CommandBlockType))),
    ("_word", Syntax.reporterSyntax(
      right = List(WildcardType | RepeatableType), ret = StringType, defaultOption = Some(2), minimumOption = Some(0))),
    ("_worldheight", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_worldwidth", Syntax.reporterSyntax(
      ret = NumberType)),
    ("_wrapcolor", Syntax.reporterSyntax(
      right = List(NumberType), ret = NumberType)),
    ("_write", Syntax.commandSyntax(
      right = List(WildcardType))),
    ("_xor", Syntax.reporterSyntax(
      precedence = NormalPrecedence - 6, left = BooleanType, right = List(BooleanType), ret = BooleanType))
  )
}
