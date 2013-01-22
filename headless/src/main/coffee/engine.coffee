class Turtle
  constructor: (@id) ->
  x: 0
  y: 0
  fd: (amount) -> @x += amount

class World
  nextId: 0
  _turtles: []
  turtles: => @_turtles
  count: => @_turtles.length
  clearall: => @_turtles = []
  createturtle: () =>
    @_turtles.push(new Turtle @nextId)
    @nextId++
  createorderedturtles: (n) => (@createturtle() for num in [1..n])

AgentSet =
  count: (x) => x.length

world = new World
