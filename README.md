My current build process for ScalaJS is absolutely disgusting, and I plan to improve upon it and remove this file in due time.  Until then, here's how I do things...

 * Check out a copy of the ScalaJS source from the central repo on GitHub
 * Modify the `project/ScalaJSBuild.scala` file like seen in Appendix A
 * Create a directory inside your copy of the ScalaJS source repo, `./examples/engine`
 * Create a dummy `startup.js` file with Appendix B as its contents
 * From `./examples/engine`, run the shell script as seen in Appendix C
 * From `$tortoise`, run `./sbt` and then `test-only org.nlogo.tortoise.*` to run the Tortoise tests

Obviously, this methodology is ***full*** of imperfections.  I'm merely documenting it now so that I don't lose it, and, should I die within the next week, someone can easily pick up where I left off.

### Appendix A

```
@@ -58,7 +58,7 @@ object ScalaJSBuild extends Build {
           clean <<= clean.dependsOn(
               // compiler, library and sbt-plugin are aggregated
               clean in corejslib, clean in javalib, clean in scalalib,
-              clean in libraryAux, clean in examples,
+              clean in libraryAux, clean in examples, clean in exampleEngine,
               clean in exampleHelloWorld, clean in exampleReversi)
       )
   ).aggregate(
@@ -178,6 +178,14 @@ object ScalaJSBuild extends Build {
       )
   ).aggregate(exampleHelloWorld, exampleReversi)
 
+  lazy val engine: Project = Project(
+      id = "engine",
+      base = file("examples"),
+      settings = defaultSettings ++ Seq(
+          name := "Scala.js Engine"
+      )
+  ).aggregate(exampleEngine)
+
   lazy val exampleSettings = defaultSettings ++ baseScalaJSSettings ++ Seq(
       /* Add the library classpath this way to escape the dependency between
        * tasks. This avoids to recompile the library every time we compile an
@@ -205,6 +213,15 @@ object ScalaJSBuild extends Build {
       }
   )
 
+  lazy val exampleEngine = Project(
+      id = "engine",
+      base = file("examples") / "engine",
+      settings = exampleSettings ++ Seq(
+          name := "NL Engine - Scala.js example",
+          moduleName := "engine"
+      )
+  ).dependsOn(compiler)
+
   lazy val exampleHelloWorld = Project(
       id = "helloworld",
       base = file("examples") / "helloworld",
```

### Appendix B

```
$(function() {
  var World   = ScalaJS.modules.org\ufe33nlogo\ufe33engine\ufe33World();
  var myWorld = new World(0, 0, 0, 0);
});
```

### Appendix C

```
tortoise=<path to NetLogo/tortoise repo>
cp $tortoise/src/main/scalajs/org/nlogo/engine/*.scala .
cd ../..
sbt engine/optimize-js
cd -
cp ./target/scala-2.10/engine* $tortoise/target/resource_managed/main/js
```
