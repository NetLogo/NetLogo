package org.nlogo.log.webstart

import java.net.URL
import actors.{TIMEOUT, Actor}
import message.{DirectorMessage, LogManagementMessage, LoggingServerMessage}
import LogManagementMessage.{Write, Abandon, Flush, Read, Finalize}
import LoggingServerMessage.{ToServerWrite, ToServerPulse, ToServerAbandon, ToServerFinalize}
import collection.mutable.ListBuffer

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
          transmitFormatted((LogBufferManager !? Read).asInstanceOf[Seq[String]])

        case ToDirectorWrite(x) =>
          LogBufferManager ! Write(x)

        case ToDirectorAbandon =>
          actorConditionTuple foreach (_._1 ! Abandon)
          abandonLog()
          replyClosing()
          exit("Abandon ye logs, mateys!")

        case ToDirectorFinalize =>
          actorConditionTuple map (_._1) filterNot (_ == LogBufferManager) foreach (_ ! Finalize)
          transmitFormatted((LogBufferManager !? Finalize).asInstanceOf[Seq[String]])
          finalizeLog()
          replyClosing()
          exit("That's a cut!")
      }
    }
  }

  private def replyClosing() { reply(DirectorMessage.FromDirectorClosed) }

  private def abandonLog()  { transmit(ToServerAbandon.toString) }
  private def finalizeLog() { transmit(ToServerFinalize.toString) }

  private def transmitFormatted(messages: Seq[String]) {
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
    private val MessageCharLimit = 12000

    private val dataBuffer = new ListBuffer[String]()

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

    private def flushBuffer() : Seq[String] = {

      // Sequentially accumulates the passed-in strings into strings that are as large as possible
      // while still adhering to the restriction that `accumulatedStr.size < MessageCharLimit`
      // Note: Before anyone gets clever and tries to convert this into functional-style code... please don't!
      //       When functional, this code suffered from performance issues (in both speed and memory).
      //       I understand that I could have improved the code to be better in both regards, but I think it's
      //       preferable to be as fast as possible here--which means using an imperative style  --JAB (4/30/12)
      def condenseToLimitedStrs(inContents: Array[String]): Seq[String] = {

        val bigBuffer = new ListBuffer[String]()
        val littleBuffer = new ListBuffer[String]()

        var i = 0

        while (i < inContents.size) {

          var size = 0
          var curr = inContents(i)

          // Potential massive breakage of code if `curr` is, itself, more than `MessageCharLimit` in size
          while ((size + curr.size) <= MessageCharLimit && (i < inContents.size)) {
            littleBuffer += curr
            size += curr.size
            i += 1
            if (i < inContents.size) curr = inContents(i)
          }

          bigBuffer += littleBuffer.mkString
          littleBuffer.clear()

        }

        bigBuffer.toSeq

      }

      val bufferContents = dataBuffer.toArray
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
