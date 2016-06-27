// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ CompilerServices, LogoException, LogoThunkFactory, ReporterLogoThunk, ValueConstraint }
import org.nlogo.core.CompilerException

import scala.util.{ Try, Failure }

object SliderConstraint {

  case class Spec(fieldName: String, displayName:String)
  val Min = Spec("minimumCode", "Minimum")
  val Max = Spec("maximumCode", "Maximum")
  val Inc = Spec("incrementCode", "Increment")

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
                           ownerName: String, thunkFactory: LogoThunkFactory): SliderConstraint = {
    val compiler: CompilerServices = agent.world.compiler
    abstract class ConstraintCompiler[T] {
      def compile(code: String, spec: Spec): Either[SliderConstraintException, T]
      def makeConstraint(minT: T, maxT: T, incT: T): SliderConstraint
      def compileAll(minCode: String, maxCode: String, incCode: String): SliderConstraint = {
        val (minT, maxT, incT) = (compile(minCode, Min), compile(maxCode, Max), compile(incCode, Inc))
        val allErrors = List(minT, maxT, incT).filter(_.isLeft).map(_.left.get)
        if (!allErrors.isEmpty) throw new ConstraintExceptionHolder(allErrors)
        // no compilation errors, so return a new constraint.
        makeConstraint(minT.right.get, maxT.right.get, incT.right.get)
      }
    }
    object ConstantConstraintCompiler extends ConstraintCompiler[Double] {
      def compile(code: String, spec: Spec) = {
        val o: Object = compiler.readFromString(code)
        if (!(o.isInstanceOf[Double])) Left(new ConstraintRuntimeException(spec, "constraint must be a number."))
        else Right(o.asInstanceOf[Double])
      }
      def makeConstraint(minT: Double, maxT: Double, incT: Double) = new ConstantSliderConstraint(minT, maxT, incT)
    }
    object DynamicConstraintCompiler extends ConstraintCompiler[ReporterLogoThunk] {
      def compile(code: String, spec: Spec) = {
        try Right(thunkFactory.makeReporterThunk(code, "slider '" + ownerName + "' " + spec.displayName))
        catch {case ex: CompilerException => Left(new ConstraintCompilerException(spec, ex))}
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
  def minimum: Double
  def increment: Double
  def maximum: Double
  var defaultValue = World.ZERO
  def assertConstraint(o: Object): Unit = {
    if (!(o.isInstanceOf[Double])) {throw new ValueConstraint.Violation("Value must be a number.")}
  }
  def coerceValue(o: Object): Object = if (o.isInstanceOf[Double]) o else defaultValue
}

case class ConstantSliderConstraint(minimum: Double, maximum: Double, increment: Double) extends SliderConstraint

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
  private def get(spec:Spec, thunk:ReporterLogoThunk): Double = {
    thunk.call
      .flatMap(res => Try(res.asInstanceOf[Double]))
      .recoverWith {
        case ex: ClassCastException => Failure(new ConstraintRuntimeException(spec, s"Must be a number"))
        case ex: LogoException      => Failure(new ConstraintRuntimeException(spec, ex.getMessage))
      }.get
  }
}
