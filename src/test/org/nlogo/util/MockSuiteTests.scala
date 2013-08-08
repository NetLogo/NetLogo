// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

class MockSuiteTests extends MockSuite{
  trait X{
    def i(i:Int)
    def d(d:Double)
    def c(c:Char)
    def a(a:Any)
  }
  mockTest("test primitive arguments"){
    val x = mock[X]
    expecting{
      one(x).i(arg(a[Int]))
      one(x).i(10)

      one(x).d(arg(a[Double]))
      one(x).d(10)
      one(x).d(10.0)

      one(x).c(arg(a[Char]))
      one(x).c('f')

      one(x).a(arg(a[Int]))
      one(x).a(arg(a[String]))
      one(x).a(arg(a[Char]))
    }
    when{
      x.i(7)
      x.i(10)

      x.d(7)
      x.d(10)
      x.d(10)

      x.c('x')
      x.c('f')

      x.a(8)
      x.a("hi")
      x.a('f')
    }
  }
  mockTest("test primitive arguments (error cases)"){
    val x = mock[X]
    expecting{
      one(x).a(arg(a[Int]))
    }
    val e = intercept[AssertionError] {
      when {
        x.a("not an int")
      }
    }
    val errorMessage = """|java.lang.AssertionError: unexpected invocation: x.a("not an int")
                          |expectations:
                          |  expected once, never invoked: x.a(<int>); returns a default value
                          |what happened before this: nothing!""".stripMargin
    assert(e.toString === errorMessage)
  }

  mockTest("test primitive arguments (more error cases)"){
    val x = mock[X]
    expecting{
      one(x).a(arg(a[Char]))
    }
    val e = intercept[AssertionError] {
      when {
        x.a("not an char")
      }
    }
    val errorMessage = """|java.lang.AssertionError: unexpected invocation: x.a("not an char")
                          |expectations:
                          |  expected once, never invoked: x.a(<char>); returns a default value
                          |what happened before this: nothing!""".stripMargin
    assert(e.toString === errorMessage)
  }

  mockTest("order doesn't matter") {
    val x = mock[X]
    expecting {
      one(x).i(10)
      one(x).i(20)
    }
    when {
      x.i(20)
      x.i(10)
    }
  }

  mockTest("extra calls do matter") {
    val x = mock[X]
    expecting {
      one(x).i(10)
    }
    val e = intercept[AssertionError] {
      when {
        x.i(10)
        x.i(20)
      }
    }
    val expected =
      """|unexpected invocation: x.i(<20>)
         |expectations:
         |  expected once, already invoked 1 time: x.i(<10>); returns a default value
         |what happened before this:
         |  x.i(<10>)""".stripMargin
    assertResult(expected)(e.getMessage.trim)
  }

  mockTest("andThen accepts correct ordering") {
    val x = mock[X]
    expecting {
      one(x).i(10) andThen one(x).i(20)
    }
    when {
      x.i(10)
      x.i(20)
    }
  }

  mockTest("andThen rejects incorrect ordering") {
    val x = mock[X]
    expecting {
      one(x).i(10) andThen one(x).i(20)
    }
    val e = intercept[AssertionError] {
      when {
        x.i(20)
        x.i(10)
      }
    }
    val expected =
      """|unexpected invocation: x.i(<20>)
         |expectations:
         |  expected once, never invoked: x.i(<10>); in sequence s; returns a default value
         |  expected once, never invoked: x.i(<20>); in sequence s; returns a default value
         |what happened before this: nothing!""".stripMargin
    assertResult(expected)(e.getMessage.trim)
  }

}
