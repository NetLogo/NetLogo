package org.nlogo.log.webstart

import java.net.URL
import actors.{TIMEOUT, Actor}
import message.{DirectorMessage, LogManagementMessage, LoggingServerMessage}
import LogManagementMessage.{Write, Abandon, Flush, Read, Finalize}
import LoggingServerMessage.{ToServerWrite, ToServerPulse, ToServerAbandon, ToServerFinalize}

// An actor controller; receives logging data and figures out what to do with it
class LogDirector(val mode: LogSendingMode, destinations: URL*) extends Actor {

  require(destinations.size > 0)

  // Actors paired with their start conditions
  // This way, if an actor is added into this class, it is unlikely that the programmer will forget to start it
  private val actorConditionTuple = List((LogBufferManager, true), (LogFlushReminder, mode == LogSendingMode.Continuous))
  actorConditionTuple foreach (x => if (x._2) x._1.start())

  def act() {
    import DirectorMessage._
    loop {
      react {
        case Flush =>
          transmitFormatted((LogBufferManager !? Read).asInstanceOf[List[String]])

        case ToDirectorWrite(x) =>
          LogBufferManager ! Write(x)

        case ToDirectorAbandon =>
          actorConditionTuple foreach (_._1 ! Abandon)
          abandonLog()
          replyClosing()
          exit("Abandon ye logs, mateys!")

        case ToDirectorFinalize =>
          actorConditionTuple map (_._1) filterNot (_ == LogBufferManager) foreach (_ ! Finalize)
          transmitFormatted((LogBufferManager !? Finalize).asInstanceOf[List[String]])
          finalizeLog()
          replyClosing()
          exit("That's a cut!")
      }
    }
  }

  private def replyClosing() { reply(DirectorMessage.FromDirectorClosed) }

  private def abandonLog()  { transmit(ToServerAbandon.toString) }
  private def finalizeLog() { transmit(ToServerFinalize.toString) }

  private def transmitFormatted(messages: List[String]) {
    val msgListOpt = if (messages filterNot (_.isEmpty) isEmpty) None else Some(messages)
    msgListOpt map (_ map (ToServerWrite(_).toString)) getOrElse (List(ToServerPulse.toString)) foreach transmit
  }

  private def transmit(message: String) {
    destinations foreach (LoggingServerHttpHandler.sendMessage(message, _))
  }


  /**************************************************
   *               End of outer class               *
   **************************************************/


  /*
  A concurrent wrapper around a queue-like string buffer
  Aside from telling it to close, there are two things that you can do with it:

    1) Tell it to write some data to the buffer
    2) Tell it to give you its contents and clear itself

  This allows a threadsafe way for the buffer to be regularly filled while being periodically read/emptied
   */
  private object LogBufferManager extends Actor {

    // Only change this constant if you have a veeeeery good reason; it has been specially tuned to this value on purpose --JAB (4/26/12)
    private val MessageCharLimit = 30000

    private val dataBuffer = new collection.mutable.ListBuffer[String]()

    def act() {
      loop {
        react {
          case Write(data) => write(data)
          case Read        => reply(flushBuffer())
          case Finalize    => reply(flushBuffer()); exit("Mission complete")
          case Abandon     => exit("Mission aborted")
        }
      }
    }

    private def write(data: String) {
      dataBuffer += data
    }

    private def flushBuffer() : List[String] = {
      // Sequentially accumulates the passed-in strings into strings that are as large as possible
      // while still adhering to the restriction that `accumulatedStr.size < MessageCharLimit`
      // Note: Could be made more efficient by retaining length info across calls
      // Note: Potential infinite recursion bug when passing in any string that has a size of > `MessageCharLimit`
      def condenseToLimitedStrs(inContents: List[String]) : List[String] = {
        def condensationHelper(remainingContents: List[String]) : List[String] = {
          remainingContents match {
            case Nil => Nil
            case _   =>
              val totalChars = remainingContents map (_.size) sum
              val accumSizeToElemList = remainingContents.foldLeft(List[(Int, String)]()){
                case (Nil, x) => (totalChars, x) :: Nil
                case (acc, x) => (acc.head._1 - acc.head._2.size, x) :: acc
              }.reverse
              val (canGoSizeStrPairs, mustWaitSizeStrPairs) = accumSizeToElemList partition (_._1 < MessageCharLimit)
              canGoSizeStrPairs.map(_._2).mkString :: condensationHelper(mustWaitSizeStrPairs map (_._2))
          }
        }
        condensationHelper(inContents).reverse
      }
      val bufferContents = dataBuffer.toList
      dataBuffer.clear()
      condenseToLimitedStrs(bufferContents)
    }
  }

  // Essentially, a timer that reminds the Director to request buffer flushes from the LogBufferManager
  private object LogFlushReminder extends Actor {
    val FlushIntervalMs = 3000
    def act() {
      loop {
        reactWithin(FlushIntervalMs) {
          case TIMEOUT  => LogDirector.this ! Flush
          case Finalize => exit("Flushing complete")
          case Abandon  => exit("Flushing aborted")
        }
      }
    }
  }

}
