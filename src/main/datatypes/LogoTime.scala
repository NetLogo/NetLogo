package org.nlogo.extensions.time.datatypes

import java.time.{Duration, LocalDate, LocalDateTime, MonthDay, Period, ZoneOffset}
import java.time.temporal.ChronoUnit._
import java.time.temporal.ChronoField.MILLI_OF_SECOND
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle.STRICT
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.WeekFields
import org.nlogo.agent.World
import org.nlogo.api.ExtensionException
import org.nlogo.core.ExtensionObject
import org.nlogo.extensions.time._

object LogoTimeComparator extends Ordering[LogoTime] {
  def compare(a: LogoTime, b: LogoTime): Int = a.compareTo(b)
}

class LogoTime extends ExtensionObject {
  var dateType: DateType = DateTime
  var datetime: LocalDateTime = null
  var date: LocalDate = null
  var monthDay: MonthDay = null
  private var customFmt: DateTimeFormatter = null
  private var defaultFmt: DateTimeFormatter =
    (new DateTimeFormatterBuilder()
      .parseStrict().appendPattern("uuuu-MM-dd HH:mm:ss.")
      .parseLenient().appendPattern("SSS")).toFormatter
  private var isAnchored: java.lang.Boolean = false
  private var tickValue: java.lang.Double = 0.0
  private var tickType: PeriodType = null
  private var anchorDatetime: LocalDateTime = null
  private var anchorDate: LocalDate = null
  private var anchorMonthDay: MonthDay = null
  private var world: World = null

  @throws[ExtensionException]
  def this(dt: LocalDateTime) = {
    this()
    this.datetime = dt
  }
  /** The LogoTime constructors below are used thoughout the
     TimePrimitive class and LogoTime class to allow conversions
     and re-initialization with different formats and times.
     This has been a source of confusion since there are
     many transitions between formatting and strings that
     have caused a lot of runtime errors
     [ CBR 02/14/19 ]
   **/

  @throws[ExtensionException]
  def this(dateStringArg: String, customFormat: Option[String]) = {
    this()
    var dateString: String = dateStringArg.replace('T',' ').replace('Y','y')
    // First we parse the string to determine the date type
    customFormat match {
      case None =>
           dateString = parseDateString(dateString).replace('T',' ')
      case Some(customForm) =>
        this.dateType =
          if (customForm.indexOf('H') >= 0 || customForm.indexOf('h') >= 0 ||
              customForm.indexOf('S') >= 0 || customForm.indexOf('s') >= 0 ||
              customForm.indexOf('K') >= 0 || customForm.indexOf('k') >= 0 ||
              customForm.indexOf('m') >= 0)
            DateTime
          else if (customForm.indexOf('Y') >= 0 || customForm.indexOf('y') >= 0)
            Date
          else
            DayDate
    }
    this.defaultFmt = this.dateType match {
      case DateTime =>
        (new DateTimeFormatterBuilder()
          .parseStrict().appendPattern("uuuu-MM-dd HH:mm:ss.").parseLenient().appendPattern("SSS")).toFormatter.withResolverStyle(STRICT)
      case Date => DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(STRICT)
      case DayDate => DateTimeFormatter.ofPattern("MM-dd").withResolverStyle(STRICT)
    }
    try
      customFormat match {
        case None =>
          dateString = dateString.replace(' ','T').replace('/','-')
          this.dateType match {
            case DateTime =>
              this.datetime =
                if (dateString.length == 0 || dateString.==("now"))
                  LocalDateTime.now
                else
                 LocalDateTime.parse(dateString)
            case Date    => this.date = LocalDate.parse(dateString)
            case DayDate => this.monthDay = MonthDay.parse(dateString, this.defaultFmt)
          }
        case Some(customForm) =>
          this.customFmt =
            if(customForm.endsWith("S") && customForm.count(_ == 'S') < 4)
              (new DateTimeFormatterBuilder()
                .parseStrict().appendPattern(customForm.filterNot(_ == 'S').replace('Y','y').replace('y','u'))
                .parseStrict().appendFraction(MILLI_OF_SECOND, 1, 3, false)
              ).toFormatter.withResolverStyle(STRICT)
            else
              (new DateTimeFormatterBuilder()
                .parseStrict().appendPattern(customForm.replace('Y','y').replace('y','u'))).toFormatter.withResolverStyle(STRICT)
          this.dateType match {
            case DateTime => this.datetime = LocalDateTime.parse(dateString, this.customFmt)
            case Date => this.date = LocalDate.parse(dateString, this.customFmt)
            case DayDate => this.monthDay = MonthDay.parse(dateString, this.customFmt)
          }
      }
      catch {
        case e: DateTimeParseException =>
          throw new ExtensionException(
            s"Extension could not parse input: $dateString and $customFormat")
      }
  }

  def this(dateString: String) = this(dateString, None)

  def this(dateString: String, dateFormat: DateTimeFormatter, datetype: DateType) = {
    this()
    this.dateType = datetype
    this.customFmt = dateFormat
    this.dateType match {
      case DateTime => this.datetime = LocalDateTime.parse(dateString, dateFormat)
      case Date => this.date = LocalDate.parse(dateString, dateFormat)
      case DayDate => this.monthDay = MonthDay.parse(dateString, dateFormat)
    }
  }

  def this(time: LogoTime) =
    this(time.show(if(time.customFmt == null) time.defaultFmt else time.customFmt), if(time.customFmt == null) time.defaultFmt else time.customFmt, time.dateType)

  def this(dt: LocalDate) = {
    this()
    this.date = dt
    this.defaultFmt = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(STRICT)
    this.dateType = Date
  }

  def this(dt: MonthDay) = {
    this()
    this.monthDay = dt
    this.defaultFmt = DateTimeFormatter.ofPattern("MM-dd").withResolverStyle(STRICT)
    this.dateType = DayDate
  }

  def compareTo(that: LogoTime): Int = {
    this.dateType match {
      case DateTime => this.datetime.compareTo(that.datetime)
      case Date => this.date.compareTo(that.date)
      case DayDate => this.monthDay.compareTo(that.monthDay)
    }
  }

  def isCloserToAThanB(timeA: LogoTime, timeB: LogoTime): java.lang.Boolean = {
    val refDateTime: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
    var millisToA: java.lang.Long = null
    var millisToB: java.lang.Long = null
    this.dateType match {
      case DateTime =>
        millisToA = Math.abs(
          (Duration.between(timeA.datetime.`with`(refDateTime),
                        this.datetime.`with`(refDateTime))).toMillis)
        millisToB = Math.abs(
          (Duration.between(timeB.datetime.`with`(refDateTime),
            this.datetime.`with`(refDateTime))).toMillis)
      case Date =>
        millisToA = Math.abs(
          (Duration.between(timeA.date.`with`(refDateTime),
                        this.date.`with`(refDateTime))).toMillis)
        millisToB = Math.abs(
          (Duration.between(timeB.date.`with`(refDateTime),
                        this.date.`with`(refDateTime))).toMillis)
      case DayDate =>
        millisToA = Math.abs((Duration.between(
          timeA.monthDay.atYear(2000).`with`(refDateTime),
          this.monthDay.atYear(2000).`with`(refDateTime))).toMillis)
        millisToB = Math.abs((Duration.between(
          timeB.monthDay.atYear(2000).`with`(refDateTime),
          this.monthDay.atYear(2000).`with`(refDateTime))).toMillis)
    }
    millisToA < millisToB
  }

  @throws[ExtensionException]
  def parseDateString(dateStringT: String): String = {
    val delimiter = if(dateStringT.contains('-')) '-' else '/'
    val dateString: String = dateStringT.replace('/', '-').replace(' ', 'T')
    val timefragments = dateString.split("T").map(_.trim)
    (dateString match {
      case "" => ""
      case "now" => "now"
      case str if timefragments.length == 2 && dateString.contains('-') =>
        this.dateType = DateTime
        parseDate(timefragments(0)) + "T" + parseTime(timefragments(1))
      case str if timefragments.length == 1 && !str.contains(':') &&  str.contains('-') =>
        if(str.length < 6) this.dateType = DayDate else this.dateType = Date
        parseDate(timefragments(0))
      case str => throw new ExtensionException(s"Invalid string fragment: $str")
    }).replace('-',delimiter)
  }
  /* Format to the desired length for datetime, date, and daydate formats */
  def formatToLength(dateString: String, desiredLength: Int): String =
    if(dateString.length >= desiredLength) dateString
    else "0" * (desiredLength - dateString.length) + dateString

  /* parseDate assumes the traditional 3 date format (year, month, day | though not in that order)
     [CBR 2019/31/01] */
  @throws[ExtensionException]
  def parseDate(dateTimeStr:String): String =
    dateTimeStr.split('-') match {
      case Array("") => ""
      case Array(date1,date2,date3)
        if dateTimeStr.length < 11 || date1.length != 3 || date2.length != 3 || date1.length != 3 =>
        formatToLength(date1,2) + "-" + formatToLength(date2,2) + "-" + formatToLength(date3,2)
      case Array(date1,date2) if dateTimeStr.length < 6 =>
        formatToLength(date1,2) + "-" + formatToLength(date2,2)
      case _ => throw new ExtensionException(s"Illegal Date String(DateTime Error)")
    }

  @throws[ExtensionException]
  def parseTime(dateTimeStr:String): String = {
    def formatSeconds = (secondAndMillis:String) =>
      secondAndMillis.split('.') match {
        case Array(sec,milli) if sec.length <= 2 && milli.length <= 3 =>
          formatToLength(sec,2) + "." + (milli + "0" * (3 - milli.length))
          /* In this case, millisecond adds zeros at the end to match decimal formatting */
        case Array(sec,milli) if sec.length <= 2 && milli.length > 3 =>
          formatToLength(sec,2) + "." + milli.take(3)
        case Array(sec) if sec.length <= 2 =>
          formatToLength(sec,2) + ".000"
        case _ => throw new ExtensionException(s"Illegal Second String")
      }
    dateTimeStr.split(':') match {
      case Array(hour,minute,second) if (hour + ":" + minute + ":").length < 7 =>
        formatToLength(hour,2) + ":" + formatToLength(minute,2) + ":" + formatSeconds(second)
      case Array(hour,minute) if dateTimeStr.length < 6 =>
        formatToLength(hour,2) + ":" + formatToLength(minute,2) + ":00.000"
      case Array(hour) if dateTimeStr.length < 3 =>
        formatToLength(hour,2) + ":00" + ":00.000"
      case tstr => throw new ExtensionException(s"Invalid Time String: $tstr")
    }
  }

  def setAnchor(tickCount: java.lang.Double, tickType: PeriodType, world: World): Unit = {
    if (tickType == DayOfWeek)
      throw new ExtensionException(tickType.toString + " type is not a supported tick type")
    this.isAnchored = true
    this.tickValue = tickCount
    this.tickType = tickType
    this.dateType match {
      case DateTime => this.anchorDatetime = LocalDateTime.from(this.datetime)
    case Date => this.anchorDate = LocalDate.from(this.date)
      case DayDate => this.anchorMonthDay = MonthDay.from(this.monthDay)
    }
    this.world = world
  }

  def dump(arg1: Boolean, arg2: Boolean, arg3: Boolean): String = this.toString

  override def toString(): String = {
    try this.updateFromTick()
    catch {
      case e: ExtensionException =>
    }
    this.dateType match {
      case DateTime =>
        val fmt = if(this.customFmt == null) this.defaultFmt else this.customFmt
        datetime.format(fmt)
      case Date =>
        val fmt = if(this.customFmt == null) this.defaultFmt else this.customFmt
        date.format(fmt)
      case DayDate =>
        val fmt = if(this.customFmt == null) this.defaultFmt else this.customFmt
        monthDay.format(fmt)
    }
  }

  def updateFromTick(): Unit = {
    if(!this.isAnchored) return
    this.dateType match {
      case DateTime =>
        this.datetime = this
          .plus(this.anchorDatetime,
                this.tickType,
                this.world.ticks * this.tickValue)
          .datetime
      case Date =>
        this.date = this
          .plus(this.anchorDate,
                this.tickType,
                this.world.ticks * this.tickValue)
          .date
      case DayDate =>
        this.monthDay = this
          .plus(this.anchorMonthDay,
                this.tickType,
                this.world.ticks * this.tickValue)
          .monthDay
    }
  }
/*  time:anchor-schedule time:create "2000-01-01" 0.5 "day"
  set current-time time:anchor-to-ticks time:create "2000-01-01" 0.5 "day"
 */
  def getExtensionName: String = "time"
  def getNLTypeName: String = "logotime"
  def recursivelyEqual(arg0: AnyRef): Boolean = equals(arg0)

  def show(fmt: DateTimeFormatter): String =
    this.dateType match {
      case DateTime => this.datetime.format(fmt)
      case Date => this.date.atStartOfDay().format(fmt)
      case DayDate => this.monthDay.atYear(2000).atStartOfDay().format(fmt)
    }

  /* Get requires a couple assumptions for a successful conversion.
     There is an implicit conversion to DateTime, which assumes
     the beginning of the day on year 2000
  */
  def get(periodType: PeriodType): java.lang.Double =
    (periodType match {
     case Milli =>
        this.dateType match {
          case DateTime => (datetime.getNano) / 1000000
          case Date => date.atStartOfDay.getNano() / 1000000
          case DayDate => monthDay.atYear(2000).atStartOfDay.getNano() / 1000000
        }
      case Second =>
        this.dateType match {
          case DateTime => datetime.getSecond
          case Date => date.atStartOfDay.getSecond()
          case DayDate => monthDay.atYear(2000).atStartOfDay.getSecond()
        }
      case Minute =>
        this.dateType match {
          case DateTime => datetime.getMinute()
          case Date => date.atStartOfDay.getMinute()
          case DayDate => monthDay.atYear(2000).atStartOfDay.getMinute()
        }
      case Hour =>
        this.dateType match {
          case DateTime => datetime.getHour
          case Date => date.atStartOfDay.getHour()
          case DayDate => monthDay.atYear(2000).atStartOfDay.getHour()
        }
      case Day =>
        this.dateType match {
          case DateTime => datetime.getDayOfMonth()
          case Date => date.getDayOfMonth()
          case DayDate => monthDay.atYear(2000).getDayOfMonth()
        }
      case DayOfYear =>
        this.dateType match {
          case DateTime => datetime.getDayOfYear
          case Date => date.getDayOfYear()
          case DayDate => monthDay.atYear(2000).getDayOfYear()
        }
      case DayOfWeek =>
        this.dateType match {
          case DateTime => datetime.getDayOfWeek.getValue()
          case Date => date.getDayOfWeek().getValue()
          case DayDate => monthDay.atYear(2000).getDayOfWeek().getValue()
        }
      case Week =>
        this.dateType match {
          case DateTime =>
            datetime.get(WeekFields.SUNDAY_START.weekOfYear)
          case Date =>
            date.get(WeekFields.SUNDAY_START.weekOfYear)
          case DayDate =>
            monthDay.atYear(2000).get(WeekFields.SUNDAY_START.weekOfYear)
        }
      case Month =>
        this.dateType match {
          case DateTime => datetime.getMonthValue()
          case Date => date.getMonthValue()
          case DayDate => monthDay.atYear(2000).getMonthValue()
        }
      case Year =>
        this.dateType match {
          case DateTime => datetime.getYear()
          case Date => date.atStartOfDay.getYear()
          case DayDate => monthDay.atYear(2000).getYear()
        }
    }).toDouble

  /** time:plus has been the source of plenty of hidden runtime issues
     with time conversions and parsing errors. These two errors are
     often found here because of the number of constructors,
     implicit conversions, parsing, and formatting that LogoTime
     tries to automate. Also, since not all DateTime formats are
     valid sometimes new values don't make sense and error out
     [ CBR 02/14/19 ]
   **/

  def plus(pType: PeriodType, durVal: java.lang.Double): LogoTime =
    this.dateType match {
      case DateTime => this.plus(this.datetime, pType, durVal)
      case Date => this.plus(this.date, pType, durVal)
      case DayDate => this.plus(this.monthDay, pType, durVal)
    }

  def plus(refTime: AnyRef, pType: PeriodType, durValArg: java.lang.Double): LogoTime = {
    var per: Option[Period] = None
    var durVal: java.lang.Double = durValArg
    pType match { //conversions
      case Week => durVal *= 7 * 24 * 60 * 60 * 1000
      case Day | DayOfYear => durVal *= 24 * 60 * 60 * 1000
      case Hour => durVal *= 60 * 60 * 1000
      case Minute => durVal *= 60 * 1000
      case Second => durVal *= 1000
      case Milli =>
      case Month =>
        per = Some (Period.of(0, TimeUtils.roundDouble(durVal), 0))
      case Year =>
        per = Some (Period.of(TimeUtils.roundDouble(durVal), 0, 0))
      case _ =>
        throw new ExtensionException(
          s"$pType type is not supported by the time:plus primitive")
    }
    this.dateType match {
      case DateTime =>
        per match {
          case None =>
            new LogoTime(refTime
              .asInstanceOf[LocalDateTime]
              .plus(Duration.of(TimeUtils.dToL(durVal), MILLIS)))
          case Some(period) =>
          new LogoTime(refTime.asInstanceOf[LocalDateTime].plus(period))
        }
      case Date =>
        per match {
          case None => {
            val logotime = refTime
              .asInstanceOf[LocalDate].atStartOfDay
              .plus(Duration.of(TimeUtils.dToL(durVal), MILLIS)).toLocalDate
            new LogoTime(logotime)
          }
          case Some(period) =>
            new LogoTime(refTime.asInstanceOf[LocalDate].plus(period))
        }
      case DayDate =>
        per match {
          case None => {
            new LogoTime(MonthDay.from(refTime.asInstanceOf[MonthDay].atYear(2000).atStartOfDay
              .plus(Duration.of(TimeUtils.dToL(durVal), MILLIS))))
          }
          case Some(period) =>
            new LogoTime(MonthDay.from(refTime.asInstanceOf[MonthDay].atYear(2000).atStartOfDay.plus(period)))
        }
    }
  }

  def isBefore(timeB: LogoTime): Boolean = {
    if (this.dateType != timeB.dateType)
      throw new ExtensionException(
        "time comparisons only work if the LogoTime's are the same variety, but you called with a " +
          this.dateType.toString +
          " and a " +
          timeB.dateType.toString)
    this.dateType match {
      case DateTime => this.datetime.isBefore(timeB.datetime)
      case Date => this.date.isBefore(timeB.date)
      case DayDate => this.monthDay.isBefore(timeB.monthDay)
    }
  }

  def isEqual(timeB: LogoTime): Boolean = {
    if (this.dateType != timeB.dateType)
      throw new ExtensionException(
        "time comparisons only work if the LogoTime's are the same variety, but you called with a " +
          this.dateType.toString + " and a " + timeB.dateType.toString)
    this.dateType match {
      case DateTime => this.datetime.isEqual(timeB.datetime)
      case Date => this.date.isEqual(timeB.date)
      case DayDate => this.monthDay.equals(timeB.monthDay)
    }
  }

  def isBetween(timeAArg: LogoTime, timeBArg: LogoTime): Boolean = {
    var timeA: LogoTime = timeAArg
    var timeB: LogoTime = timeBArg
    if (!timeA.isBefore(timeB)) {
      val tempA: LogoTime = timeA
      timeA = timeB
      timeB = tempA
    }
    if (this.dateType != timeA.dateType || this.dateType != timeB.dateType)
      throw new ExtensionException(
        "time comparisons only work if the LogoTime's are the same variety, but you called with a " +
          this.dateType.toString +
          ", a " +
          timeA.dateType.toString +
          ", and a " +
          timeB.dateType.toString)

    this.dateType match {
      case DateTime =>
        ((this.datetime.isAfter(timeA.datetime) && this.datetime
          .isBefore(timeB.datetime)) ||
          this.datetime.isEqual(timeA.datetime) ||
          this.datetime.isEqual(timeB.datetime))
      case Date =>
        ((this.date.isAfter(timeA.date) && this.date.isBefore(timeB.date)) ||
          this.date.isEqual(timeA.date) ||
          this.date.isEqual(timeB.date))
      case DayDate =>
        if(timeA.monthDay.equals(timeAArg.monthDay)){
          ((this.monthDay.isAfter(timeA.monthDay) && this.monthDay
          .isBefore(timeB.monthDay)) ||
          this.monthDay.equals(timeA.monthDay) ||
          this.monthDay.equals(timeB.monthDay))
        } else {
          ((this.monthDay
          .isBefore(timeBArg.monthDay)) ||
          this.monthDay.equals(timeA.monthDay) ||
          this.monthDay.equals(timeB.monthDay))
        }
    }
  }

  /** getDifferencebetween is another primitive that requires implicit conversions.
     Since matching types are a required, there shouldn't be any issues,
     but with time:plus and other primitives with hidden conversions, sometimes type checking at
     runtime fails. [ CBR 02/14/19 ]
   **/
  def getDifferenceBetween(pType: PeriodType, endTime: LogoTime): java.lang.Double = {
    if (this.dateType != endTime.dateType){
      throw new ExtensionException(s"time comparisons only work if the LogoTimes are the same variety, but you called with a ${this.dateType.toString} and a ${endTime.dateType.toString}")
    }
    var durVal: java.lang.Double = 1.0
    pType match {
      case Year =>
        this.dateType match {
          case DateTime =>
            TimeUtils.intToDouble((Period.between(this.datetime.toLocalDate(), endTime.datetime.toLocalDate())).getYears)
          case Date =>
            TimeUtils.intToDouble((Period.between(this.date, endTime.date)).getYears)
          case DayDate =>
            throw new ExtensionException(s"$pType type is not supported by the time:difference-between primitive with LogoTimes of type DAY")
        }
      case Month =>
        this.dateType match {
          case DateTime =>
            TimeUtils.intToDouble((Period.between(this.datetime.toLocalDate(), endTime.datetime.toLocalDate())).getMonths)
          case Date =>
            TimeUtils.intToDouble(
              (Period.between(this.date, endTime.date)).getMonths)
          case DayDate =>
            TimeUtils.intToDouble(
              (Period.between(this.monthDay.atYear(2000), endTime.monthDay.atYear(2000))).getMonths)
        }
      case Week | Day | DayOfYear | Hour | Minute | Second | Milli => {
        pType match {
          case Week => durVal *= (7.0 * 24.0 * 60.0 * 60.0 * 1000)
          case Day | DayOfYear => durVal *= (24.0 * 60.0 * 60.0 * 1000)
          case Hour => durVal *= (60.0 * 60.0 * 1000)
          case Minute => durVal *= (60.0 * 1000)
          case Second => durVal *= 1000
          case Milli =>
          case _ => throw new ExtensionException(s"$pType testing type is not supported by the time:difference-between primitive")
        }
        this.dateType match {
          case DateTime =>
            (Duration.between(this.datetime, endTime.datetime)).toMillis /durVal
          case Date =>
            (Duration.between(this.date.atStartOfDay(), endTime.date.atStartOfDay())).toMillis / durVal
          case DayDate =>
            (Duration.between(this.monthDay.atYear(2000).atStartOfDay(), endTime.monthDay.atYear(2000).atStartOfDay())).toMillis /durVal
        }
      }
      case _ => throw new ExtensionException(s"$pType coding type is not supported by the time:difference-between primitive")
    }
  }
}
