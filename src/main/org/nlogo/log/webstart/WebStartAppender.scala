package org.nlogo.log.webstart

import org.apache.log4j.Appender

trait WebStartAppender extends Appender {
  /*none*/ def deleteLog()
  /*none*/ def initialize()
}
