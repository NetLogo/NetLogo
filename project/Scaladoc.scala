object Scaladoc {

  val excludedPackages = Seq(
    "app.previewcommands"
  , "awt"
  , "compile"
  , "core.prim"
  , "gl"
  , "hubnet"
  , "job"
  , "lex"
  , "log"
  , "mc"
  , "parse"
  , "plot"
  , "properties"
  , "sdm"
  , "shape"
  , "widget"
  , "window"
  , "generate"
  , "lab"
  , "prim"
  , "swing"
  ).map( (p) => s"org.nlogo.$p")

}
