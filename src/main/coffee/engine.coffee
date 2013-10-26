turtleBuiltins = ["id", "color", "heading", "xcor", "ycor", "shape", "label", "labelcolor", "breed", "hidden", "size", "pensize", "penmode"]
patchBuiltins = ["pxcor", "pycor", "pcolor", "plabel", "plabelcolor"]

class NetLogoException
  constructor: (@message) ->
class DeathInterrupt extends NetLogoException
class TopologyInterrupt extends NetLogoException

Updates = []

Nobody = {
  toString: -> "nobody"
}

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

noop = (vars...) ->

updated = (obj, vars...) ->
  # is there some less simpleminded way we could build this? surely there
  # must be. my CoffeeScript fu is stoppable - ST 1/24/13
  change = {}
  for v in vars
    if (v == "plabelcolor")
      change["PLABEL-COLOR"] = obj[v]
    else if (v == "breed")
      change["BREED"] = obj[v].name
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
  else if(obj instanceof Patch)
    update.turtles = {}
    update.patches = oneUpdate
  Updates.push(update)
  return

class Turtle
  vars: []
  constructor: (@color = 0, @heading = 0, @xcor = 0, @ycor = 0, breed = Breeds.get("TURTLES"), @label = "", @labelcolor = 9.9, @hidden = false, @size = 1.0, @pensize = 1.0, @penmode = "up") ->
    @breedvars = {}
    @updateBreed(breed)
    @vars = (x for x in TurtlesOwn.vars)
  updateBreed: (breed) ->
    @breed = breed
    @shape = @breed.shape()
    if(@breed != Breeds.get("TURTLES"))
      for x in @breed.vars
        if(@breedvars[x] == undefined)
          @breedvars[x] = 0
  setBreed: (breed) ->
    @updateBreed(breed)
    updated(this, "breed")
    updated(this, "shape")
  toString: -> "(" + @breed.singular + " " + @id + ")"
  keepHeadingInRange: ->
    if (@heading < 0 || @heading >= 360)
      @heading = ((@heading % 360) + 360) % 360
    return
  canMove: (amount) -> @patchAhead(amount) != Nobody
  distancexy: (x, y) ->
    StrictMath.sqrt(StrictMath.pow(world.topology().shortestX(@xcor, x), 2) +
                    StrictMath.pow(world.topology().shortestY(@ycor, y), 2))
  distance: (agent) ->
    if (agent instanceof Turtle)
      @distancexy(agent.xcor, agent.ycor)
    else if(agent instanceof Patch)
      @distancexy(agent.pxcor, agent.pycor)
  inRadius: (agents, radius) ->
    new Agents(a for a in agents.items when @distance(a) <= radius)
  patchAt: (dx, dy) ->
    try
      newX = world.topology().wrapX(@xcor + dx,
          world.minPxcor - 0.5, world.maxPxcor + 0.5)
      newY = world.topology().wrapY(@ycor + dy,
          world.minPycor - 0.5, world.maxPycor + 0.5)
      return world.getPatchAt(newX, newY)
    catch error
      if error instanceof TopologyInterrupt then Nobody else throw error
  patchRightAndAhead: (angle, amount) ->
    heading = @heading + angle
    if (heading < 0 || heading >= 360)
      heading = ((heading % 360) + 360) % 360
    try
      newX = world.topology().wrapX(@xcor + amount * Trig.sin(heading),
          world.minPxcor - 0.5, world.maxPxcor + 0.5)
      newY = world.topology().wrapY(@ycor + amount * Trig.cos(heading),
          world.minPycor - 0.5, world.maxPycor + 0.5)
      return world.getPatchAt(newX, newY)
    catch error
      if error instanceof TopologyInterrupt then Nobody else throw error
  patchLeftAndAhead: (angle, amount) ->
    @patchRightAndAhead(-angle, amount)
  patchAhead: (amount) ->
    @patchRightAndAhead(0, amount)
  fd: (amount) ->
    if amount > 0
      while amount >= 1 and @canMove(1)
        @jump(1)
        amount -= 1
      if amount > 0 and @canMove(amount)
        @jump(amount)
    else if amount < 0
      while amount <= -1 and @canMove(-1)
        @jump(-1)
        amount += 1
      if amount < 0 and @canMove(amount)
        @jump(amount)
    return
  jump: (amount) ->
    if @canMove(amount)
      @xcor = world.topology().wrapX(@xcor + amount * Trig.sin(@heading),
          world.minPxcor - 0.5, world.maxPxcor + 0.5)
      @ycor = world.topology().wrapY(@ycor + amount * Trig.cos(@heading),
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
  hideTurtle: (flag) ->
    @hidden = flag
    updated(this, "hidden")
    return
  isBreed: (breedName) ->
    @breed.name == breedName
  die: ->
    if (@id != -1)
      world.removeTurtle(@id)
      died(@id)
      @id = -1
    throw new DeathInterrupt("Call only from inside an askAgent block")
  getTurtleVariable: (n) ->
    if (n < turtleBuiltins.length)
      this[turtleBuiltins[n]]
    else
      @vars[n - turtleBuiltins.length]
  setTurtleVariable: (n, v) ->
    if (n < turtleBuiltins.length)
      if (n == 5)  # shape
        v = v.toLowerCase()
      this[turtleBuiltins[n]] = v
      if (n == 2)  # heading
        @keepHeadingInRange()
      updated(this, turtleBuiltins[n])
    else
      @vars[n - turtleBuiltins.length] = v
  getBreedVariable: (n) -> @breedvars[n]
  setBreedVariable: (n, v) -> @breedvars[n] = v
  getPatchHere: -> world.getPatchAt(@xcor, @ycor)
  getPatchVariable: (n)    -> @getPatchHere().getPatchVariable(n)
  setPatchVariable: (n, v) -> @getPatchHere().setPatchVariable(n, v)
  getNeighbors: -> @getPatchHere().getNeighbors()
  getNeighbors4: -> @getPatchHere().getNeighbors4()
  turtlesHere: -> @getPatchHere().turtlesHere()
  breedHere: (breedName) ->
    p = @getPatchHere()
    new Agents(t for t in world.turtlesOfBreed(breedName).items when t.getPatchHere() == p, Breeds.get(breedName))
  hatch: (n, breedName) ->
    breed = if breedName then Breeds.get(breedName) else @breed
    newTurtles = []
    for num in [0...n]
      t = new Turtle(@color, @heading, @xcor, @ycor, breed, @label, @labelcolor, @hidden, @size, @pensize, @penmode)
      for v in TurtlesOwn.vars
        t.setTurtleVariable(turtleBuiltins.length + v, @getTurtleVariable(turtleBuiltins.length + v))
      newTurtles.push(world.createturtle(t))
    new Agents(newTurtles, breed)
  moveto: (agent) ->
    if (agent instanceof Turtle)
      @setxy(agent.xcor, agent.ycor)
    else if(agent instanceof Patch)
      @setxy(agent.pxcor, agent.pycor)

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
      if(patchBuiltins[n] == "pcolor" && v != 0)
        world.patchesAllBlack(false)
      updated(this, patchBuiltins[n])
    else
      @vars[n - patchBuiltins.length] = v
  distancexy: (x, y) ->
    StrictMath.sqrt(StrictMath.pow(world.topology().shortestX(@pxcor, x), 2) +
                    StrictMath.pow(world.topology().shortestY(@pycor, y), 2))
  distance: (agent) ->
    if (agent instanceof Turtle)
      @distancexy(agent.xcor, agent.ycor)
    else if(agent instanceof Patch)
      @distancexy(agent.pxcor, agent.pycor)
  turtlesHere: -> new Agents(t for t in world.turtles().items when t.getPatchHere() == this, Breeds.get("TURTLES"))
  getNeighbors: -> world.getNeighbors(@pxcor, @pycor) # world.getTopology().getNeighbors(this)
  getNeighbors4: -> world.getNeighbors4(@pxcor, @pycor) # world.getTopology().getNeighbors(this)
  sprout: (n, breedName) ->
    breed = if("" == breedName) then Breeds.get("TURTLES") else Breeds.get(breedName)
    new Agents(world.createturtle(new Turtle(5 + 10 * Random.nextInt(14), Random.nextInt(360), @pxcor, @pycor, breed)) for num in [0...n])

class World
  # any variables used in the constructor should come
  # before the constructor, else they get overwritten after it.
  _nextId = 0
  _turtles = []
  _patches = []
  _topology = null
  _ticks = -1
  _timer = Date.now()
  _patchesAllBlack = true
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor, @patchSize, @wrappingAllowedInY, @wrappingAllowedInX, turtleShapeList, linkShapeList, @interfaceGlobalCount) ->
    collectUpdates()
    Updates.push(
      {
        world: {
          0: {
            worldWidth: Math.abs(@minPxcor - @maxPxcor) + 1,
            worldHeight: Math.abs(@minPycor - @maxPycor) + 1,
            minPxcor: @minPxcor,
            minPycor: @minPycor,
            maxPxcor: @maxPxcor,
            maxPycor: @maxPycor,
            nbInterfaceGlobals: @interfaceGlobalCount,
            linkBreeds: "XXX IMPLEMENT ME",
            linkShapeList: linkShapeList,
            patchSize: @patchSize,
            patchesAllBlack: _patchesAllBlack,
            patchesWithLabels: 0,
            ticks: _ticks,
            turtleBreeds: "XXX IMPLEMENT ME",
            turtleShapeList: turtleShapeList,
            unbreededLinksAreDirected: false
            wrappingAllowedInX: @wrappingAllowedInX,
            wrappingAllowedInY: @wrappingAllowedInY
          }
        }
      })
    @resize(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
  createPatches: ->
    nested =
      for y in [@maxPycor..@minPycor]
        for x in [@minPxcor..@maxPxcor]
          new Patch((@width() * (@maxPycor - y)) + x - @minPxcor, x, y)
    # http://stackoverflow.com/questions/4631525/concatenating-an-array-of-arrays-in-coffeescript
    _patches = [].concat nested...
    for p in _patches
      updated(p, "pxcor", "pycor", "pcolor", "plabel", "plabelcolor")
  topology: -> _topology
  turtles: () -> new Agents(_turtles, Breeds.get("TURTLES"))
  turtlesOfBreed: (breedName) ->
    breed = Breeds.get(breedName)
    new Agents((_turtles.filter (t) -> t.breed == breed ), breed)
  patches: -> new Agents(_patches)
  resetTimer: ->
    _timer = Date.now()
  resetTicks: ->
    _ticks = 0
    Updates.push( world: { 0: { ticks: _ticks } } )
  clearTicks: ->
    _ticks = -1
    Updates.push( world: { 0: { ticks: _ticks } } )
  resize: (minPxcor, maxPxcor, minPycor, maxPycor) ->
    if(minPxcor > 0 || maxPxcor < 0 || minPycor > 0 || maxPycor < 0)
      throw new NetLogoException("You must include the point (0, 0) in the world.")
    @minPxcor = minPxcor
    @maxPxcor = maxPxcor
    @minPycor = minPycor
    @maxPycor = maxPycor
    if(@wrappingAllowedInX && @wrappingAllowedInY)
      _topology = new Torus(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
    else if(@wrappingAllowedInX)
      _topology = new VertCylinder(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
    else if(@wrappingAllowedInY)
    else
      _topology = new Box(@minPxcor, @maxPxcor, @minPycor, @maxPycor)
    for t in @turtles().items
      try
        t.die()
      catch error
        throw error if !(error instanceof DeathInterrupt)
    @createPatches()
    Updates.push(
      world: {
        0: {
          worldWidth: Math.abs(@minPxcor - @maxPxcor) + 1,
          worldHeight: Math.abs(@minPycor - @maxPycor) + 1,
          minPxcor: @minPxcor,
          minPycor: @minPycor,
          maxPxcor: @maxPxcor,
          maxPycor: @maxPycor
        }
      }
    )
  tick: ->
    if(_ticks == -1)
      throw new NetLogoException("The tick counter has not been started yet. Use RESET-TICKS.")
    _ticks++
    Updates.push( world: { 0: { ticks: _ticks } } )
  advancetick: (n) ->
    if(_ticks == -1)
      throw new NetLogoException("The tick counter has not been started yet. Use RESET-TICKS.")
    if(n < 0)
      throw new NetLogoException("Cannot advance the tick counter by a negative amount.")
    _ticks += n
    Updates.push( world: { 0: { ticks: _ticks } } )
  timer: ->
    (Date.now() - _timer) / 1000
  ticks: ->
    if(_ticks == -1)
      throw new NetLogoException("The tick counter has not been started yet. Use RESET-TICKS.")
    _ticks
  # TODO: this needs to support all topologies
  width: () -> 1 + @maxPxcor - @minPxcor
  height: () -> 1 + @maxPycor - @minPycor
  getPatchAt: (x, y) ->
    index  = (@maxPycor - StrictMath.round(y)) * @width() + (StrictMath.round(x) - @minPxcor)
    return _patches[index]
  getTurtle: (id) ->
    filteredTurtles = (@turtles().items.filter (t) -> t.id == id)
    if filteredTurtles.length == 0 then Nobody else filteredTurtles[0]
  getTurtleOfBreed: (breedName, id) ->
    filteredTurtles = (@turtlesOfBreed(breedName).items.filter (t) -> t.id == id)
    if filteredTurtles.length == 0 then Nobody else filteredTurtles[0]
  removeTurtle: (id) ->
    _turtles = @turtles().items.filter (t) -> t.id != id
    return
  patchesAllBlack: (val) ->
    _patchesAllBlack = val
    Updates.push( world: { 0: { patchesAllBlack: _patchesAllBlack }})
  clearall: ->
    Globals.clear(@interfaceGlobalCount)
    for t in @turtles().items
      try
        t.die()
      catch error
        throw error if !(error instanceof DeathInterrupt)
    @createPatches()
    _nextId = 0
    @patchesAllBlack(true)
    @clearTicks()
    return
  createturtle: (t) ->
    t.id = _nextId++
    updated(t, turtleBuiltins...)
    _turtles.push(t)
    t
  createorderedturtles: (n, breedName) ->
    new Agents(@createturtle(new Turtle((10 * num + 5) % 140, (360 * num) / n, 0, 0, Breeds.get(breedName))) for num in [0...n])
  createturtles: (n, breedName) ->
    new Agents(@createturtle(new Turtle(5 + 10 * Random.nextInt(14), Random.nextInt(360), 0, 0, Breeds.get(breedName))) for num in [0...n])
  getNeighbors: (pxcor, pycor) -> @topology().getNeighbors(pxcor, pycor)
  getNeighbors4: (pxcor, pycor) -> @topology().getNeighbors4(pxcor, pycor)

AgentSet =
  count: (x) -> x.items.length
  any: (x) -> x.items.length > 0
  _self: 0
  _myself: 0
  self: -> @_self
  myself: -> if @_myself != 0 then @_myself else throw new NetLogoException("There is no agent for MYSELF to refer to.")
  askAgent: (a, f) ->
    oldMyself = @_myself
    oldAgent = @_self
    @_myself = @_self
    @_self = a
    try
      res = f()
    catch error
      throw error if!(error instanceof DeathInterrupt)
    @_self = oldAgent
    @_myself = oldMyself
    res
  ask: (agentsOrAgent, shuffle, f) ->
    if(agentsOrAgent.items)
      agents = agentsOrAgent.items
    else
      agents = [agentsOrAgent]
    iter =
      if (shuffle)
        new Shufflerator(agents)
      else
        new Iterator(agents)
    while (iter.hasNext())
      a = iter.next()
      @askAgent(a, f)
    return
  agentFilter: (agents, f) -> new Agents(a for a in agents.items when @askAgent(a, f))
  of: (agentsOrAgent, f) ->
    isagentset = agentsOrAgent.items
    if(isagentset)
      agents = agentsOrAgent.items
    else
      agents = [agentsOrAgent]
    result = []
    iter = new Shufflerator(agents)
    while (iter.hasNext())
      a = iter.next()
      result.push(@askAgent(a, f))
    if isagentset
      result
    else
      result[0]
  oneOf: (agentsOrList) ->
    isagentset = agentsOrList.items
    if(isagentset)
      l = agentsOrList.items
    else
      l = agentsOrList
    if l.length == 0 then Nobody else l[Random.nextInt(l.length)]
  nOf: (resultSize, agentsOrList) ->
    items = agentsOrList.items
    if(!items)
      throw new Error("n-of not implemented on lists yet")
    new Agents(
      switch resultSize
        when 0
          []
        when 1
          [items[Random.nextInt(items.length)]]
        when 2
          index1 = Random.nextInt(items.length)
          index2 = Random.nextInt(items.length - 1)
          [index1, index2] =
            if index2 >= index1
              [index1, index2 + 1]
            else
              [index2, index1]
          [items[index1], items[index2]]
        else
          i = 0
          j = 0
          result = []
          while j < resultSize
            if Random.nextInt(items.length - i) < resultSize - j
              result.push(items[i])
              j += 1
            i += 1
          result
    )
  # I'm putting some things in Agents, and some in Prims
  # I did that on purpose to show how arbitrary/confusing this seems.
  # May we should put *everything* in Prims, and Agents can be private.
  # Prims could/would/should be the compiler/runtime interface.
  die: -> @_self.die()
  getTurtleVariable: (n)    -> @_self.getTurtleVariable(n)
  setTurtleVariable: (n, v) -> @_self.setTurtleVariable(n, v)
  getBreedVariable: (n)    -> @_self.getBreedVariable(n)
  setBreedVariable: (n, v) -> @_self.setBreedVariable(n, v)
  setBreed: (agentSet) -> @_self.setBreed(agentSet.breed)
  getPatchVariable:  (n)    -> @_self.getPatchVariable(n)
  setPatchVariable:  (n, v) -> @_self.setPatchVariable(n, v)

class Agents
  constructor: (@items, @breed) ->
  toString: ->
    "(" + @items.length + " " + @breed.name + ")"

class Iterator
  constructor: (@agents) ->
  i: 0
  hasNext: -> @i < @agents.length
  next: ->
    result = @agents[@i]
    @i = @i + 1
    result

class Shufflerator
  constructor: (@agents) ->
    @agents = @agents[..]
    @fetch()
  i: 0
  nextOne: null
  hasNext: -> @nextOne != null
  next: ->
    result = @nextOne
    @fetch()
    result
  fetch: ->
    if (@i >= @agents.length)
      @nextOne = null
    else
      if (@i < @agents.length - 1)
        r = @i + Random.nextInt(@agents.length - @i)
        @nextOne = @agents[r]
        @agents[r] = @agents[@i]
      else
        @nextOne = @agents[@i]
      @i = @i + 1
    return

Prims =
  fd: (n) -> AgentSet.self().fd(n)
  bk: (n) -> AgentSet.self().fd(-n)
  right: (n) -> AgentSet.self().right(n)
  left: (n) -> AgentSet.self().right(-n)
  setxy: (x, y) -> AgentSet.self().setxy(x, y)
  getNeighbors: -> AgentSet.self().getNeighbors()
  getNeighbors4: -> AgentSet.self().getNeighbors4()
  sprout: (n, breedName) -> AgentSet.self().sprout(n, breedName)
  hatch: (n, breedName) -> AgentSet.self().hatch(n, breedName)
  patch: (x, y) -> world.getPatchAt(x, y)
  randomxcor: -> world.minPxcor - 0.5 + Random.nextDouble() * (world.maxPxcor - world.minPxcor + 1)
  randomycor: -> world.minPycor - 0.5 + Random.nextDouble() * (world.maxPycor - world.minPycor + 1)
  shadeOf: (c1, c2) -> Math.floor(c1 / 10) == Math.floor(c2 / 10)
  equality: (a, b) ->
    if(a == undefined || b == undefined)
      throw new Error("Checking equality on undefined is an invalid condition")
    if(a == b)
      true
    else if (typeIsArray(a) && typeIsArray(b))
      a.length == b.length && a.every (elem, i) -> elem is b[i]
    else
      false
  scaleColor: (color, number, min, max) ->
    color = Math.floor(color / 10) * 10
    perc = 0.0
    if(min > max)
      if(number < max)
        perc = 1.0
      else if (number > min)
        perc = 0.0
      else
        tempval = min - number
        tempmax = min - max
        perc = tempval / tempmax
    else
      if(number > max)
        perc = 1.0
      else if (number < min)
        perc = 0.0
      else
        tempval = number - min
        tempmax = max - min
        perc = tempval / tempmax
    perc *= 10
    if(perc >= 9.9999)
      perc = 9.9999
    if(perc < 0)
      perc = 0
    color + perc
  randomfloat: (n) -> n * Random.nextDouble()
  list: (xs...) -> xs
  item: (n, xs) -> xs[n]
  first: (xs) -> xs[0]
  last: (xs) -> xs[xs.length - 1]
  fput: (x, xs) -> [x].concat(xs)
  lput: (x, xs) ->
    result = xs[..]
    result.push(x)
    result
  butfirst: (xs) -> xs[1..]
  butlast: (xs) -> xs[0..xs.length - 1]
  length: (xs) -> xs.length
  _int: (n) -> if n < 0 then Math.ceil(n) else Math.floor(n)
  max: (xs) -> Math.max(xs...)
  min: (xs) -> Math.min(xs...)
  mean: (xs) -> @sum(xs) / xs.length
  sum: (xs) -> xs.reduce((a, b) -> a + b)
  sort: (xs) -> xs.sort()
  removeDuplicates: (xs) ->
    result = {}
    result[xs[key]] = xs[key] for key in [0...xs.length]
    value for key, value of result
  outputprint: (x) ->
    println(Dump(x))
  patchset: (inputs...) ->
    # O(n^2) -- should be smarter (use hashing for contains check)
    result = []
    recurse = (inputs) ->
      for input in inputs
        if (typeIsArray(input))
          recurse(input)
        else if (input instanceof Patch)
          result.push(input)
        else
          for agent in input.items
            if (!(agent in result))
              result.push(agent)
    recurse(inputs)
    new Agents(result)

Globals =
  vars: []
  # compiler generates call to init, which just
  # tells the runtime how many globals there are.
  # they are all initialized to 0
  init: (n) -> @vars = (0 for x in [0...n])
  clear: (n) ->
    @vars[i] = 0 for i in [n...@vars.length]
    return
  getGlobal: (n) -> @vars[n]
  setGlobal: (n, v) -> @vars[n] = v

TurtlesOwn =
  vars: []
  init: (n) -> @vars = (0 for x in [0..n-1])

PatchesOwn =
  vars: []
  init: (n) -> @vars = (0 for x in [0..n-1])

# like api.Dump. will need more cases. for now at least knows
# about lists.
Dump = (x) ->
  if (typeIsArray(x))
    "[" + (Dump(x2) for x2 in x).join(" ") + "]"
  else
    "" + x

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

class Breed
  constructor: (@name, @singular, @_shape = false) ->
  shape: () -> if @_shape then @_shape else Breeds.get("TURTLES")._shape
  vars: []

Breeds = {
  breeds: [new Breed("TURTLES", "turtle", "default")]
  add: (name, singular) ->
    @breeds.push(new Breed(name, singular))
  get: (name) ->
    (@breeds.filter (b) -> b.name == name)[0]
  setDefaultShape: (agents, shape) ->
    agents.breed._shape = shape.toLowerCase()
}
class Topology
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
    new Agents((patch for patch in @_getNeighbors(pxcor, pycor) when patch != false))

  _getNeighbors: (pxcor, pycor) ->
    if (pxcor == @maxPxcor && pxcor == @minPxcor)
      if (pycor == @maxPycor && pycor == @minPycor) []
      else [@getPatchNorth(pxcor, pycor), @getPatchSouth(pxcor, pycor)]
    else if (pycor == @maxPycor && pycor == @minPycor)
      [@getPatchEast(pxcor, pycor), @getPatchWest(pxcor, pycor)]
    else [@getPatchNorth(pxcor, pycor),     @getPatchEast(pxcor, pycor),
          @getPatchSouth(pxcor, pycor),     @getPatchWest(pxcor, pycor),
          @getPatchNorthEast(pxcor, pycor), @getPatchSouthEast(pxcor, pycor),
          @getPatchSouthWest(pxcor, pycor), @getPatchNorthWest(pxcor, pycor)]

  getNeighbors4: (pxcor, pycor) ->
    new Agents((patch for patch in @_getNeighbors4(pxcor, pycor) when patch != false))

  _getNeighbors4: (pxcor, pycor) ->
    if (pxcor == @maxPxcor && pxcor == @minPxcor)
      if (pycor == @maxPycor && pycor == @minPycor) []
      else [@getPatchNorth(pxcor, pycor), @getPatchSouth(pxcor, pycor)]
    else if (pycor == @maxPycor && pycor == @minPycor)
      [@getPatchEast(pxcor, pycor), @getPatchWest(pxcor, pycor)]
    else [@getPatchNorth(pxcor, pycor),     @getPatchEast(pxcor, pycor),
          @getPatchSouth(pxcor, pycor),     @getPatchWest(pxcor, pycor)]

class Torus extends Topology
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->

  wrapX: (pos) ->
    @wrap(pos, @minPxcor - 0.5, @maxPxcor + 0.5)
  wrapY: (pos) ->
    @wrap(pos, @minPycor - 0.5, @maxPycor + 0.5)
  shortestX: (x1, x2) ->
    if(StrictMath.abs(x1 - x2) > (1 + @maxPxcor - @minPxcor) / 2)
      world.width() - StrictMath.abs(x1 - x2)
    else
      StrictMath.abs(x1 - x2)
  shortestY: (y1, y2) ->
    if(StrictMath.abs(y1 - y2) > (1 + @maxPycor - @minPycor) / 2)
      world.height() - StrictMath.abs(y1 - y2)
    else
      StrictMath.abs(y1 - y2)
  diffuse: (vn, amount) ->
    scratch = for x in [0...world.width()]
      []
    for patch in world.patches().items
      scratch[patch.pxcor - @minPxcor][patch.pycor - @minPycor] = patch.getPatchVariable(vn)
    for patch in world.patches().items
      pxcor = patch.pxcor
      pycor = patch.pycor
      # We have to order the neighbors exactly how Torus.java:diffuse does them so we don't get floating discrepancies.  FD 10/19/2013
      diffusallyOrderedNeighbors =
        [@getPatchSouthWest(pxcor, pycor), @getPatchWest(pxcor, pycor),
         @getPatchNorthWest(pxcor, pycor), @getPatchSouth(pxcor, pycor),
         @getPatchNorth(pxcor, pycor), @getPatchSouthEast(pxcor, pycor),
         @getPatchEast(pxcor, pycor), @getPatchNorthEast(pxcor, pycor)]
      diffusalSum = (scratch[n.pxcor - @minPxcor][n.pycor - @minPycor] for n in diffusallyOrderedNeighbors).reduce((a, b) -> a + b)
      patch.setPatchVariable(vn, patch.getPatchVariable(vn) * (1.0 - amount) + (diffusalSum / 8) * amount)

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

class VertCylinder extends Topology
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->

  wrapX: (pos) ->
    @wrap(pos, @minPxcor - 0.5, @maxPxcor + 0.5)
  wrapY: (pos) ->
    if(pos >= @maxPycor + 0.5 || pos <= @minPycor - 0.5)
      throw new TopologyInterrupt ("Cannot move turtle beyond the world's edge.")
    else pos

class Box extends Topology
  constructor: (@minPxcor, @maxPxcor, @minPycor, @maxPycor) ->

  shortestX: (x1, x2) -> StrictMath.abs(x1 - x2)
  shortestY: (y1, y2) -> StrictMath.abs(y1 - y2)
  wrapX: (pos) ->
    if(pos >= @maxPxcor + 0.5 || pos <= @minPxcor - 0.5)
      throw new TopologyInterrupt ("Cannot move turtle beyond the world's edge.")
    else pos
  wrapY: (pos) ->
    if(pos >= @maxPycor + 0.5 || pos <= @minPycor - 0.5)
      throw new TopologyInterrupt ("Cannot move turtle beyond the world's edge.")
    else pos

  getPatchNorth: (pxcor, pycor) -> (pycor != @maxPycor) && world.getPatchAt(pxcor, pycor + 1)
  getPatchSouth: (pxcor, pycor) -> (pycor != @minPycor) && world.getPatchAt(pxcor, pycor - 1)
  getPatchEast: (pxcor, pycor) -> (pxcor != @maxPxcor) && world.getPatchAt(pxcor + 1, pycor)
  getPatchWest: (pxcor, pycor) -> (pxcor != @minPxcor) && world.getPatchAt(pxcor - 1, pycor)

  getPatchNorthWest: (pxcor, pycor) -> (pycor != @maxPycor) && (pxcor != @minPxcor) && world.getPatchAt(pxcor - 1, pycor + 1)
  getPatchSouthWest: (pxcor, pycor) -> (pycor != @minPycor) && (pxcor != @minPxcor) && world.getPatchAt(pxcor - 1, pycor - 1)
  getPatchSouthEast: (pxcor, pycor) -> (pycor != @minPycor) && (pxcor != @maxPxcor) && world.getPatchAt(pxcor + 1, pycor - 1)
  getPatchNorthEast: (pxcor, pycor) -> (pycor != @maxPycor) && (pxcor != @maxPxcor) && world.getPatchAt(pxcor + 1, pycor + 1)

  diffuse: (vn, amount) ->
    yy = world.height()
    xx = world.width()
    scratch = for x in [0...xx]
      for y in [0...yy]
        world.getPatchAt(x + @minPxcor, y + @minPycor).getPatchVariable(vn)
    scratch2 = for x in [0...xx]
      for y in [0...yy]
        0
    for y in [0...yy]
      for x in [0...xx]
        diffuseVal = (scratch[x][y] / 8) * amount
        if (y > 0 && y < yy - 1 && x > 0 && x < xx - 1)
          scratch2[x    ][y    ] += scratch[x][y] - (8 * diffuseVal)
          scratch2[x - 1][y - 1] += diffuseVal
          scratch2[x - 1][y    ] += diffuseVal
          scratch2[x - 1][y + 1] += diffuseVal
          scratch2[x    ][y + 1] += diffuseVal
          scratch2[x    ][y - 1] += diffuseVal
          scratch2[x + 1][y - 1] += diffuseVal
          scratch2[x + 1][y    ] += diffuseVal
          scratch2[x + 1][y + 1] += diffuseVal
        else if (y > 0 && y < yy - 1)
          if (x == 0)
            scratch2[x    ][y    ] += scratch[x][y] - (5 * diffuseVal)
            scratch2[x    ][y + 1] += diffuseVal
            scratch2[x    ][y - 1] += diffuseVal
            scratch2[x + 1][y - 1] += diffuseVal
            scratch2[x + 1][y    ] += diffuseVal
            scratch2[x + 1][y + 1] += diffuseVal
          else
            scratch2[x    ][y    ] += scratch[x][y] - (5 * diffuseVal)
            scratch2[x    ][y + 1] += diffuseVal
            scratch2[x    ][y - 1] += diffuseVal
            scratch2[x - 1][y - 1] += diffuseVal
            scratch2[x - 1][y    ] += diffuseVal
            scratch2[x - 1][y + 1] += diffuseVal
        else if (x > 0 && x < xx - 1)
          if (y == 0)
            scratch2[x    ][y    ] += scratch[x][y] - (5 * diffuseVal)
            scratch2[x - 1][y    ] += diffuseVal
            scratch2[x - 1][y + 1] += diffuseVal
            scratch2[x    ][y + 1] += diffuseVal
            scratch2[x + 1][y    ] += diffuseVal
            scratch2[x + 1][y + 1] += diffuseVal
          else
            scratch2[x    ][y    ] += scratch[x][y] - (5 * diffuseVal)
            scratch2[x - 1][y    ] += diffuseVal
            scratch2[x - 1][y - 1] += diffuseVal
            scratch2[x    ][y - 1] += diffuseVal
            scratch2[x + 1][y    ] += diffuseVal
            scratch2[x + 1][y - 1] += diffuseVal
        else if (x == 0)
          if (y == 0)
            scratch2[x    ][y    ] += scratch[x][y] - (3 * diffuseVal)
            scratch2[x    ][y + 1] += diffuseVal
            scratch2[x + 1][y    ] += diffuseVal
            scratch2[x + 1][y + 1] += diffuseVal
          else
            scratch2[x    ][y    ] += scratch[x][y] - (3 * diffuseVal)
            scratch2[x    ][y - 1] += diffuseVal
            scratch2[x + 1][y    ] += diffuseVal
            scratch2[x + 1][y - 1] += diffuseVal
        else if (y == 0)
          scratch2[x    ][y    ] += scratch[x][y] - (3 * diffuseVal)
          scratch2[x    ][y + 1] += diffuseVal
          scratch2[x - 1][y    ] += diffuseVal
          scratch2[x - 1][y + 1] += diffuseVal
        else
          scratch2[x    ][y    ] += scratch[x][y] - (3 * diffuseVal)
          scratch2[x    ][y - 1] += diffuseVal
          scratch2[x - 1][y    ] += diffuseVal
          scratch2[x - 1][y - 1] += diffuseVal
    for y in [0...yy]
      for x in [0...xx]
        world.getPatchAt(x + @minPxcor, y + @minPycor).setPatchVariable(vn, scratch2[x][y])
