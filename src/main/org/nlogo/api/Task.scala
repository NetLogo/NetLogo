package org.nlogo.api

sealed trait Task
trait ReporterTask extends Task
trait CommandTask extends Task
