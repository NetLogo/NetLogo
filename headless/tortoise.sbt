libraryDependencies ++= Seq(
  "org.skyscreamer" % "jsonassert" % "1.1.0" % "test",
  "org.json4s" %% "json4s-native" % "3.1.0"
)

seq(coffeeSettings: _*)

(CoffeeKeys.bare in (Compile, CoffeeKeys.coffee)) := true

seq(Tortoise.settings: _*)
