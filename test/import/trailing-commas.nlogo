globals 
[
  clock tick-length          ;; clock variables
  bounce?
  collisions?
  number
  box-x box-y                ;; patch coords of box's upper right corner
  total-particle-number
]

breed [ particles particle ]

particles-own 
[
  speed mass                 ;; particle info
  last-collision
]
