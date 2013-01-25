Updates = []

collectUpdates = ->
  result = JSON.stringify(Updates)
  Updates = []
  result

updated = (obj, vars...) ->
  # is there some less simpleminded way we could build this? surely there
  # must be. my CoffeeScript fu is stoppable - ST 1/24/13
  change = {}
  for v in vars
    change[v] = obj[v]
  oneUpdate = {}
  oneUpdate[obj.id] = change
  update = {}
  update.turtles = oneUpdate
  update.patches = {}
  Updates.push(update)
  return

class Turtle
  _vars = []
  constructor: (@id, @xcor, @ycor, @heading) ->
    updated(this, "xcor", "ycor", "heading")
    @_vars = TurtlesOwn.vars
  vars: -> @_vars
  fd: (amount) ->
    @xcor += amount * Trig.sin(@heading)
    @ycor += amount * Trig.cos(@heading)
    updated(this, "xcor", "ycor")
    return
  rt: (amount) ->
    @heading += amount
    keepHeadingInRange()
    updated(this, "heading")
    return
  lt: (amount) ->
    @heading -= amount
    keepHeadingInRange()
    updated(this, "heading")
    return
  keepHeadingInRange = ->
    if (heading < 0 || heading >= 360)
      heading = ((heading % 360) + 360) % 360
    return
  die: () ->
    if(@id != -1)
      world.removeTurtle(@id)
      @id = -1
      updated(this, "id")
    return
  # TODO: add the rest of the turtle variables here
  getTurtleVariable: (n) ->
    switch n
      when 3 then @xcor
      when 4 then @ycor
      # case for turtles-own variables
      else @_vars[n-13]

  # TODO: add the rest of the turtle variables here
  setTurtleVariable: (n, v) ->
    switch n
      when 3 then @xcor = v
      when 4 then @ycor = v
      # case for turtles-own variables
      else @_vars[n-13] = v

class Patch
  _vars = []
  constructor: (@pxcor, @pycor, @pcolor) ->
    @_vars = TurtlesOwn.vars
  # TODO: add the rest of the patch variables here.
  getPatchVariable: (n) ->
    switch n
      when 0 then @pxcor
      when 1 then @pycor
      when 2 then @pcolor
      # case for patches-own variables
      else @_vars[n-5]
    # TODO: add the rest of the patch variables here.
  setPatchVariable: (n, v) ->
    switch n
      when 0 then @pxcor = v
      when 1 then @pycor = v
      when 2 then @pcolor = v
      # case for patches-own variables
      else @_vars[n-5] = v

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
    # TODO: need to kill the turtles off one by one so they emit death cries in JSON
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
  # I'm putting some things in Agents, and some in Prims
  # I did that on purpose to show how arbitrary/confusing this seems.
  # May we should put *everything* in Prims, and Agents can be private.
  # Prims could/would/should be the compiler/runtime interface.
  die: () -> @_currentAgent.die()
  getTurtleVariable: (n)    -> @_currentAgent.getTurtleVariable(n)
  setTurtleVariable: (n, v) -> @_currentAgent.setTurtleVariable(n, v)
  getPatchVariable:  (n)    -> @_currentAgent.getPatchVariable(n)
  setPatchVariable:  (n, v) -> @_currentAgent.setPatchVariable(n, v)


Prims =
  fd: (n) -> AgentSet.currentAgent().fd(n)

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

# this gets overridden by the compiler,
# but not yet for compareCommands
world = new World(-5,5,-5,5)

