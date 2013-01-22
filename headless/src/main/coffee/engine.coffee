class Turtle
  constructor: (@id, @x, @y, @heading) ->
  fd: (amount) -> @x += amount # TODO: put a real calculation here...

class World
  _nextId = 0
  _turtles = []
  turtles: => _turtles
  clearall: =>
    _turtles = []
    return
  createturtle: (x, y, heading) =>
    _turtles.push(new Turtle((_nextId++), x, y, heading))
    return
  createorderedturtles: (n) =>
    (@createturtle(0, 0, num * (360 / n)) for num in [0..n-1])
    return

AgentSet =
  count: (x) => x.length

world = new World
