package org.nlogo.webstart.logging

import java.net.URL
import actors.{TIMEOUT, Actor}
import message.LogManagementMessage._
import message.{LoggingServerMessage, DirectorMessage}

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
          transmitFormatted(LogBufferManager !? Read)

        case ToDirectorWrite(x) =>
          LogBufferManager ! Write(x)

        case ToDirectorAbandon =>
          actorConditionTuple foreach (_._1 ! Abandon)
          abandonLog()
          replyClosing()
          exit("Abandon ye logs, mateys!")

        case ToDirectorFinalize =>
          LogFlushReminder ! Finalize
          transmitFormatted(LogBufferManager !? Finalize)
          finalizeLog()
          replyClosing()
          exit("That's a cut!")
      }
    }
  }

  private def replyClosing() { reply(DirectorMessage.FromDirectorClosed) }

  private def abandonLog()  { transmit(LoggingServerMessage.ToServerAbandon.toString) }
  private def finalizeLog() { transmit(LoggingServerMessage.ToServerFinalize.toString) }

  private def transmitFormatted(message: Any) {
    import LoggingServerMessage._
    val msgOpt = Option(message.asInstanceOf[String]) flatMap { case x => if (!x.isEmpty) Some(x) else None }
    transmit(msgOpt map (ToServerWrite(_).toString) getOrElse (ToServerPulse.toString))
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

    private val dataBuffer = new collection.mutable.ListBuffer[String]()

    def act() {
      loop {
        react {
          case Write(data) => write(data)
          case Read        => replyAndClear()
          case Finalize    => replyAndClear(); exit("Mission complete")
          case Abandon     => exit("Mission aborted")
        }
      }
    }

    private def write(data: String) {
      dataBuffer += data
    }

    private def replyAndClear() {
      reply(dataBuffer.mkString("\n"))
      dataBuffer.clear()
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
