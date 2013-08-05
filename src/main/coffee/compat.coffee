##
## stuff in this file papers over differences between Rhino, Nashorn,
## and other JS implementations such as Node as the ones in browsers.
##
## on Rhino/Nashorn, the goal is precisely bit-for-bit identical
## results as JVM NetLogo.  elsewhere, "close enough" is close enough
##

# Rhino has "println" already, Nashorn needs to borrow it from
# System.out, V8 and browsers have "console.log". get it somehow!
unless println?
  if console?
    println = console.log
  unless println?
    println = java.lang.System.out.println

# on Rhino, we provide this via MersenneTwisterFast.  in the browser,
# we delegate to Math.random(), for speed.  we could swap in a JS
# implementation of the Mersenne Twister (code for it is googlable),
# but I fear (though have not measured) the performance impact
unless Random?
  Random = {}
  Random.nextInt = (limit) -> Math.floor(Math.random() * limit)
  Random.nextLong = Random.nextInt
  Random.nextDouble = -> Math.random()

# on Rhino, we use the JVM StrictMath stuff so results are identical
# with regular NetLogo. in browser, be satisfied with "close enough"
unless StrictMath?
  StrictMath = Math
  Math.toRadians = (degrees) ->
    degrees * Math.PI / 180

# So ScalaJS can check types
`typeOf = function(x) { return typeof x; }`
