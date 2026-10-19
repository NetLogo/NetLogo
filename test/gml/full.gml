graph
[
  Creator Gephi
  node
  [
    id "1"
    label "1"
    graphics
    [
      x -355.93912
      y -114.81057
      z 0.0
      w 10.0
      h 10.0
      d 10.0
      fill "#999999"
    ]
    tvar "true"
    breed "MICE"
    fur "white"
    missing "foo"
  ]
  node
  [
    id "2"
    label "2"
    graphics
    [
      x -170.28754
      y 214.69585
      z 0.0
      w 10.0
      h 10.0
      d 10.0
      fill "#999999"
    ]
    tvar "false"
    breed "FROGS"
    spots "true"
    missing "foo"
  ]
  node
  [
    id "3"
    label "3"
    graphics
    [
      x 526.2267
      y -99.88528
      z 0.0
      w 10.0
      h 10.0
      d 10.0
      fill "#999999"
    ]
    tvar "false"
    breed "FROGS"
    spots "true"
    missing "foo"
  ]
  edge
  [
    id "16"
    source "1"
    target "2"
    value 1.0
    directed 1
    breed "directed-edges"
    lvar "5.0"
  ]
  edge
  [
    id "17"
    source "3"
    target "1"
    value 10.0
    directed 0
    breed "undirected-edges"
  ]
]
