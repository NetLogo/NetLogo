package org.nlogo.webstart.logging

import org.apache.log4j.Appender

trait WebStartAppender extends Appender {
  /*none*/ def deleteLog()
  /*none*/ def initialize()
}
