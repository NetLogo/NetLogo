class AgentModel
  constructor: ->
    @turtles = {}
    @patches = {}
    @links = {}
    @observer = {}
    @world = {}

  updates: (modelUpdates) ->
    for u in modelUpdates
      @update(u)
    return

  update: (modelUpdate) -> # boolean
    anyUpdates = false
    for turtleId, varUpdates of modelUpdate.turtles
      anyUpdates = true
      if varUpdates == null || varUpdates['WHO'] == -1  # old and new death formats
        delete @turtles[turtleId]
      else
        t = @turtles[turtleId]
        if not t?
          t = @turtles[turtleId] = {}
        mergeObjectInto(varUpdates, t)
    for patchId, varUpdates of modelUpdate.patches
      anyUpdates = true
      p = @patches[patchId]
      p ?= @patches[patchId] = {}
      mergeObjectInto(varUpdates, p)
    for linkId, varUpdates of modelUpdate.links
      anyUpdates = true
      if varUpdates == null || varUpdates['WHO'] == -1
        delete @links[linkId]
      else
        l = @links[linkId]
        l ?= @links[linkId] = {}
        mergeObjectInto(varUpdates, l)
    mergeObjectInto(modelUpdate.observer, @observer)
    if(modelUpdate.world && modelUpdate.world[0])
      mergeObjectInto(modelUpdate.world[0], @world)
    anyUpdates

  mergeObjectInto = (updatedObject, targetObject) ->
    for variable, value of updatedObject
      targetObject[variable.toLowerCase()] = value
    return
