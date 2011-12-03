package org.nlogo.hotlink.controller;

import org.apache.log4j.spi.LoggingEvent;
import org.nlogo.log.LogMessage;

public class TickListeningAppender extends org.apache.log4j.AppenderSkeleton {

    TickListener tickListener;

    TickListeningAppender( TickListener tickListener ) {
        this.tickListener = tickListener;
    }

    @Override
    protected void append(LoggingEvent arg0) {
        org.nlogo.log.LogMessage message = (LogMessage) arg0.getMessage();
        if( message.elements()[0].data().equals("ticks") ) {
          tickListener.tick( Double.valueOf( message.elements()[1].data() ) );
        } else {
           // System.out.println( message.elements[0].data + ": " +
            //        message.elements[1].data );
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {
    }

}
