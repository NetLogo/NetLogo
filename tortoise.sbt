libraryDependencies ++= Seq(
  "org.skyscreamer" % "jsonassert" % "1.1.0" % "test",
  "org.json4s" %% "json4s-native" % "3.1.0"
)

seq(coffeeSettings: _*)

(CoffeeKeys.bare in (Compile, CoffeeKeys.coffee)) := true

resourceGenerators in Compile <+= Def.task {
  val path = resourceManaged.value / "json2.js"
  if (!path.exists) {
    streams.value.log.info("downloading json2.js")
    IO.download(
      new java.net.URL("http://ccl.northwestern.edu/devel/json2-43d7836c.js"),
      path)
  }
  Seq(path)
}
