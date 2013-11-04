##
## stuff in this file papers over differences between Rhino, Nashorn,
## and other JS implementations such as Node and the ones in browsers.
##
## on Rhino/Nashorn, the goal is precisely bit-for-bit identical
## results as JVM NetLogo.  elsewhere, "close enough" is close enough
##
# from: http://coffeescriptcookbook.com/chapters/arrays/filtering-arrays
# this works with the coffee command, but is absent in Rhino.
unless Array::filter
  Array::filter = (callback) ->
    element for element in this when callback(element)

# Rhino has "println" already, Nashorn needs to borrow it from
# System.out, V8 and browsers have "console.log". get it somehow!
unless println?
  if console?
    println = console.log
  unless println?
    println = java.lang.System.out.println

# surprisingly difficult to ask if something is an array or not
typeIsArray = (value) ->
  value and
  typeof value is 'object' and
  value instanceof Array and
  typeof value.length is 'number' and
  typeof value.splice is 'function' and
  not ( value.propertyIsEnumerable 'length' )

# on Rhino, we provide this via MersenneTwisterFast.  in the browser,
# we delegate to Math.random(), for speed.  we could swap in a JS
# implementation of the Mersenne Twister (code for it is googlable),
# but I fear (though have not measured) the performance impact
unless Random?
  Random = {}
  Random.nextInt = (limit) -> Math.floor(Math.random() * limit)
  Random.nextLong = Random.nextInt
  Random.nextDouble = -> Math.random()

# For divergences between Rhino and browsers, clone and extend!
Cloner = {
  clone: (obj) ->
    return obj if obj is null or typeof (obj) isnt "object"
    temp = new obj.constructor()
    for key in Object.getOwnPropertyNames(obj)
      temp[key] = @clone(obj[key])
    temp
}

# on Rhino, we use the JVM StrictMath stuff so results are identical
# with regular NetLogo. in browser, be satisfied with "close enough"
unless StrictMath?
  StrictMath = Cloner.clone(Math)
  # For functions that are not "close enough," or that don't exist in the browser, manually define them here!
  StrictMath.toRadians = (degrees) -> degrees * Math.PI / 180
  StrictMath.toDegrees = (radians) -> radians * 180 / Math.PI
