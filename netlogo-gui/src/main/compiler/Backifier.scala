// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import scala.collection.immutable.ListMap
import org.nlogo.core.{ CompilerException, Fail, I18N, Instantiator, Program, BreedIdentifierHandler }, Fail.exception
import org.nlogo.{ api, core, nvm, prim => nvmprim }

class Backifier(program: Program,
  extensionManager: core.ExtensionManager,
  procedures: ListMap[String, nvm.Procedure]) {

  val replacements = Map[String, String](
    "org.nlogo.prim._inradius"            -> "org.nlogo.prim.etc._inradius",
    "org.nlogo.prim._report"              -> "org.nlogo.prim.etc._report",
    "org.nlogo.prim._run"                 -> "org.nlogo.prim.etc._run",
    "org.nlogo.prim._stop"                -> "org.nlogo.prim.etc._stop",
    "org.nlogo.prim.etc._plus"            -> "org.nlogo.prim._plus",
    "org.nlogo.prim.etc._breedon"         -> "org.nlogo.prim._breedon",
    "org.nlogo.prim.etc._breedat"         -> "org.nlogo.prim._breedat",
    "org.nlogo.prim.etc._isbreed"         -> "org.nlogo.prim._isbreed",
    "org.nlogo.prim.etc._link"            -> "org.nlogo.prim._link",
    "org.nlogo.prim.etc._linkwith"        -> "org.nlogo.prim._linkwith",
    "org.nlogo.prim.etc._linkbreed"       -> "org.nlogo.prim._linkbreed",
    "org.nlogo.prim.etc._linkbreedsingular" -> "org.nlogo.prim._linkbreedsingular",
    "org.nlogo.prim.etc._linkneighbors"       -> "org.nlogo.prim._linkneighbors",
    "org.nlogo.prim.etc._linkneighbor"       -> "org.nlogo.prim._linkneighbor",
    "org.nlogo.prim.etc._links"           -> "org.nlogo.prim._links",
    "org.nlogo.prim.etc._inlinkneighbor"  -> "org.nlogo.prim._inlinkneighbor",
    "org.nlogo.prim.etc._patch"           -> "org.nlogo.prim._patch",
    "org.nlogo.prim.etc._breedhere"       -> "org.nlogo.prim._breedhere",
    "org.nlogo.prim.etc._breedsingular"   -> "org.nlogo.prim._breedsingular",
    "org.nlogo.prim.etc._createlinkfrom"  -> "org.nlogo.prim._createlinkfrom",
    "org.nlogo.prim.etc._createlinkto"    -> "org.nlogo.prim._createlinkto",
    "org.nlogo.prim.etc._createlinkwith"  -> "org.nlogo.prim._createlinkwith",
    "org.nlogo.prim.etc._createlinksto"   -> "org.nlogo.prim._createlinksto",
    "org.nlogo.prim.etc._createlinksfrom" -> "org.nlogo.prim._createlinksfrom",
    "org.nlogo.prim.etc._createlinkfrom"  -> "org.nlogo.prim._createlinkfrom",
    "org.nlogo.prim.etc._mylinks"         -> "org.nlogo.prim._mylinks",
    "org.nlogo.prim.etc._myinlinks"       -> "org.nlogo.prim._myinlinks",
    "org.nlogo.prim.etc._myoutlinks"      -> "org.nlogo.prim._myoutlinks",
    "org.nlogo.prim.etc._inlinkneighbors" -> "org.nlogo.prim._inlinkneighbors",
    "org.nlogo.prim.etc._inlinkfrom"      -> "org.nlogo.prim._inlinkfrom",
    "org.nlogo.prim.etc._outlinkto"       -> "org.nlogo.prim._outlinkto",
    "org.nlogo.prim.etc._outlinkneighbor" -> "org.nlogo.prim._outlinkneighbor",
    "org.nlogo.prim.etc._outlinkneighbors" -> "org.nlogo.prim._outlinkneighbors",
    "org.nlogo.prim.etc._symbolstring"    -> "org.nlogo.prim.etc._symbol",
    "org.nlogo.prim.etc._exportplot"      -> "org.nlogo.prim.plot._exportplot",
    "org.nlogo.prim.etc._exportplots"     -> "org.nlogo.prim.plot._exportplots",
    "org.nlogo.prim.etc._exportinterface" -> "org.nlogo.prim.gui._exportinterface",
    "org.nlogo.prim.etc._filedelete"      -> "org.nlogo.prim.file._filedelete",
    "org.nlogo.prim.etc._fileexists"      -> "org.nlogo.prim.file._fileexists",
    "org.nlogo.prim.etc._fileflush"       -> "org.nlogo.prim.file._fileflush",
    "org.nlogo.prim.etc._fileopen"        -> "org.nlogo.prim.file._fileopen",
    "org.nlogo.prim.etc._fileshow"        -> "org.nlogo.prim.file._fileshow",
    "org.nlogo.prim.etc._filewrite"       -> "org.nlogo.prim.file._filewrite",
    "org.nlogo.prim.etc._fileprint"       -> "org.nlogo.prim.file._fileprint",
    "org.nlogo.prim.etc._fileclose"       -> "org.nlogo.prim.file._fileclose",
    "org.nlogo.prim.etc._filecloseall"    -> "org.nlogo.prim.file._filecloseall",
    "org.nlogo.prim.etc._fileread"        -> "org.nlogo.prim.file._fileread",
    "org.nlogo.prim.etc._filereadchars"   -> "org.nlogo.prim.file._filereadchars",
    "org.nlogo.prim.etc._filereadline"    -> "org.nlogo.prim.file._filereadline",
    "org.nlogo.prim.etc._fileatend"       -> "org.nlogo.prim.file._fileatend",
    "org.nlogo.prim.etc._filetype"        -> "org.nlogo.prim.file._filetype",
    "org.nlogo.prim.etc._sethistogramnumbars" -> "org.nlogo.prim.plot._sethistogramnumbars",
    "org.nlogo.prim.etc._histogram"       -> "org.nlogo.prim.plot._histogram",
    "org.nlogo.prim.etc._plotname"        -> "org.nlogo.prim.plot._plotname",
    "org.nlogo.prim.etc._plotpendown"     -> "org.nlogo.prim.plot._plotpendown",
    "org.nlogo.prim.etc._plotpenexists"   -> "org.nlogo.prim.plot._plotpenexists",
    "org.nlogo.prim.etc._plotpenhide"     -> "org.nlogo.prim.plot._plotpenhide",
    "org.nlogo.prim.etc._plotpenexists"   -> "org.nlogo.prim.plot._plotpenexists",
    "org.nlogo.prim.etc._plotpenreset"    -> "org.nlogo.prim.plot._plotpenreset",
    "org.nlogo.prim.etc._plotpenshow"     -> "org.nlogo.prim.plot._plotpenshow",
    "org.nlogo.prim.etc._plotpenup"       -> "org.nlogo.prim.plot._plotpenup",
    "org.nlogo.prim.etc._plotymin"        -> "org.nlogo.prim.plot._plotymin",
    "org.nlogo.prim.etc._plotymax"        -> "org.nlogo.prim.plot._plotymax",
    "org.nlogo.prim.etc._plotxmin"        -> "org.nlogo.prim.plot._plotxmin",
    "org.nlogo.prim.etc._plotxmax"        -> "org.nlogo.prim.plot._plotxmax",
    "org.nlogo.prim.etc._setplotxrange"   -> "org.nlogo.prim.plot._setplotxrange",
    "org.nlogo.prim.etc._setplotpencolor"   -> "org.nlogo.prim.plot._setplotpencolor",
    "org.nlogo.prim.etc._setplotpenmode"   -> "org.nlogo.prim.plot._setplotpenmode",
    "org.nlogo.prim.etc._setplotpeninterval"   -> "org.nlogo.prim.plot._setplotpeninterval",
    "org.nlogo.prim.etc._plotxy"          -> "org.nlogo.prim.plot._plotxy",
    "org.nlogo.prim.etc._setcurrentplotpen" -> "org.nlogo.prim.plot._setcurrentplotpen",
    "org.nlogo.prim.etc._setplotyrange"   -> "org.nlogo.prim.plot._setplotyrange",
    "org.nlogo.prim.etc._clearallplots"   -> "org.nlogo.prim.plot._clearallplots",
    "org.nlogo.prim.etc._clearplot"   -> "org.nlogo.prim.plot._clearplot",
    "org.nlogo.prim.etc._autoplotoff"   -> "org.nlogo.prim.plot._autoplotoff",
    "org.nlogo.prim.etc._autoploton"   -> "org.nlogo.prim.plot._autoploton",
    "org.nlogo.prim.etc._updateplots"     -> "org.nlogo.prim.plot._updateplots",
    "org.nlogo.prim.etc._plot"            -> "org.nlogo.prim.plot._plot",
    "org.nlogo.prim.etc._setupplots"      -> "org.nlogo.prim.plot._setupplots",
    "org.nlogo.prim.etc._setcurrentplot"  -> "org.nlogo.prim.plot._setcurrentplot",
    "org.nlogo.prim.etc._createtemporaryplotpen" -> "org.nlogo.prim.plot._createtemporaryplotpen",
    "org.nlogo.prim.etc._createlinkswith" -> "org.nlogo.prim._createlinkswith",
    "org.nlogo.prim.etc._inspect"         -> "org.nlogo.prim.gui._inspect",
    "org.nlogo.prim.etc._stopinspecting"  -> "org.nlogo.prim.gui._stopinspecting",
    "org.nlogo.prim.etc._stopinspectingdeadagents" -> "org.nlogo.prim.gui._stopinspectingdeadagents",
    "org.nlogo.prim.etc._usernewfile"     -> "org.nlogo.prim.gui._usernewfile",
    "org.nlogo.prim.etc._usernewfile"     -> "org.nlogo.prim.gui._usernewfile",
    "org.nlogo.prim.etc._userdirectory"   -> "org.nlogo.prim.gui._userdirectory",
    "org.nlogo.prim.etc._userfile"        -> "org.nlogo.prim.gui._userfile",
    "org.nlogo.prim.etc._userinput"       -> "org.nlogo.prim.gui._userinput",
    "org.nlogo.prim.etc._usermessage"     -> "org.nlogo.prim.gui._usermessage",
    "org.nlogo.prim.etc._useryesorno"     -> "org.nlogo.prim.gui._useryesorno",
    "org.nlogo.prim.etc._useroneof"       -> "org.nlogo.prim.gui._useroneof",
    "org.nlogo.prim.etc._usermessage"     -> "org.nlogo.prim.gui._usermessage",
    "org.nlogo.prim.etc._mousexcor"       -> "org.nlogo.prim.gui._mousexcor",
    "org.nlogo.prim.etc._mouseycor"       -> "org.nlogo.prim.gui._mouseycor",
    "org.nlogo.prim.etc._mousedown"       -> "org.nlogo.prim.gui._mousedown",
    "org.nlogo.prim.etc._mouseinside"     -> "org.nlogo.prim.gui._mouseinside",
    "org.nlogo.prim.threed._load3Dshapes" -> "org.nlogo.prim.gui._load3Dshapes",
    "org.nlogo.prim.threed._face"         -> "org.nlogo.prim.etc._face"
  )

  private def backifyName(name: String): String = {
      val alteredName = name.replaceFirst("\\.core\\.", ".").replaceFirst("\\.compiler\\.", ".")
      if (replacements.contains(alteredName))
        replacements(alteredName)
      else
        alteredName
  }

  private def fallback[T1 <: core.Instruction, T2 <: nvm.Instruction](i: T1): T2 =
    BreedIdentifierHandler.process(i.token.copy(value = i.token.text.toUpperCase), program) match {
      case None =>
        try {
          val klass = Class.forName(backifyName(i.getClass.getName))
          Instantiator.newInstance[T2](klass)
        } catch {
          case e: ClassNotFoundException =>
            i match {
              case replaced: ReplacedPrim =>
                exception(I18N.errors.getN("compiler.Backifier.replaced", i.token.text, replaced.recommendedReplacement), i.token)
              case _ =>
                exception(I18N.errors.getN("compiler.LocalsVisitor.notDefined", i.token.text), i.token)
            }
        }
      case Some((className, breedName, _)) =>
        val name = "org.nlogo.prim." + className
        val primName = if (replacements.contains(name)) replacements(name) else name
        Instantiator.newInstance[T2](
          Class.forName(primName), breedName)
    }

  def apply(c: core.Command): nvm.Command = {
    val result: nvm.Command = c match {
      case core.prim._extern(_) =>
        new nvmprim._extern(
          extensionManager.replaceIdentifier(c.token.text.toUpperCase)
            .asInstanceOf[api.Command])
      case core.prim._call(proc) =>
        new nvmprim._call(procedures(proc.name))
      case core.prim._let(let) =>
        val l = new nvmprim._let()
        l.let = let
        l
      case api.NetLogoLegacyDialect._magicopen(name) =>
        new nvmprim._magicopen(name)
      case cc: core.prim._carefully =>
        new nvmprim._carefully(cc.let)
      case _ =>
        fallback[core.Command, nvm.Command](c)
    }
    result.token_=(c.token)
    result.agentClassString = c.agentClassString
    result
  }

  def apply(r: core.Reporter): nvm.Reporter = {
    val result: nvm.Reporter = r match {

      case core.prim._letvariable(let) =>
        new nvmprim._letvariable(let, let.name)

      case core.prim._const(value) =>
        value match {
          case d: java.lang.Double   => new nvmprim._constdouble(d)
          case b: java.lang.Boolean  => new nvmprim._constboolean(b)
          case l: core.LogoList      => new nvmprim._constlist(l)
          case core.Nobody           => new nvmprim._nobody()
          case s: String             => new nvmprim._conststring(s)
        }

      case core.prim._constcodeblock(toks) =>
        new nvmprim._constcodeblock(toks)

      case core.prim._commandtask(argcount) =>
        new nvmprim._commandtask(argcount)  // LambdaLifter will fill in

      case core.prim._reportertask(argcount) =>
        new nvmprim._reportertask()

      case core.prim._externreport(_) =>
        new nvmprim._externreport(
          extensionManager.replaceIdentifier(r.token.text.toUpperCase)
            .asInstanceOf[api.Reporter])

      case core.prim._breedvariable(varName) =>
        new nvmprim._breedvariable(varName)
      case core.prim._linkbreedvariable(varName) =>
        new nvmprim._linkbreedvariable(varName)

      case core.prim._procedurevariable(vn, name) =>
        new nvmprim._procedurevariable(vn, name)
      case core.prim._taskvariable(vn) =>
        new nvmprim._taskvariable(vn)

      case core.prim._observervariable(vn, _) =>
        new nvmprim._observervariable(vn)
      case core.prim._turtlevariable(vn, _) =>
        new nvmprim._turtlevariable(vn)
      case core.prim._linkvariable(vn, _) =>
        new nvmprim._linkvariable(vn)
      case core.prim._patchvariable(vn, _) =>
        new nvmprim._patchvariable(vn)
      case core.prim._turtleorlinkvariable(varName, _) =>
        new nvmprim._turtleorlinkvariable(varName)

      case core.prim._callreport(proc) =>
        new nvmprim._callreport(procedures(proc.name))

      case core.prim._errormessage(Some(let)) =>
        new nvmprim._errormessage(let)
      case core.prim._errormessage(None) =>
        throw new Exception("Parse error - errormessage not matched with carefully")

      // diabolical special case: if we have e.g. `breed [fish]` with no singular,
      // then the singular defaults to `turtle`, which will cause BreedIdentifierHandler
      // to interpret "turtle" as _breedsingular - ST 4/12/14
      case core.prim._turtle() =>
        new nvmprim._turtle()

      case s: core.prim._symbol =>
        new nvmprim._constsymbol(s.token)

      case _ =>
        fallback[core.Reporter, nvm.Reporter](r)

    }
    result.token_=(r.token)
    result.agentClassString = r.agentClassString
    result
  }
}
