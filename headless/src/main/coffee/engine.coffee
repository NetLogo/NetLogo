turtleBuiltins = ["id", "color", "heading", "xcor", "ycor", "shape", "label", "labelcolor", "breed", "hidden", "size", "pensize", "penmode"]
patchBuiltins = ["pxcor", "pycor", "pcolor", "plabel", "plabelcolor"]

Updates = []

collectUpdates = ->
  result = JSON.stringify(
    if (Updates.length == 0)
      [turtles: {}, patches: {}]
    else
      Updates)
  Updates = []
  result

# gross hack - ST 1/25/13
died = (id) ->
  update = {}
  update.patches = {}
  update.turtles = {}
  update.turtles[id] = "WHO": -1
  Updates.push(update)
  return

updated = (obj, vars...) ->
  # is there some less simpleminded way we could build this? surely there
  # must be. my CoffeeScript fu is stoppable - ST 1/24/13
  change = {}
  for v in vars
    if (v == "plabelcolor")
      change["PLABEL-COLOR"] = obj[v]
    else if (v == "labelcolor")
      change["LABEL-COLOR"] = obj[v]
    else if (v == "pensize")
      change["PEN-SIZE"] = obj[v]
    else if (v == "penmode")
      change["PEN-MODE"] = obj[v]
    else if (v == "hidden")
      change["HIDDEN?"] = obj[v]
    else if (v == "id")
      change["WHO"] = obj[v]
    else
      change[v.toUpperCase()] = obj[v]
  oneUpdate = {}
  oneUpdate[obj.id] = change
  update = {}
  if (obj instanceof Turtle)
    update.turtles = oneUpdate
    update.patches = {}
  else
    update.turtles = {}
    update.patches = oneUpdate
  Updates.push(update)
  return

class Turtle
  _vars = []
  constructor: (@id, @color, @heading, @xcor, @ycor, @shape = "default", @label = "", @labelcolor = 9.9, @breed ="TURTLES", @hidden = false, @size = 1.0, @pensize = 1.0, @penmode = "up") ->
    updated(this, turtleBuiltins...)
    @_vars = TurtlesOwn.vars
  keepHeadingInRange: ->
    if (@heading < 0 || @heading >= 360)
      @heading = ((@heading % 360) + 360) % 360
    return
  vars: -> @_vars
  fd: (amount) ->
    @xcor += amount * Trig.sin(@heading)
    @ycor += amount * Trig.cos(@heading)
    updated(this, "xcor", "ycor")
    return
  right: (amount) ->
    @heading += amount
    @keepHeadingInRange()
    updated(this, "heading")
    return
  die: ->
    if (@id != -1)
      world.removeTurtle(@id)
      died(@id)
      @id = -1
    return
  getTurtleVariable: (n) ->
    if (n < turtleBuiltins.length)
      this[turtleBuiltins[n]]
    else
       @_vars[n - turtleBuiltins.length]
  setTurtleVariable: (n, v) ->
    if (n < turtleBuiltins.length)
      this[turtleBuiltins[n]] = v
      updated(this, turtleBuiltins[n])
    else
       @_vars[n - turtleBuiltins.length] = v
  getPatchHere: -> world.getPatchAt(@xcor, @ycor)
  getPatchVariable: (n)    -> @getPatchHere().getPatchVariable(n)
  setPatchVariable: (n, v) -> @getPatchHere().setPatchVariable(n, v)

class Patch
  _vars = []
  constructor: (@id, @pxcor, @pycor, @pcolor = 0.0, @plabel = "", @plabelcolor = 9.9) ->
    @_vars = TurtlesOwn.vars
  getPatchVariable: (n) ->
    if (n < patchBuiltins.length)
      this[patchBuiltins[n]]
    else
       @_vars[n - patchBuiltins.length]
  setPatchVariable: (n, v) ->
    if (n < patchBuiltins.length)
      this[patchBuiltins[n]] = v
      updated(this, patchBuiltins[n])
    else
       @_vars[n - patchBuiltins.length] = v

class World
  # any variables used in the constructor should come
  # before the constructor, else they get overwritten after it.
  _nextId = 0
  _turtles = []
  _patches = []
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->
    collectUpdates()
    width = (maxPxcor - minPxcor) + 1
    nested =
      for x in [@minPxcor..@maxPxcor]
        for y in [@minPycor..@maxPycor]
          new Patch((width * (@maxPycor - y)) + x - @minPxcor, x, y)
    # http://stackoverflow.com/questions/4631525/concatenating-an-array-of-arrays-in-coffeescript
    _patches = [].concat nested...
    for p in _patches
      updated(p, "pxcor", "pycor", "pcolor", "plabel", "plabelcolor")
  turtles: -> _turtles
  patches: -> _patches
  # TODO: I do believe this is borken.
  getPatchAt: (x, y) ->
    return _patches[(Math.floor(x) - @minPxcor) + (Math.floor(y) - @minPycor)]
  removeTurtle: (id) ->
    _turtles = @turtles().filter (t) -> t.id != id
    return
  clearall: ->
    for t in @turtles()
      t.die()
    _nextId = 0
    return
  createturtle: (x, y, heading, color) ->
    _turtles.push(new Turtle((_nextId++), color, heading, x, y))
    return
  createorderedturtles: (n) ->
    (@createturtle(0, 0, num * (360 / n), (num * 10 + 5) % 140) for num in [0..n-1])
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
  # I'm putting some things in Agents, and some in Prims
  # I did that on purpose to show how arbitrary/confusing this seems.
  # May we should put *everything* in Prims, and Agents can be private.
  # Prims could/would/should be the compiler/runtime interface.
  die: -> @_currentAgent.die()
  getTurtleVariable: (n)    -> @_currentAgent.getTurtleVariable(n)
  setTurtleVariable: (n, v) -> @_currentAgent.setTurtleVariable(n, v)
  getPatchVariable:  (n)    -> @_currentAgent.getPatchVariable(n)
  setPatchVariable:  (n, v) -> @_currentAgent.setPatchVariable(n, v)


Prims =
  fd: (n) -> AgentSet.currentAgent().fd(n)
  bk: (n) -> AgentSet.currentAgent().fd(-n)
  right: (n) -> AgentSet.currentAgent().right(n)
  left: (n) -> AgentSet.currentAgent().right(-n)

Globals =
  vars: []
  # compiler generates call to init, which just
  # tells the runtime how many globals there are.
  # they are all initialized to 0
  init: (n) -> @vars = (0 for x in [0..n-1])
  getGlobal: (n) -> @vars[n]
  setGlobal: (n, v) -> @vars[n] = v

TurtlesOwn =
  vars: []
  init: (n) -> @vars = (0 for x in [0..n-1])

PatchesOwn =
  vars: []
  init: (n) -> @vars = (0 for x in [0..n-1])

AgentSet = new Agents

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
