package arduino

import java.util.{ Deque, Map => JMap }

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortException

class PortListener(port: SerialPort, values: JMap[String, Object], inboundErrors: Deque[ErrorRecord])
  extends SerialPortEventListener {

  private val MaxErrorSize = 10
  private var residue: String = ""

  override def serialEvent(event: SerialPortEvent): Unit = {
    if (event.isRXCHAR) {
      try {
        val readValue = port.readString()
        if (readValue != null) {
          residue += readValue
          val (newResidue, results) = MessageParser.parseStream(residue)
          residue = newResidue
          results.foreach {
            case Right(ValuePair(key, value)) => values.put(key, value)
            case Left(e: ErrorRecord) =>
              for (i <- 0 until inboundErrors.size - MaxErrorSize) {
                inboundErrors.removeLast()
              }
              inboundErrors.addFirst(e)
          }
        }
      } catch {
        case s: SerialPortException => s.printStackTrace()
      }
    }
  }
}
