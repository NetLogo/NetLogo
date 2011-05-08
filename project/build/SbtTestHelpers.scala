import sbt._

trait SbtTestHelpers { this: DefaultProject =>
  
  def singleTestTask(className: String) = task { args =>
    defaultTestTask(TestFilter(_ == className) :: testOptions.toList ::: ScalaTestArgs(args))
  }

  def runSubclassesOf(className: String) = {
    val subclass: Boolean => Boolean = x => x
    subclassTest(className, subclass)
  }

  def runEverythingButSubclassesOf(className: String) = {
    val notSubclass: Boolean => Boolean = x => ! x
    subclassTest(className, notSubclass)
  }

  private def subclassTest(className: String, subclassCheck: Boolean => Boolean) = task { args =>
    lazy val jars = testClasspath.get.toList.map(_.asURL).toArray[java.net.URL]
    lazy val loader = new java.net.URLClassLoader(jars,buildScalaInstance.loader)
    def clazz(name: String) = Class.forName(name, false, loader)
    lazy val superClass = clazz(className)
    def filter = TestFilter(c => subclassCheck(superClass.isAssignableFrom(clazz(c))))
    defaultTestTask(filter :: testOptions.toList ::: ScalaTestArgs(args))
  }

  def newScalaTestArg(l: String*) = TestArgument(TestFrameworks.ScalaTest, l:_*)

  private def ScalaTestArgs(args: Seq[String]): List[TestArgument] = {
    def KVArgs(args: Seq[String]): TestArgument = newScalaTestArg(args.map("-D" + _):_*)
    def tagsFromArgs(tags: Seq[String]): List[TestArgument] = {
      if (tags.isEmpty) Nil else List(newScalaTestArg("-n", tags.mkString(" ")))
    }
    val (kvs, tags) = args.partition(_.contains("="))
    KVArgs(kvs.toSeq) :: tagsFromArgs(tags.toSeq)
  }
}
