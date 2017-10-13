// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ BufferedReader, IOException, Reader }

import org.nlogo.core.{ CompilerException, File, FileMode }
import org.nlogo.api.{ Dump, Exceptions, LocalFile, LogoException, OutputDestination, Workspace => ApiWorkspace }
import org.nlogo.agent.{ Agent, ImporterJ, OutputObject, World }

object Importing {
  abstract class FileImporter(val filename: String) {
    @throws(classOf[IOException])
    def doImport(reader: File): Unit
  }
}

import Importing.FileImporter

// this extends ApiWorkspace to prevent a abstract/default conflict in GUIWorkspace
trait Importing extends ApiWorkspace { self: AbstractWorkspace =>
  def world: World

  @throws(classOf[IOException])
    def importWorld(filename: String): Unit = {
      // we need to clearAll before we import in case
      // extensions are hanging on to old data. ev 4/10/09
      clearAll()
      doImport(new BufferedReaderImporter(filename) {
        @throws(classOf[IOException])
        override def doImport(reader: BufferedReader): Unit = {
          world.importWorld(importerErrorHandler, self, stringReader, reader)
        }
      })
    }

    @throws(classOf[IOException])
    def importWorld(reader: Reader): Unit = {
      // we need to clearAll before we import in case
      // extensions are hanging on to old data. ev 4/10/09
      clearAll()
      world.importWorld(importerErrorHandler, self, stringReader, new java.io.BufferedReader(reader))
    }

    private final def stringReader: ImporterJ.StringReader = {
      new ImporterJ.StringReader() {
        @throws(classOf[ImporterJ.StringReaderException])
        def readFromString(s: String): AnyRef = {
          try {
            return compiler.readFromString(s, world, extensionManager)
          } catch {
            case ex: CompilerException => throw new ImporterJ.StringReaderException(ex.getMessage)
          }
        }
      }
    }

  // overridden in subclasses - ST 9/8/03, 3/1/11
  @throws(classOf[IOException])
  def doImport(importer: BufferedReaderImporter): Unit = {
    val file = new LocalFile(importer.filename)
    try {
      file.open(FileMode.Read)
      importer.doImport(file.reader)
    } finally {
      try {
        file.close(false)
      } catch {
        case ex2: IOException => Exceptions.ignore(ex2)
      }
    }
  }

  @throws(classOf[IOException])
  protected def doImport(importer: Importing.FileImporter) {
    val newFile = new LocalFile(importer.filename)

    importer.doImport(newFile);
  }

  @throws(classOf[IOException])
  def importDrawing(filename: String): Unit = {
    val importer = new FileImporter(filename) {
      @throws(classOf[IOException])
      override def doImport(file: File): Unit = {
        importDrawing(file);
      }
    }
    doImport(importer)
  }

  // used by import world
  def setOutputAreaContents(text: String): Unit = {
    try {
      clearOutput()
      if (text.length() > 0) {
        sendOutput(new OutputObject("", text, false, false), true)
      }
    } catch {
      case e: LogoException => Exceptions.handle(e)
    }
  }

  @throws(classOf[IOException])
  protected def importDrawing(file: File): Unit

  protected def importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler

  @throws(classOf[LogoException])
  protected def sendOutput(oo: OutputObject, toOutputArea: Boolean): Unit

  def outputObject(obj: AnyRef,
    owner: AnyRef,
    addNewline: Boolean,
    readable: Boolean,
    destination: OutputDestination): Unit = {
      val caption = owner match {
        case a: Agent => Dump.logoObject(owner)
        case _        => ""
      }
      val message = ((owner match {
        case a: Agent      => ""
        case _ if readable => " "
        case _             => ""
      }) + Dump.logoObject(obj, readable, false))
      val oo = new OutputObject(caption, message, addNewline, false);
      destination match {
        case OutputDestination.File => fileManager.writeOutputObject(oo)
        case _ =>
          sendOutput(oo, destination == OutputDestination.OutputArea)
      }
  }
}
