class Turtle
  constructor: (@id, @x, @y, @heading) ->
  fd: (amount) -> @y += amount # TODO: put a real calculation here...

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

class Agents
  count: (x) => x.length
  _currentAgent: 0
  currentAgent: => @_currentAgent
  askAgent: (a, f) =>
    oldAgent = @_currentAgent
    @_currentAgent = a
    f()
    @_currentAgent = oldAgent
  ask: (agents, f) =>
    (@askAgent(a, f) for a in agents)
    return
  # obvious hack for now.
  getVariable: (n) => @_currentAgent.y

Prims =
  fd: (n) => AgentSet.currentAgent().fd(n)

AgentSet = new Agents

world = new World
