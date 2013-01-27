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
  getNeighbors: -> world.getNeighbors(@pxcor, @pycor) # world.getTopology().getNeighbors(this)

class World
  # any variables used in the constructor should come
  # before the constructor, else they get overwritten after it.
  _nextId = 0
  _turtles = []
  _patches = []
  width = 0
  _topology = null
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->
    collectUpdates()
    width = (@maxPxcor - @minPxcor) + 1
    _topology = new Torus(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
    nested =
      for y in [@maxPycor..@minPycor]
        for x in [@minPxcor..@maxPxcor]
          new Patch((width * (@maxPycor - y)) + x - @minPxcor, x, y)
    # http://stackoverflow.com/questions/4631525/concatenating-an-array-of-arrays-in-coffeescript
    _patches = [].concat nested...
    for p in _patches
      updated(p, "pxcor", "pycor", "pcolor", "plabel", "plabelcolor")
  topology: -> _topology
  turtles:  -> _turtles
  patches:  -> _patches
  # TODO: this needs to support all topologies
  getPatchAt: (x, y) ->
    index  = (@maxPycor - Math.round(y)) * width + (Math.round(x) - @minPxcor)
    return _patches[index]
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
  getNeighbors: (pxcor, pycor) -> @topology().getNeighbors(pxcor, pycor)

class Agents
  count: (x) -> x.length
  _currentAgent: 0
  currentAgent: -> @_currentAgent
  askAgent: (a, f) ->
    oldAgent = @_currentAgent
    @_currentAgent = a
    res = f()
    @_currentAgent = oldAgent
    res
  ask: (agents, f) ->
    (@askAgent(a, f) for a in agents)
    return
  agentFilter: (agents, f) -> a for a in agents when @askAgent(a, f)
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
  getNeighbors: -> AgentSet.currentAgent().getNeighbors()
  patch: (x, y) ->
    p = world.getPatchAt(x, y)
    console.log("found this patch at #{x} #{y}")
    console.log(p)
    p

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


class Torus
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->

  getNeighbors: (pxcor, pycor) ->
    if (pxcor == @maxPxcor && pxcor == @minPxcor)
      if (pycor == @maxPycor && pycor == @minPycor) []
      else  [@getPatchNorth(pxcor, pycor), @getPatchSouth(pxcor, pycor)]
    else if (pycor == @maxPycor && pycor == @minPycor)
      [@getPatchEast(pxcor, pycor), @getPatchWest(pxcor, pycor)]
    else [@getPatchNorth(pxcor, pycor),     @getPatchEast(pxcor, pycor),
          @getPatchSouth(pxcor, pycor),     @getPatchWest(pxcor, pycor),
          @getPatchNorthEast(pxcor, pycor), @getPatchSouthEast(pxcor, pycor),
          @getPatchSouthWest(pxcor, pycor), @getPatchNorthWest(pxcor, pycor)]

  getPatchNorth: (pxcor, pycor) ->
    if (pycor == @maxPycor)
      world.getPatchAt(pxcor, @minPycor)
    else
      world.getPatchAt(pxcor, pycor + 1)

  getPatchSouth: (pxcor, pycor) ->
    if (pycor == @minPycor) 
      world.getPatchAt(pxcor, @maxPycor)
    else
      world.getPatchAt(pxcor, pycor - 1)

  getPatchEast: (pxcor, pycor) ->
    if (pxcor == @maxPxcor) 
      world.getPatchAt(@minPxcor, pycor)
    else
      world.getPatchAt(pxcor + 1, pycor)

  getPatchWest: (pxcor, pycor) ->
    if (pxcor == @minPxcor) 
      world.getPatchAt(@maxPxcor, pycor)
    else
      world.getPatchAt(pxcor - 1, pycor)

  getPatchNorthWest: (pxcor, pycor) ->
    if (pycor == @maxPycor) 
      if (pxcor == @minPxcor) 
        world.getPatchAt(@maxPxcor, @minPycor)
      else
        world.getPatchAt(pxcor - 1, @minPycor)
      
     else if (pxcor == @minPxcor) 
      world.getPatchAt(@maxPxcor, pycor + 1)
    else
      world.getPatchAt(pxcor - 1, pycor + 1)
    
  getPatchSouthWest: (pxcor, pycor) ->
    if (pycor == @minPycor) 
      if (pxcor == @minPxcor) 
        world.getPatchAt(@maxPxcor, @maxPycor)
      else
        world.getPatchAt(pxcor - 1, @maxPycor)
    else if (pxcor == @minPxcor)
      world.getPatchAt(@maxPxcor, pycor - 1)
    else
      world.getPatchAt(pxcor - 1, pycor - 1)

  getPatchSouthEast: (pxcor, pycor) ->
    if (pycor == @minPycor) 
      if (pxcor == @maxPxcor)
        world.getPatchAt(@minPxcor, @maxPycor)
      else
        world.getPatchAt(pxcor + 1, @maxPycor)
    else if (pxcor == @maxPxcor)
      world.getPatchAt(@minPxcor, pycor - 1)
    else
      world.getPatchAt(pxcor + 1, pycor - 1)
    
  getPatchNorthEast: (pxcor, pycor) ->
    if (pycor == @maxPycor) 
      if (pxcor == @maxPxcor) 
        world.getPatchAt(@minPxcor, @minPycor)
      else
        world.getPatchAt(pxcor + 1, @minPycor)
    else if (pxcor == @maxPxcor)
      world.getPatchAt(@minPxcor, pycor + 1)
    else
      world.getPatchAt(pxcor + 1, pycor + 1)
