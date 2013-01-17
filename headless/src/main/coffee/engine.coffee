World =
  _turtles: 0
  clearall: () => World._turtles = 0
  turtles: () => World._turtles
  crofast: (n) => World._turtles += n

AgentSet =
  count: (x) => x
