# from: http://coffeescriptcookbook.com/chapters/arrays/filtering-arrays
# this works with the coffee command, but is absent in Rhino.
unless Array::filter
  Array::filter = (callback) ->
    element for element in this when callback(element)

class Turtle
  constructor: (@id, @xcor, @ycor, @heading) ->
  fd: (amount) ->
    @xcor += amount * Trig.sin(@heading)
    @ycor += amount * Trig.cos(@heading)
    return
  rt: (amount) ->
    @heading += amount
    keepHeadingInRange()
    return
  lt: (amount) ->
    @heading -= amount
    keepHeadingInRange()
    return
  keepHeadingInRange = ->
    if (heading < 0 || heading >= 360)
      heading = ((heading % 360) + 360) % 360
    return
  die: () ->
    if(@id != -1)
      world.removeTurtle(@id)
      @id = -1
    return

class Patch
  constructor: (@pxcor, @pycor) ->

class World
  # any variables used in the constructor should come
  # before the constructor, else they get overwritten after it.
  _nextId = 0
  _turtles = []
  _patches = []
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->
    nested = (new Patch(x, y) for x in [@minPxcor..@maxPxcor] for y in [@minPycor..@maxPycor])
    # http://stackoverflow.com/questions/4631525/concatenating-an-array-of-arrays-in-coffeescript
    _patches = [].concat nested...
  turtles: -> _turtles
  patches: -> _patches
  removeTurtle: (id) ->
    _turtles = @turtles().filter (t) -> t.id != id
    return
  clearall: ->
    _turtles = []
    return
  createturtle: (x, y, heading) ->
    _turtles.push(new Turtle((_nextId++), x, y, heading))
    return
  createorderedturtles: (n) ->
    (@createturtle(0, 0, num * (360 / n)) for num in [0..n-1])
    return

class Agents
  count: (x) -> x.length
  _currentAgent: 0
  currentAgent: -> @_currentAgent
  askAgent: (a, f) ->
    oldAgent = @_currentAgent
    @_currentAgent = a
    f()
    @_currentAgent = oldAgent
  ask: (agents, f) ->
    (@askAgent(a, f) for a in agents)
    return
  # obvious hack for now.
  getTurtleVariable: (n) ->
    switch n
      when 3 then @_currentAgent.xcor
      when 4 then @_currentAgent.ycor
  getPatchVariable: (n) ->
    switch n
      when 0 then @_currentAgent.pxcor
      when 1 then @_currentAgent.pycor
  # I'm putting some things in Agents, and some in Prims
  # I did that on purpose to show how arbitrary/confusing this seems.
  # May we should put *everything* in Prims, and Agents can be private.
  # Prims could/would/should be the compiler/runtime interface.
  die: () -> @_currentAgent.die()

Prims =
  fd: (n) -> AgentSet.currentAgent().fd(n)

AgentSet = new Agents

world = new World(-5,5,-5,5)

Trig =
  squash: (x) ->
    if (Math.abs(x) < 3.2e-15)
      0
    else
      x
  degreesToRadians: (degrees) ->
    degrees * Math.PI / 180
  sin: (degrees) ->
    @squash(Math.sin(@degreesToRadians(degrees)))
  cos: (degrees) ->
    @squash(Math.cos(@degreesToRadians(degrees)))
