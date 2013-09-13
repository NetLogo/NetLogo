turtleBuiltins = ["id", "color", "heading", "xcor", "ycor", "shape", "label", "labelcolor", "breed", "hidden", "size", "pensize", "penmode"]
patchBuiltins = ["pxcor", "pycor", "pcolor", "plabel", "plabelcolor"]

Updates = []

collectUpdates = ->
  result =
    if (Updates.length == 0)
      [turtles: {}, patches: {}]
    else
      Updates
  Updates = []
  result

# gross hack - ST 1/25/13
died = (id) ->
  update = patches: {}, turtles: {}
  update.turtles[id] = WHO: -1
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
  vars: []
  constructor: (@id, @color, @heading, @xcor, @ycor, @shape = "default", @label = "", @labelcolor = 9.9, @breed ="TURTLES", @hidden = false, @size = 1.0, @pensize = 1.0, @penmode = "up") ->
    updated(this, turtleBuiltins...)
    @vars = (x for x in TurtlesOwn.vars)
  toString: -> "(turtle " + @id + ")"
  keepHeadingInRange: ->
    if (@heading < 0 || @heading >= 360)
      @heading = ((@heading % 360) + 360) % 360
    return
  patchAhead: (amount) ->
    newX = world.topology().wrap(@xcor + amount * Trig.sin(@heading),
        world.minPxcor - 0.5, world.maxPxcor + 0.5)
    newY = world.topology().wrap(@ycor + amount * Trig.cos(@heading),
        world.minPycor - 0.5, world.maxPycor + 0.5)
    return world.getPatchAt(newX, newY)
  fd: (amount) ->
    @xcor = world.topology().wrap(@xcor + amount * Trig.sin(@heading),
        world.minPxcor - 0.5, world.maxPxcor + 0.5)
    @ycor = world.topology().wrap(@ycor + amount * Trig.cos(@heading),
        world.minPycor - 0.5, world.maxPycor + 0.5)
    updated(this, "xcor", "ycor")
    return
  right: (amount) ->
    @heading += amount
    @keepHeadingInRange()
    updated(this, "heading")
    return
  setxy: (x, y) ->
    @xcor = x
    @ycor = y
    updated(this, "xcor", "ycor")
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
      @vars[n - turtleBuiltins.length]
  setTurtleVariable: (n, v) ->
    if (n < turtleBuiltins.length)
      this[turtleBuiltins[n]] = v
      updated(this, turtleBuiltins[n])
    else
      @vars[n - turtleBuiltins.length] = v
  getPatchHere: -> world.getPatchAt(@xcor, @ycor)
  getPatchVariable: (n)    -> @getPatchHere().getPatchVariable(n)
  setPatchVariable: (n, v) -> @getPatchHere().setPatchVariable(n, v)
  turtlesHere: ->
    p = @getPatchHere()
    t for t in world.turtles() when t.getPatchHere() == p

class Patch
  vars: []
  constructor: (@id, @pxcor, @pycor, @pcolor = 0.0, @plabel = "", @plabelcolor = 9.9) ->
    @vars = (x for x in PatchesOwn.vars)
  toString: -> "(patch " + @pxcor + " " + @pycor + ")"
  getPatchVariable: (n) ->
    if (n < patchBuiltins.length)
      this[patchBuiltins[n]]
    else
      @vars[n - patchBuiltins.length]
  setPatchVariable: (n, v) ->
    if (n < patchBuiltins.length)
      this[patchBuiltins[n]] = v
      updated(this, patchBuiltins[n])
    else
      @vars[n - patchBuiltins.length] = v
  getNeighbors: -> world.getNeighbors(@pxcor, @pycor) # world.getTopology().getNeighbors(this)
  sprout: (n) ->
    (world.createturtle(@pxcor, @pycor, 5 + 10 * Random.nextInt(14), Random.nextInt(360)) for num in [0...n])
    return

class World
  # any variables used in the constructor should come
  # before the constructor, else they get overwritten after it.
  _nextId = 0
  _turtles = []
  _patches = []
  width = 0
  _topology = null
  _ticks = false
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->
    collectUpdates()
    width = (@maxPxcor - @minPxcor) + 1
    _topology = new Torus(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
    @createPatches()
  createPatches: ->
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
  resetTicks: -> _ticks = 0
  clearTicks: -> _ticks = false
  tick: ->
    if(_ticks == false)
      throw new Error("Need to call reset-ticks")
    _ticks++
  advancetick: (n) ->
    if(_ticks == false)
      throw new Error("Need to call reset-ticks")
    if(n < 0)
      throw new Error("Cannot advance ticks by a negative amount")
    _ticks += n
  ticks: ->
    if(_ticks == false)
      throw new Error("Need to call reset-ticks")
    _ticks
  # TODO: this needs to support all topologies
  getPatchAt: (x, y) ->
    index  = (@maxPycor - StrictMath.round(y)) * width + (StrictMath.round(x) - @minPxcor)
    return _patches[index]
  removeTurtle: (id) ->
    _turtles = @turtles().filter (t) -> t.id != id
    return
  clearall: ->
    Globals.init(Globals.vars.length)
    for t in @turtles()
      t.die()
    @createPatches()
    _nextId = 0
    _ticks = false
    return
  createturtle: (x, y, color, heading) ->
    _turtles.push(new Turtle((_nextId++), color, heading, x, y))
    return
  createorderedturtles: (n) ->
    (@createturtle(0, 0, (num * 10 + 5) % 140, num * (360 / n)) for num in [0...n])
    return
  createturtles: (n) ->
    (@createturtle(0, 0, 5 + 10 * Random.nextInt(14), Random.nextInt(360)) for num in [0...n])
    return
  getNeighbors: (pxcor, pycor) -> @topology().getNeighbors(pxcor, pycor)

class Agents
  count: (x) -> x.length
  any: (x) -> x.length > 0
  _self: 0
  self: -> @_self
  askAgent: (a, f) ->
    oldAgent = @_self
    @_self = a
    res = f()
    @_self = oldAgent
    res
  ask: (agentsOrAgent, f) ->
    agents = agentsOrAgent
    if (! (typeIsArray agentsOrAgent))
      agents = [agentsOrAgent]
    (@askAgent(a, f) for a in agents)
    return
  agentFilter: (agents, f) -> a for a in agents when @askAgent(a, f)
  of: (agents, f) -> @askAgent(a, f) for a in agents
  oneOf: (agents) -> agents[Random.nextInt(agents.length)]
  # I'm putting some things in Agents, and some in Prims
  # I did that on purpose to show how arbitrary/confusing this seems.
  # May we should put *everything* in Prims, and Agents can be private.
  # Prims could/would/should be the compiler/runtime interface.
  die: -> @_self.die()
  getTurtleVariable: (n)    -> @_self.getTurtleVariable(n)
  setTurtleVariable: (n, v) -> @_self.setTurtleVariable(n, v)
  getPatchVariable:  (n)    -> @_self.getPatchVariable(n)
  setPatchVariable:  (n, v) -> @_self.setPatchVariable(n, v)


Prims =
  fd: (n) -> AgentSet.self().fd(n)
  bk: (n) -> AgentSet.self().fd(-n)
  right: (n) -> AgentSet.self().right(n)
  left: (n) -> AgentSet.self().right(-n)
  setxy: (x, y) -> AgentSet.self().setxy(x, y)
  getNeighbors: -> AgentSet.self().getNeighbors()
  sprout: (n) -> AgentSet.self().sprout(n)
  patch: (x, y) -> world.getPatchAt(x, y)
  randomxcor: -> world.minPxcor - 0.5 + Random.nextDouble() * (world.maxPxcor - world.minPxcor + 1)
  randomycor: -> world.minPycor - 0.5 + Random.nextDouble() * (world.maxPycor - world.minPycor + 1)
  shadeOf: (c1, c2) -> Math.floor(c1 / 10) == Math.floor(c2 / 10)
  randomfloat: (n) -> n * Random.nextDouble()
  list: (xs...) -> xs
  max: (xs) -> Math.max(xs...)
  min: (xs) -> Math.min(xs...)
  sum: (xs) -> xs.reduce((a, b) -> a + b)
  sort: (xs) -> xs.sort()
  removeDuplicates: (xs) ->
    result = {}
    result[xs[key]] = xs[key] for key in [0...xs.length]
    value for key, value of result

Globals =
  vars: []
  # compiler generates call to init, which just
  # tells the runtime how many globals there are.
  # they are all initialized to 0
  init: (n) -> @vars = (0 for x in [0...n])
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
    if (StrictMath.abs(x) < 3.2e-15)
      0
    else
      x
  sin: (degrees) ->
    @squash(StrictMath.sin(StrictMath.toRadians(degrees)))
  cos: (degrees) ->
    @squash(StrictMath.cos(StrictMath.toRadians(degrees)))
  unsquashedSin: (degrees) ->
    StrictMath.sin(StrictMath.toRadians(degrees))
  unsquashedCos: (degrees) ->
    StrictMath.cos(StrictMath.toRadians(degrees))


class Torus
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->

  # based on agent.Topology.wrap()
  wrap: (pos, min, max) ->
    if (pos >= max)
      (min + ((pos - max) % (max - min)))
    else if (pos < min)
      result = max - ((min - pos) % (max - min))
      if (result < max)
        result
      else
        min
    else
      pos

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
