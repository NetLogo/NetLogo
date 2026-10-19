package org.nlogo.extensions.time

sealed trait AddType
object AddType
case object Default extends AddType
case object Shuffle extends AddType
case object Repeat  extends AddType
case object RepeatShuffled extends AddType

sealed trait DateType
case object DateTime extends DateType
case object Date extends DateType
case object DayDate extends DateType

sealed trait PeriodType
case object Milli extends PeriodType
case object Second extends PeriodType
case object Minute extends PeriodType
case object Hour extends PeriodType
case object Day extends PeriodType
case object DayOfYear extends PeriodType
case object DayOfWeek extends PeriodType
case object Week extends PeriodType
case object Month extends PeriodType
case object Year extends PeriodType

sealed trait DataType
case object BooleanData extends DataType
case object IntegerData extends DataType
case object DoubleData extends DataType
case object StringData extends DataType

sealed trait GetTSMethod
case object Exact extends GetTSMethod
case object Nearest extends GetTSMethod
case object LinearInterp extends GetTSMethod
