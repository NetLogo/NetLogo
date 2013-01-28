# from: http://coffeescriptcookbook.com/chapters/arrays/filtering-arrays
# this works with the coffee command, but is absent in Rhino.
unless Array::filter
  Array::filter = (callback) ->
    element for element in this when callback(element)

# Rhino has "print" and "println", V8 and browsers have "console.log".  we use println,
# so ensure it is present
unless println
  println = console.log

typeIsArray = ( value ) ->
  value and
  typeof value is 'object' and
  value instanceof Array and
  typeof value.length is 'number' and
  typeof value.splice is 'function' and
  not ( value.propertyIsEnumerable 'length' )

unless Random
  Random = {}
  Random.nextInt = (limit) -> Math.floor(Math.random() * limit)
  Random.nextLong = Random.nextInt
