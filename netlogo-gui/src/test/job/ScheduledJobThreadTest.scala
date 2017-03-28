// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import org.scalatest.FunSuite

class ScheduledJobThreadTest {
  test("job ordering puts update variables ahead of stopping a job")
  test("job ordering puts stopping a job ahead of adding a job")

  test("if a job stop is processed before the job is added, the job is never added")
  test("adding a job schedules the job to be run")
}
