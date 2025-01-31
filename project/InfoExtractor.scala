import java.io.StringReader
import javax.xml.stream.{ XMLInputFactory, XMLStreamConstants }

object InfoExtractor {
  def apply(source: String): String = {
    val sourceReader = new StringReader(source)
    val reader = {
      val factory = XMLInputFactory.newFactory

      factory.setProperty("javax.xml.stream.isCoalescing", true)

      factory.createXMLStreamReader(sourceReader)
    }

    while (reader.hasNext) {
      if (reader.next == XMLStreamConstants.START_ELEMENT && reader.getLocalName == "info") {
        val text = reader.getElementText

        reader.close()
        sourceReader.close()

        return text
      }
    }

    reader.close()
    sourceReader.close()

    ""
  }
}
