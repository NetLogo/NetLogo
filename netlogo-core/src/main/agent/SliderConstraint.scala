// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.lang.{ Double => JDouble }

import org.nlogo.api.{ CompilerServices, LogoException, LogoThunkFactory, ReporterLogoThunk, ValueConstraint }
import org.nlogo.core.{ CompilerException, I18N }

import scala.util.{ Failure, Success, Try }

object SliderConstraint {

  case class Spec(fieldName: String, displayName:String)
  val Min = Spec("minimumCode", I18N.gui.get("edit.slider.minimum"))
  val Max = Spec("maximumCode", I18N.gui.get("edit.slider.maximum"))
  val Inc = Spec("incrementCode", I18N.gui.get("edit.slider.increment"))

  trait SliderConstraintException extends Exception { val spec: Spec }
  class ConstraintCompilerException(val spec: Spec, ex: CompilerException) extends
    CompilerException(ex.getMessage, ex.start, ex.end, ex.filename) with SliderConstraintException
  class ConstraintRuntimeException(val spec:Spec, message: String) extends
    RuntimeException("Constraint runtime error for " + spec.displayName + ": " + message) with SliderConstraintException

  class ConstraintExceptionHolder(errors: List[SliderConstraintException]) extends Exception {
    def getErrors: List[SliderConstraintException] = errors
  }

  @throws(classOf[ConstraintExceptionHolder])
  def makeSliderConstraint(agent: Agent, minCode: String, maxCode: String, incCode: String, defValue: Double,
                           ownerName: String, thunkFactory: LogoThunkFactory, compiler: CompilerServices): SliderConstraint = {
    abstract class ConstraintCompiler[T] {
      def compile(code: String, spec: Spec): Try[T]
      def makeConstraint(minT: T, maxT: T, incT: T): SliderConstraint
      def compileAll(minCode: String, maxCode: String, incCode: String): SliderConstraint = {
        val (minT, maxT, incT) = (compile(minCode, Min), compile(maxCode, Max), compile(incCode, Inc))
        val allErrors = List(minT, maxT, incT).collect {
          case Failure(e: SliderConstraintException) => e
        }
        if (!allErrors.isEmpty) throw new ConstraintExceptionHolder(allErrors)
        // no compilation errors, so return a new constraint.
        makeConstraint(minT.get, maxT.get, incT.get)
      }
    }
    object ConstantConstraintCompiler extends ConstraintCompiler[Double] {
      def compile(code: String, spec: Spec) = {
        compiler.readFromString(code) match {
          case d: JDouble => Success(d)
          case _ => Failure(new ConstraintRuntimeException(spec, "constraint must be a number."))
        }
      }
      def makeConstraint(minT: Double, maxT: Double, incT: Double) = new ConstantSliderConstraint(minT, maxT, incT)
    }
    object DynamicConstraintCompiler extends ConstraintCompiler[ReporterLogoThunk] {
      def compile(code: String, spec: Spec) = {
        try Success(thunkFactory.makeReporterThunk(code, "slider '" + ownerName + "' " + spec.displayName))
        catch {case ex: CompilerException => Failure(new ConstraintCompilerException(spec, ex))}
      }
      def makeConstraint(minT: ReporterLogoThunk, maxT: ReporterLogoThunk, incT: ReporterLogoThunk) =
        new DynamicSliderConstraint(minT, maxT, incT)
    }
    val allConstants =
      compiler.isConstant(minCode) && compiler.isConstant(maxCode) && compiler.isConstant(incCode)
    val constraintCompiler = if (allConstants) ConstantConstraintCompiler else DynamicConstraintCompiler
    val con: SliderConstraint = constraintCompiler.compileAll(minCode, maxCode, incCode)
    con.defaultValue=defValue
    con
  }
}

abstract class SliderConstraint extends ValueConstraint {
  def minimum: Try[Double]
  def increment: Try[Double]
  def maximum: Try[Double]
  var defaultValue: JDouble = World.Zero
  def assertConstraint(o: Object): Unit = {
    if (!(o.isInstanceOf[Double])) { throw new ValueConstraint.Violation("Value must be a number.") }
  }
  def coerceValue(o: Object): Object = if (o.isInstanceOf[Double]) o else defaultValue
}

case class ConstantSliderConstraint(min: Double, max: Double, inc: Double) extends SliderConstraint {
  def minimum: Try[Double] = Success(min)
  def increment: Try[Double] = Success(inc)
  def maximum: Try[Double] = Success(max)
}

/**
 * Constraint suitable for Slider variables.  The various limits on the
 * Slider value can be specified using a NetLogo reporter.
 */
class DynamicSliderConstraint(min: ReporterLogoThunk,
                              max: ReporterLogoThunk,
                              inc: ReporterLogoThunk) extends SliderConstraint {
  import SliderConstraint._
  override def minimum = get(Min, min)
  override def maximum = get(Max, max)
  override def increment = get(Inc, inc)
  private def get(spec:Spec, thunk:ReporterLogoThunk): Try[Double] = {
    thunk.call()
      .flatMap(res => Try(res.asInstanceOf[Double]))
      .recoverWith {
        case ex: ClassCastException => Failure(new ConstraintRuntimeException(spec, s"Must be a number"))
        case ex: LogoException      => Failure(new ConstraintRuntimeException(spec, ex.getMessage))
      }
  }
}
