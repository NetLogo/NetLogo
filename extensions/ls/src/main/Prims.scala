package org.nlogo.ls

import java.io.IOException
import java.lang.{Boolean => JBoolean, Double => JDouble}
import java.net.MalformedURLException

import org.nlogo.api.{Argument, Command, Context, Dump, ExtensionException, Reporter}
import org.nlogo.core.{CompilerException, I18N, LogoList, Syntax, Token}
import org.nlogo.nvm.{Activation, Binding, ExtensionContext, HaltException, Context => NvmContext}
import org.nlogo.workspace.AbstractWorkspace

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.{Map => MMap, WeakHashMap => WeakMap}

object CtxConverter {
  def nvm(ctx: Context): NvmContext = ctx.asInstanceOf[ExtensionContext].nvmContext
}

class ScopedVals(elems: (Activation, AnyRef)*) extends WeakMap[Activation, AnyRef] {
  elems foreach { case (k, v) => this(k) = v }
}

class LetPrim extends Command {
  /*
  The Binding hierarchy roughly corresponds to NetLogo's scoping (except for things like `if`, which do not create new
  Binding objects). However, we can't actually use the Binding objects to hold our bindings since Let lookup works by
  reference rather than by name (so we end up with duplicate Lets in the case of, for instance, `repeat`; see issue
  #116). Thus, we use maps here, scoped by Binding, that we can do by name lookup quickly in such a way that scope is
  still mostly respected and we don't end up with duplicate bindings (corresponding to the same Binding anyway).

  One upshot of this is that `ls:let` can actually shadow each other. Preventing this would be a performance hit and I
  don't really see it as a bad thing anyway.
  -- BCH 1/20/2018
   */

  def bindings(ctx: NvmContext ): Map[String, AnyRef] =
    bindings(ctx.activation.binding)

  def bindings(binding: Binding): Map[String, AnyRef] = {
    if (binding == null)
      Map.empty[String, AnyRef]
    else
      bindings(binding.parent) ++ scopedBindings.get(binding).map(_.toMap).getOrElse(Map.empty[String, AnyRef])
  }

  private val scopedBindings = WeakMap[Binding, MMap[String, AnyRef]]()

  override def getSyntax: Syntax = Syntax.commandSyntax(List(Syntax.SymbolType, Syntax.ReadableType))

  override def perform(args: Array[Argument], ctx: Context): Unit = {
    val token = args(0).getSymbol
    val nvmCtx = CtxConverter.nvm(ctx)
    scopedBindings.getOrElseUpdate(nvmCtx.activation.binding, MMap.empty[String, AnyRef])(token.text) = args(1).get
  }
}

object ModelRunner {
  def buildCode(tokens: java.util.List[Token]): String = {
    val sb = new StringBuilder()
    sb.clear()
    tokens.forEach(t =>
      sb.append(t.text).append(' ')
    )
    sb.toString
  }

  def extractArgs(args: Array[Argument], pos: Int): Vector[AnyRef] =
    if (args.length > pos)
      args.drop(pos).map(_.get).toVector
    else
      Vector.empty[AnyRef]

  def run[R](ls: LevelSpace, allModels: LogoList, prun: ChildModel => Notifying[R], erun: HeadlessChildModel => Notifying[R]): Seq[Notifying[R]] = {
    val results = Array.ofDim[Notifying[R]](allModels.size)
    var i = 0
    allModels.foreach { id =>
      ls.getModel(LevelSpace.castToId(id)) match {
        case m: GUIChildModel =>
          results(i) = prun(m)
        case m: HeadlessChildModel => if (i > allModels.size - 4 || i > allModels.size * 0.9)
          results(i) = erun(m)
        else
          results(i) = prun(m)
      }
      i += 1
    }
    Seq(ArraySeq.unsafeWrapArray(results)*)
  }

}
class Ask(ls: LevelSpace) extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.NumberType | Syntax.ListType,
                                      Syntax.CodeBlockType,
                                      Syntax.RepeatableType | Syntax.ReadableType),
                         defaultOption = Some(2))

  override def perform(args: Array[Argument], ctx: Context): Unit = {
    val code = ModelRunner.buildCode(args(1).getCode)
    val cmdArgs = ModelRunner.extractArgs(args, 2)
    val lets = ls.letManager.bindings(CtxConverter.nvm(ctx)).toSeq
    val rng = RNG(ctx)
    args(0).get match {
      case i: JDouble => (ls.getModel(i.toInt) match {
        case m: HeadlessChildModel => m.tryEagerAsk(code, lets, cmdArgs, rng)
        case m => m.ask(code, lets, cmdArgs, rng)
      }).waitFor
      case l: LogoList =>
        ModelRunner.run(ls, l, _.ask(code, lets, cmdArgs, rng), _.tryEagerAsk(code, lets, cmdArgs, rng)).foreach(_.waitFor)
    }
  }
}

class Report(ls: LevelSpace) extends Reporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(right = List(Syntax.NumberType | Syntax.ListType,
                                       Syntax.CodeBlockType,
                                       Syntax.RepeatableType | Syntax.ReadableType),
                          ret = Syntax.ReadableType,
                          defaultOption = Some(2))

  override def report(args: Array[Argument], ctx: Context): AnyRef = {
    val code = ModelRunner.buildCode(args(1).getCode)
    val cmdArgs = ModelRunner.extractArgs(args, 2)
    val lets = ls.letManager.bindings(CtxConverter.nvm(ctx)).toSeq
    val rng = RNG(ctx)
    args(0).get match {
      case i: JDouble => (ls.getModel(i.toInt) match {
        case m: HeadlessChildModel => m.tryEagerOf(code, lets, cmdArgs, rng)
        case m => m.of(code, lets, cmdArgs, RNG(ctx))
      }).waitFor
      case l: LogoList =>
        LogoList.fromVector(
          ModelRunner.run(ls, l, _.of(code, lets, cmdArgs, rng), _.tryEagerOf(code, lets, cmdArgs, rng)).map(_.waitFor).toVector)
    }
  }
}

class Of(ls: LevelSpace) extends Report(ls) {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(left = Syntax.CodeBlockType,
                          right = List(Syntax.NumberType | Syntax.ListType),
                          ret = Syntax.ReadableType,
                          precedence = Syntax.NormalPrecedence + 1,
                          isRightAssociative = true)

  override def report(args: Array[Argument], ctx: Context): AnyRef =
    super.report(Array(args(1), args(0)), ctx)
}

class With(ls: LevelSpace) extends Reporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(left = Syntax.ListType,
                          right = List(Syntax.CodeBlockType),
                          ret = Syntax.ListType,
                          precedence = Syntax.NormalPrecedence + 2,
                          isRightAssociative = false)

  override def report(args: Array[Argument], ctx: Context): AnyRef = {
    val code = ModelRunner.buildCode(args(1).getCode)
    val cmdArgs = ModelRunner.extractArgs(args, 2)
    val lets = ls.letManager.bindings(CtxConverter.nvm(ctx)).toSeq
    val modelList = args(0).getList
    val rng = RNG(ctx)
    val results =
      ModelRunner.run(ls, modelList, _.of(code, lets, cmdArgs, rng), _.tryEagerOf(code, lets, cmdArgs, rng)).map(_.waitFor)
    LogoList.fromVector((modelList zip results).filter {
      case (_, b: java.lang.Boolean) =>
        b
      case (id: AnyRef, x: AnyRef) =>
        val m = ls.getModel(LevelSpace.castToId(id))
        throw new ExtensionException(I18N.errorsJ.getN("org.nlogo.prim.$common.expectedBooleanValue",
          "ls:with", m.name, Dump.logoObject(x)))
    }.map(_._1).toVector)
  }
}

class CreateModels[T <: ChildModel](ls: LevelSpace, createModel: (AbstractWorkspace, String) => ChildModel) extends Command {
  def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.NumberType, Syntax.StringType, Syntax.CommandType | Syntax.RepeatableType),
    defaultOption = Some(2)
  )

  def perform(args: Array[Argument], ctx: Context): Unit = {
    val parentWS = ctx.workspace.asInstanceOf[AbstractWorkspace]
    val modelPath = ctx.attachCurrentDirectory(args(1).getString)
    try {
      for (_ <- 0 until args(0).getIntValue) {
        val model = createModel(parentWS, modelPath)
        if (args.length > 2)
          args(2).getCommand.perform(ctx, Array[AnyRef](Double.box(model.modelID)))
      }
      ls.updateModelMenu()
    } catch {
      case e: MalformedURLException => throw new ExtensionException(e)
      case e: CompilerException =>
        throw new ExtensionException(modelPath + " has an error in its code: " + e.getMessage, e)
      case e: IOException =>
        throw new ExtensionException("NetLogo couldn't read the file \"" + modelPath + "\". Are you sure it exists and that NetLogo has permission to read it?", e)
      case _: InterruptedException => throw new HaltException(false)
    }
  }
}

class Reset(ls: LevelSpace) extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax()

  @throws[org.nlogo.api.LogoException]
  @throws[ExtensionException]
  override def perform(args: Array[Argument], context: Context): Unit = ls.reset()
}

class ModelCommand(ls: LevelSpace, cmd: ChildModel => Unit) extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(List(Syntax.NumberType | Syntax.ListType))
  override def perform(args: Array[Argument], ctx: Context): Unit = ls.toModelList(args(0)).foreach(cmd)
}

class ModelReporter(ls: LevelSpace, ret: Int, reporter: ChildModel => AnyRef) extends Reporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(right = List(Syntax.NumberType | Syntax.ListType), ret = ret)
  override def report(args: Array[Argument], ctx: Context): AnyRef = {
    val names = ls.toModelList(args(0)).map(reporter)
    if (args(0).get.isInstanceOf[LogoList]) LogoList.fromVector(names.toVector)
    else names.head
  }
}

class Show(ls: LevelSpace) extends ModelCommand(ls, _.show())
class Hide(ls: LevelSpace) extends ModelCommand(ls, _.hide())
class ShowAll(ls: LevelSpace) extends ModelCommand(ls, _.showAll())
class HideAll(ls: LevelSpace) extends ModelCommand(ls, _.hideAll())
class Close(ls: LevelSpace) extends ModelCommand(ls, ls.closeModel)
class Name(ls: LevelSpace) extends ModelReporter(ls, Syntax.StringType, _.name)
class Path(ls: LevelSpace) extends ModelReporter(ls, Syntax.StringType, _.path)
class UsesLS(ls: LevelSpace) extends ModelReporter(ls, Syntax.BooleanType, (model: ChildModel) => Boolean.box(model.usesLevelSpace))

class SetName(ls: LevelSpace) extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(List(Syntax.NumberType, Syntax.StringType))
  override def perform(args: Array[Argument], ctx: Context): Unit = ls.getModel(args(0).getIntValue).name = args(1).getString
}

class ModelExists(ls: LevelSpace) extends Reporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(right = List(Syntax.NumberType), ret = Syntax.BooleanType)
  override def report(args: Array[Argument], ctx: Context): JBoolean = Boolean.box(ls.containsModel(args(0).getIntValue))
}

class AllModels(ls: LevelSpace) extends Reporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(ret = Syntax.ListType)
  override def report(args: Array[Argument], ctx: Context): LogoList =
    LogoList.fromVector(ls.modelList.map(id => Double.box(id.doubleValue)).toVector)
}

class RandomSeed(ls: LevelSpace) extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.NumberType)
  )

  override def perform(args: Array[Argument], ctx: Context): Unit = {
    val l = args(0).getDoubleValue.toLong
    if (l < -2147483648 || l > 2147483647)
      throw new ExtensionException(
        Dump.number(l.toDouble) + " is not in the allowable range for random seeds (-2147483648 to 2147483647)")
    ctx.getRNG.setSeed(l)
    ls.modelList.foreach(i => ls.getModel(i).seedRNG(RNG(ctx), ctx.getRNG.nextInt))
  }
}

class Assign(ls: LevelSpace) extends Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.NumberType | Syntax.ListType, Syntax.SymbolType, Syntax.WildcardType),
    defaultOption = Some(3)

  )

  override def perform(args: Array[Argument], ctx: Context): Unit = {
    val rng = RNG(ctx)
    val globalName = args(1).getSymbol.text
    val setString = s"__ls-assign-var -> set $globalName __ls-assign-var"
    val setArgs = Seq(args(2).get)
    ls.toModelList(args(0)).map { m =>
      m.ask(setString, Seq(), setArgs, rng)
    }.foreach(_.waitFor)
  }
}
