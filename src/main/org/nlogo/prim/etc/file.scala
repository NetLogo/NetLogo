// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ CompilerException, FileMode, OutputDestination, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException, Reporter }
import java.io.IOException

class _fileatend extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    try Boolean.box(workspace.fileManager.eof)
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}

class _fileclose extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    try
      if (workspace.fileManager.hasCurrentFile)
        workspace.fileManager.closeCurrentFile()
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}

class _filecloseall extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    try workspace.fileManager.closeAllFiles()
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}

class _filedelete extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(context: Context) {
    try
      workspace.fileManager.deleteFile(
        workspace.fileManager.attachPrefix(
          argEvalString(context, 0)))
    catch {
      case ex: java.net.MalformedURLException =>
        throw new EngineException(
          context, this, argEvalString(context, 0) +
          " is not a valid path name: " + ex.getMessage)
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}

class _fileexists extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.StringType),
                          Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    try
      Boolean.box(
        workspace.fileManager.fileExists(
          workspace.fileManager.attachPrefix(
            argEvalString(context, 0))))
    catch {
      case ex: java.net.MalformedURLException =>
        throw new EngineException(
          context, this, argEvalString(context, 0) +
          " is not a valid path name: " + ex.getMessage)
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}

class _fileflush extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    try
      if (workspace.fileManager.hasCurrentFile)
        workspace.fileManager.flushCurrentFile()
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}

class _fileopen extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(context: Context) {
    try
      // DefaultFileManager.openFile attaches the prefix for us, so we need not normalize our path
      // before calling that method - CLB 05/17/05
      workspace.fileManager.openFile(
        argEvalString(context, 0))
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    context.ip = next
  }
}

class _fileprint extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    try
      workspace.fileManager.ensureMode(
        org.nlogo.api.FileMode.Append)
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    workspace.outputObject(
      args(0).report(context), null, true, false,
      OutputDestination.File)
    context.ip = next
  }
}

class _fileread extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.ReadableType)
  override def report(context: Context): AnyRef =
    try workspace.fileManager.read(world)
    catch {
      case ex: CompilerException =>
        throw new EngineException(
          context, this,
          ex.getMessage + workspace.fileManager.getErrorInfo)
      case ex: java.io.EOFException =>
        throw new EngineException(
          context, this, "The end of file has been reached")
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}

class _filereadchars extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType),
                          Syntax.StringType)
  override def report(context: Context): String =
    try workspace.fileManager.readChars(argEvalIntValue(context, 0))
    catch {
      case _: java.io.EOFException =>
        throw new EngineException(
          context, this, "The end of file has been reached")
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}

class _filereadline extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context): String =
    try workspace.fileManager.readLine()
    catch {
      case _: java.io.EOFException =>
        throw new EngineException(
          context, this, "The end of file has been reached")
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}

class _fileshow extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    val s = args(0).report(context)
    try
      workspace.fileManager.ensureMode(FileMode.Append)
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    workspace.outputObject(s, context.agent, true, true,
                           OutputDestination.File)
    context.ip = next
  }
}

class _filetype extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    val s = args(0).report(context)
    try workspace.fileManager.ensureMode(FileMode.Append)
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    workspace.outputObject(s, null, false, false,
                           OutputDestination.File)
    context.ip = next
  }
}

class _filewrite extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.ReadableType))
  override def perform(context: Context) {
    val s = args(0).report(context)
    try
      workspace.fileManager.ensureMode(FileMode.Append)
    catch {
      case ex: IOException =>
        throw new EngineException(context, this, ex.getMessage)
    }
    workspace.outputObject(s, null, false, true,
                           OutputDestination.File)
    context.ip = next
  }
}
