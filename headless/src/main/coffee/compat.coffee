# from: http://coffeescriptcookbook.com/chapters/arrays/filtering-arrays
# this works with the coffee command, but is absent in Rhino.
unless Array::filter
  Array::filter = (callback) ->
    element for element in this when callback(element)

# Rhino has "print" and "println", V8 and browsers have "console.log".  we use println,
# so ensure it is present
unless println
  println = console.log
