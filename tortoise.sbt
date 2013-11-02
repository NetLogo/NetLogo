libraryDependencies ++= Seq(
  "org.skyscreamer" % "jsonassert" % "1.1.0" % "test",
  "org.json4s" %% "json4s-native" % "3.1.0"
)

seq(Coffee.settings: _*)

seq(Tortoise.settings: _*)
