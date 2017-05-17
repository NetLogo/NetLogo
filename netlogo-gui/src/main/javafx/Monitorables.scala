// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ core, internalapi, nvm },
    core.CompilerException,
    internalapi.{ ModelUpdate, Monitorable },
    nvm.Procedure

import
  scala.{ reflect, util },
    reflect.ClassTag,
    util.{ Failure, Success, Try }

// NOTE: This class isn't specific to javafx, but seemed like the most convenient place to put it.
trait ReporterMonitorable {
  def procedureTag: String
  def update(a: AnyRef): Unit
  def update(t: Try[AnyRef]): Unit =
    t.foreach(update _)
  def procedure: Procedure
}

// There is probably some way to unify this with ReporterMonitorable, but I haven't yet figured it out.
trait StaticMonitorable {
  def tags: Seq[String]
  def notifyUpdate(update: ModelUpdate): Unit
}

case class CompiledMonitorable[A](
  val defaultValue: A,
  val compilerError: Option[CompilerException],
  val procedureTag: String,
  val procedure: Procedure,
  val compiledSource: String)(implicit ct: ClassTag[A])
  extends Monitorable[A]
  with ReporterMonitorable {

  var currentValue: A = defaultValue

  var updateCallback: (A => Unit) = { (a: A) => }
  var errorCallback: (Exception => Unit) = { (e: Exception) => }

  def onUpdate(callback: A => Unit): Unit = {
    updateCallback = callback
  }

  def onError(callback: Exception => Unit): Unit = {
    errorCallback = callback
  }

  def update(value: AnyRef): Unit = {
    value match {
      case a: A  =>
        currentValue = a
        updateCallback(a)
      case other =>
    }
  }

  override def update(tryValue: Try[AnyRef]): Unit = {
    tryValue match {
      case Success(s) => update(s)
      case Failure(e: Exception) => errorCallback(e)
      case Failure(e) => throw e
    }
  }
}

case class MappedMonitorable[A, B](
  val defaultValue: B,
  val compilerError: Option[CompilerException],
  val procedureTag: String,
  val procedure: Procedure,
  val compiledSource: String,
  transform: A => B)(implicit ct: ClassTag[A]) extends Monitorable[B] with ReporterMonitorable {

  def this(c: CompiledMonitorable[A], transform: A => B)(implicit ct: ClassTag[A]) =
    this(transform(c.defaultValue), c.compilerError, c.procedureTag, c.procedure, c.compiledSource, transform)

  var currentValue: B = defaultValue

  var updateCallback: (B => Unit) = { (b: B) => }
  var errorCallback: (Exception => Unit) = { (e: Exception) => }

  def onUpdate(callback: B => Unit): Unit = {
    updateCallback = callback
  }

  def onError(callback: Exception => Unit): Unit = {
    errorCallback = callback
  }

  def update(value: AnyRef): Unit = {
    value match {
      case a: A  =>
        currentValue = transform(a)
        updateCallback(currentValue)
      case other =>
    }
  }

  override def update(tryValue: Try[AnyRef]): Unit = {
    tryValue match {
      case Success(s) => update(s)
      case Failure(e: Exception) => errorCallback(e)
      case Failure(e) => throw e
    }
  }
}

case class NonCompiledMonitorable[A](val defaultValue: A) extends Monitorable[A] {
  val currentValue: A = defaultValue
  def onUpdate(callback: A => Unit): Unit = {}
  def onError(callback: Exception => Unit): Unit = {}
  def compilerError = None
  def procedureTag = ""
}
