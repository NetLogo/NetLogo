package org.nlogo.extensions.time.datatypes

class TimeSeriesColumn() {
  var data: List[String] = List[String]()

  def add(value: String): Unit = {
      this.data = this.data :+ value
  }
}
