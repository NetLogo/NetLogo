class Turtle
  constructor: (@id, @x, @y, @heading) ->
  fd: (amount) -> @x += amount # TODO: put a real calculation here...

class World
  nextId:       0
  _turtles:     []
  turtles:      => @_turtles
  count:        => @_turtles.length
  clearall:     => @_turtles = []
  createturtle: (x, y, heading) =>
    @_turtles.push(new Turtle((@nextId++), x, y, heading))
  createorderedturtles: (n) =>
    (@createturtle(0, 0, num * (360 / n)) for num in [0..n-1])

AgentSet =
  count: (x) => x.length

world = new World
