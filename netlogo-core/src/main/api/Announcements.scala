// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.time.LocalDate

sealed trait AnnouncementType
case object Release  extends AnnouncementType
case object Advisory extends AnnouncementType
case object Event    extends AnnouncementType

case class Announcement(id: Int, title: String, date: LocalDate, endDate: Option[LocalDate], annType: AnnouncementType, summary: String, desc: String)
