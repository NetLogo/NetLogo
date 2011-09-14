;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variables ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals [
  cloud-line      ;; y coordinate of bottom row of cloud
  surface        ;; y coordinate of top row of earth
  top-of-cloud
  bottom-of-cloud
  dust-probability
  fade-probability
  lightning-struck?
  hit-tree?
]

breed [trees my-tree]
breed [clouds cloud]
breed [positive-cloud-ions positive-cloud-ion]
breed [negative-cloud-ions negative-cloud-ion]
breed [positive-ground-ions positive-ground-ion]
breed [step-leaders step-leader]
breed [positive-streamers positive-streamer]


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;; Setup ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to setup
  clear-all
  setup-world
  reset-ticks
end

;; ;;;;;; Setup World ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; set up the world background
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to setup-world
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; set global variable values
  set cloud-line 100
  set surface -50; set the height of the earth
  set top-of-cloud 86
  set bottom-of-cloud (75 - (size-of-cloud))
  set dust-probability 70 ;; the probability the ionzation will happen to a dust particle
  set fade-probability 1 ;; the probability a path of ionized air will end or fade
  set lightning-struck? false
  set hit-tree? false
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; set colors for the patches in the background
  ask patches [
    if pycor = surface ;; set color of the earth surface with input number of positive streamers
      [set pcolor green - 1] 
    if pycor > (cloud-line - 1)
      [set pcolor gray]    ;;set color of the cloud
    if pycor > surface and pycor < cloud-line
      [set pcolor black]  ;; set color of the sky 
    if pycor < surface 
      [set pcolor green] ;; set color of the earth
  ]
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; create the trees in the meadow based on the slider value for the number of trees
  create-trees number-of-trees [
    ;; half the trees are trees and half are pines
    ifelse ((random 2) = 0) 
    [set shape "myTree"]
    [set shape "myPineTree"]
    set size ((random 20) + 15)  
    setxy random-pxcor (surface + size / 2) 
  ]
  
  ;;create cloud
  create-clouds 1 [
    set shape "cloud"
    set size (size-of-cloud * 20)
    setxy 0 75
  ]
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;create Ions in the world and cloud
  create-positive-cloud-ions strength-of-field
  create-negative-cloud-ions strength-of-field
  create-positive-ground-ions strength-of-field
  
  ask positive-cloud-ions [
    set shape "+"
    set size 7
    setxy random-x-in-cloud (top-of-cloud - random-y-in-cloud)
  ]
  
  ask negative-cloud-ions [
    set shape "-"
    set size 7
    setxy random-x-in-cloud (bottom-of-cloud - random-y-in-cloud) 
  ]  
  
  ask positive-ground-ions [
    set shape "+"
    set size 7
    setxy random-x-in-cloud (surface - random 15) ;; distribute randomly along the surface, beneath the cloud area
  ]
  
  setup-streamers-leaders
  add-dust 
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;create and place the streamers and leaders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to setup-streamers-leaders
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;create lightning makers
  create-step-leaders strength-of-field  
  ask step-leaders [
    set shape "negativeStrm"
    set size 5
    let xcoor ((random (size-of-cloud * 15)) - (size-of-cloud * 7))
    setxy xcoor (bottom-of-cloud - (size-of-cloud * 2))
    facexy xcoor surface
  ]
  
  create-positive-streamers strength-of-field
  ask positive-streamers [
    set shape "positiveStrm"
    set size 5
    let xcoor random-x-in-cloud
    setxy xcoor surface
    while [(pcolor = lime) or (pcolor = turquoise) or (pcolor = brown)] [
      set ycor (ycor + 1) 
    ]
    facexy xcoor bottom-of-cloud
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The ions move around as a result of bouncing around and colliding (not explicitly modelled).
;; There is a charge separation within the cloud, the cause of which is not entirely understood,
;; although there are theories.  The separation is illustrated in this model, but the underlying
;; cause/process of the separation is not explicitly modelled.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to make-ions-move
  ask positive-cloud-ions [ ;; set the position to a random X, and change the Y by a random amount to stay in upper area of cloud
    setxy random-x-in-cloud (top-of-cloud - random-y-in-cloud)
  ]
  ask negative-cloud-ions [;; set the position to a random X, and change the Y by a random amount to stay in lower area of cloud
    setxy random-x-in-cloud (bottom-of-cloud - random-y-in-cloud) 
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; determine the area of the cloud
;;; and randomly select positions from width and height (X and Y)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; get a random X coordinate within the cloud's area
to-report random-x-in-cloud
  ;; the width is determined by multiplying the size-of-cloud variable by 15
  ;; however, we subtract a multiple of the size to make the distribution cover the positive and negative regions of the axis 
  report random (size-of-cloud * 15) - (size-of-cloud * 7)
end  

;; get a random Y coordinate within the cloud's area
to-report random-y-in-cloud
  ;; the height of the cloud is determined by doubling the size-of-cloud variable
  report random (size-of-cloud * 2)
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; disperse dust into the air
;;; these particles influence the path of the lightning
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to add-dust
  ask n-of dust patches with [pycor > surface  and pycor < cloud-line] [
    set pcolor gray - 2
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Runtime Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to go 
  ;; if lightning has struck, clear the faded paths as the charge has now been neutralized 
  if lightning-struck? [
    clear-fades ;; if there's been a lightning strike, clear all the faded paths
    stop  
  ]
  
  ;; if the leaders and streamers have all died or faded, stop the model
  if (not any? step-leaders) and (not any? positive-streamers) [
    stop
  ]
  
  ;; since the positive streamers grow at a slower rate than the step leaders,
  ;; we keep track of the time so make them move at appropriate times compared to each other
  
  ;; the step leaders leave the cloud and move towards the earth at one patch per tick
  move-step-leaders-down
  
  ;; meanwhile, the positive streamers extend from the earth towards the cloud, but at a slower rate
  if ticks > 50
    [grow-positive-streamers-up]
  
  ;; as time moves on the ions move around the cloud
  make-ions-move
  
  tick 
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get the step leaders to move towards the earth
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to move-step-leaders-down
  let movex 0
  let movey 0
  
  ask step-leaders[
    set moveX 1000 ;; default values to indicate that the move coordinates have not been set
    set moveY 1000
    
    ask neighbors with [pcolor = gray - 2] [ ;; if the patch is gray then the leader 
                                             ;; has reached a dust particle and is more likely 
                                             ;; move that direction and ionize it
      if random 100 < dust-probability [
        set movex pxcor
        set movey pycor
      ]
    ]
    
    ;; if the stepleaders sense a positive streamer they move towards it
    let xcoor xcor
    let ycoor ycor
    
    ;; so we check for positive streamers between the leader and the ground
    ask patches in-cone 10 60 [
      
      ifelse any? trees-here ;; if any tree exists in the local area
      [
        ifelse ((xcoor - pxcor) > 0)   ;; check if it is to the left
          [set movex (xcoor - 1)] ;; then move one step to the left
          [set movex (xcoor + 1)] ;; otherwise, it is to the right, so move one step to the right
        
        set movey (ycoor - 1) ;; move down one step closer to the surface
      ]         
      [
        if any? positive-streamers-here ;; if one exists then the leader moves in the direction towards it
          [ ifelse ((xcoor - pxcor) > 0)   ;; if the positive streamer is to the left
            [set movex (xcoor - 1)] ;; then move one step to the left
            [set movex (xcoor + 1)] ;; otherwise, it is to the right, so move one step to the right
          
          set movey (ycoor - 1) ] ;; move down one step closer to the surface
      ]]
    
    ;; if no charges or dust particles are encouraging direction, pick at random but most likely toward earth
    ;; the random numbers are used to show the probabilities of ionized air moving in specific directions. 
    ;; the path will move based on some probability of ten by selecting a random number below 10.
    ;; these probabilities are not scientific and are used only to help guide the path
    
    if movex = 1000 [
      
      ;;set y movement
      let rand-no random 10
      
      ;; if that number is greater than 2, the path will move directly down one towards earth (80%). 
      ;; if it is less than 2 the path won't move (10%)
      ;; if it is 2 the path will move up one towards the cloud (10%)
      
      ifelse (rand-no > 2)[
        set movey ycor - 1
      ][
      ifelse (rand-no < 2)[
        set movey ycor
      ][
      set movey ycor + 1
      ]
      ]
      
      ;; if that number is greater than 6, the path will move to the left (40%). 
      ;; if it is less than 2 the path won't move (20%)
      ;; if it is between 2 and 5 the path will move to the right (40%)
      
      ;;set x movement
      set rand-no random 10
      
      ifelse (rand-no > 6)[
        set movex xcor - 1
      ][
      ifelse (rand-no < 3)[
        set movex xcor
      ][
      set movex xcor + 1
      ]
      ]     
    ]
    
    ;; make sure its still in the bounds of the world; otherwise, the path fades
    ifelse (movex < min-pxcor) or (movex > max-pxcor) or (movey > max-pycor)
      [die]
      [If (random 100 < fade-probability) ;; these is some chance (the fade-probability) that the path will fade on its own
        [die]   
      setxy movex movey
      ]
    
    ;; check if the path hit a tree
    did-hit-tree
    
    ;; and change whatever patch the leader has moved to plasma, or a blue patch
    ask patch-here [
      ifelse (any? positive-streamers-here or  ;; if that patch is a positive streamer, or
        (pcolor = (violet + 3)) or               ;; the path of a streamer, or
        (hit-tree?) or                         ;; a tree, or
        (pycor = surface))                       ;; the earth's surface  
      [ make-lightning movex movey                     ;; then lightning strikes and we turn the patch yellow
        set pcolor yellow ]
      [ set pcolor (blue + 3)]                     ;; otherwise, turn that patch of air to plasma (ie, a blue patch)
    ]
  ] 
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; here we approximate the position of trees using a circle roughly the size of the trees
;; if the step leader path reaches the tree, lightning strikes
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to did-hit-tree
  ask patches in-radius 12 [
    if any? trees-here 
      [ set hit-tree? true ]
  ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; grow the positive streamers toward the cloud
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to grow-positive-streamers-up
  
  ask positive-streamers [
    let movex 0
    let movey 0
    
    ;;;;;;;;;;;;;;;;;;; if the patch is gray then the leader has reached a dust particle and is more likely to ionize it
    ask neighbors [
      if pcolor = gray - 2 [ 
        if random 100 < dust-probability [
          set movex pxcor
          set movey pycor
        ]
      ]
    ]
    
    ;;;;;;;;;;;;;;;;; generate random direction moves for the path to grow
    if (movex = 0)[    
      ;;set y movement
      let rand-no random 10
      
      ifelse (rand-no < 8)[
        set movey ycor + 1
      ][
      ifelse (rand-no < 2)[
        set movey ycor
      ][
      set movey ycor - 1
      ]
      ]
      
      ;;set x movement
      set rand-no random 11       
      ifelse (rand-no < 5)[
        set movex xcor - 1
      ][
      ifelse (rand-no < 2)[
        set movex xcor
      ][
      set movex xcor + 1
      ]
      ]     
    ]
    
    ;;;;;;;;;;;;;;;;; if the movement is off the screen, kill the stream    
    let random-growth-height (surface + random 20 + 20)
    
    ifelse (moveX < min-pxcor) or (movex > max-pxcor) or 
      (moveY < surface) or (movey > max-pycor) or
      (moveY > random-growth-height)
      [die]
      [if (random 100 < fade-probability)  ;;there is some probability that the stream will just end 
        [die]   
      setxy movex movey
      ]
    
    ;;;;;;;;;;;;;;;;; if the patch is empty then change the color to show the path is now positive
    ;;;;;;;;;;;;;;;;; if the patch has both a stepleader and a positive streamer the paths connect and lightning strikes!
    ask patch-here [
      ifelse (any? step-leaders-here or (pcolor = (blue + 3)))
        [make-lightning movex movey
          set pcolor yellow ]
        [set pcolor (violet + 3)]
    ]
    
  ] ;; end ask +streamers
end

;;; ;;;;;;; Make Lightning ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; we'll check all the neighboring patches to see if they've been ionized (or turned light blue color)
;; if they have been they are now charged and send it to their neighbors as well
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to make-lightning [x y]
  set lightning-struck? true
  create-strike x y
end

to create-strike [x y]
  if (y < bottom-of-cloud) and (y > (surface - 1)) [
    ask neighbors [
      if (pcolor = (violet + 3)) or (pcolor = (blue + 3)) [  
        set pcolor yellow
        make-lightning pxcor pycor
      ]
      if pcolor = black [
        set pcolor white
      ]
    ]
  ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;clear the remaining positive streamers from the sky
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to clear-fades  
  ask patches [
    if (pcolor = (violet + 3)) or (pcolor = (blue + 3)) [
      set pcolor black
    ]
  ]
  ask positive-streamers [ die ]
  ask step-leaders [ die ]
end
@#$#@#$#@
GRAPHICS-WINDOW
215
20
744
570
86
86
3.0
1
10
1
1
1
0
0
0
1
-86
86
-86
86
1
1
1
ticks
15.0

BUTTON
15
20
105
53
NIL
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
110
20
200
53
NIL
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
15
180
200
213
dust
dust
0
5000
2000
10
1
NIL
HORIZONTAL

SLIDER
15
60
200
93
strength-of-field
strength-of-field
1
10
8
1
1
NIL
HORIZONTAL

SLIDER
15
100
200
133
size-of-cloud
size-of-cloud
2
8
8
1
1
NIL
HORIZONTAL

SLIDER
15
140
200
173
number-of-trees
number-of-trees
0
10
2
1
1
NIL
HORIZONTAL

@#$#@#$#@
## WHAT IS IT?

This model shows how lightning is generated. The process consists of two phases: the production of the electric field, and the air ionization to create the bolt. This model represents the latter phase. 

Lightning is one of the most visually impressive and frequently occurring natural phenomena on Earth. However, very few people actually have a solid understanding of what causes lightning, how it works, and why it occurs. This model attempts to illustrate lightning strikes from beginning to end at the level of individual charges. The user will observe how the behavior and interaction of extremely small charges can lead to very powerful and visually impressive action of a lightning strike.

In order to thoroughly understand the phenomenon of lightning one must have a strong understanding of electrical physics and thermodynamics. In order to be accessible this model avoids such complicated topics and instead provides a more general basis for charge and particle behavior. The emphasis in this model is to understand lightning as it relates to individual charges, not the underlying forces behind the charges themselves.

## HOW IT WORKS

### How does lightning work?

The Earth (green squares) and sky (black squares) serve as the layout for the world. The cloud appears as a gray cloud-shape at the top of the skyline. Among these are three main attributes that influence the production of lightning: step leaders at the bottom of the clouds (blue negative circles), dust particles in the air (dark gray squares), and positive streamers on the surface of the Earth (red positive circles). 

The initial cause of a lightning strike is a separation of charge within a cloud.  Positively charged particles accumulate at the top of the cloud while negatively charged particles concentrate themselves at the bottom of the cloud.  Within the cloud the charges move in a random manner; however, they are constrained to their charge regions. The negative charges in the bottom of the cloud have such a high concentration that they force the electrons on the Earth's surface deep into the ground.  This also has the effect of pulling the positive charges in the Earth's surface. 

As the strength of the electric field increases the air around the cloud breaks down and converts into plasma, or ionized air.  The positive and negative components of the air itself are pulled apart and separated from each other.  This separation allows the electrons to flow through the plasma much more easily than they could through normal air.  As electrons flow through the plasma they force the air around them to become plasma in turn.  In this way electrons in the cloud "burn out" paths towards the Earth.  The paths are known as step leaders, and grow from cloud to ground in a tentacle-like manner. 

Impurities in the air may cause some patches of air to turn into plasma more easily than others.  Rather than direct lines from cloud to ground, lightning takes the path of least resistance.  In this model this is illustrated by distribution of dust particles throughout the sky. 

Streamers are the positive equivalent of the negative step leaders created by clouds.  However, streamers are not self-sufficient and thus do not grow indefinitely towards the cloud.  All objects on the Earth's surface will emit a streamer, though depending on the size and material of the object the streamer's length may vary. Streamers are released much more quickly than a step leader since they are much smaller and only extend a very limited distance.

Step leaders progress towards the ground until they encounter either the ground or a streamer.  In both cases the "circuit" is completed and charge may flow freely between cloud and ground. The large concentration of positive charges on the Earth's surface flow very quickly through the plasma stream towards the sky and neutralize the electrons in the cloud.  The flash of light that is seen is the rapid movement of charge through the air, exactly the same as the light you see during a spark of static electricity.  

Once the massive negative charge in the cloud has been neutralized the flow of charge through the air comes to a stop.  The plasma becomes de-ionized and once again becomes air as the step leaders are destroyed. The environment is now ready to begin the process anew.

### How does the model work?

There are a series of breeds that live in the world that simulate lightning. 

Trees -  These are the trees on the Earth's surface. They stand above the Earth's surface and release positive streamers.

Positive Cloud Ions -  These are the positive ions that bounce around the top of the cloud. As molecules of water evaporate, they collide into one another and exchange charges. Those with positive charges move to the top of the cloud. 

Negative Cloud Ions - These are negatively charged ions at the bottom part of the cloud. They move around the cloud and become step leaders.

Positive Ground Ions - These are the positive ions on the Earth's surface. As the electric field forms around the cloud it pulls the positive charges to the top of the Earth's surface. 

Step Leaders - These are the negative ions that branch out from the bottom of the cloud. The ions are drawn to the positive charge on the Earth's surface, particularly the positive streamers. These particles leave a path of ionized air as they travel; they tend to move towards the Earth's surface or impurities, such as dust, in the air. When the step leaders reach the Earth's surface, a positive streamer, or a tree, the path is closed and lightning strikes. There is a chance that the step leader will die and the path will fade. 

Positive Streamers - These are the positive ions that branch upwards from the Earth's surface. They tend to move towards the electric field formed by the cloud or step leaders. If the positive streamer reaches the path of ionized air or connects with a step leader then lightning strikes.

These breeds live together following specific rules elaborated below:    
At every time step, each step leader moves towards the Earth ionizing a blue path of air along the way. This path is found using the following criteria:    
- if one of the immediate neighbors is a dust particle, there is a chance that it will move to that patch     
- if there is no dust, then if one of the patches in front of it is a positiveStreamer, it moves one step towards it    
- otherwise, the leader will move in a random direction towards the Earth's surface with the highest probably being directly downwards

After the step leaders have made one move we check their new position. If they have moved of the edge of the world, then their path fades. Some paths also fade by chance.

If the path does not fade we check to see if they have closed a connection by hitting any of a tree, a positive streamer, a positive streamer's path, or the Earth's surface. If so, then lightning strikes.

When lightning strikes, the patch at which the connection took place will turn yellow to indicate a connection. From there each patch will ask its neighbors to check if they've been ionized (or are a blue or violet color). If they have been ionized, they too will turn yellow and ask their neighbors about their color. If the neighboring patches have not been ionized they will turn white to highlight the glow of the lightning bolt. This cycle continues until the the surface of the Earth and the top of the cloud are reached. This indicates that the charge has been released, and the paths are removed from the world.

Since the positive streamers (from the ground) grow at a slower rate than the step leaders, after 40 ticks have passed the streamers begin to grow up by one step for every tick. The direction of their growth is random, but most likely in the upward direction.

The ions in the clouds shift their location to indicate the motion of the particles in the cloud. 

## HOW TO USE IT

SETUP button - sets up the step leaders, dust particles and positive streamers in the world

GO button - runs the model

STRENGTH-OF-FIELD slider - changes the strength of the electric field produced within the cloud

SIZE-OF-CLOUD slider - changes the size of the cloud

NUMBER-OF-TREES slider - changes the number of trees on the Earth's surface

DUST slider - changes the number of dust particles in the sky between the cloud and the Earth's surface

The model runs until all paths have died or a bolt of lightning has been produced. 

## THINGS TO NOTICE

Notice the charges on the earth's surface and how they relate to the cloud's field and position. 

Notice how the trees and dust particles change the path taken from the cloud. The location and size of the trees influence the pull on the ionized air.

The positive streamers and the step-leaders have similar behavior as they grow; however, the speed, distance, and strategy in which they do so is quite different. Can you tell how these paths differ?

## THINGS TO TRY

Try different settings for the strength of field.  What do you notice about the amount of lightning that is formed? 

How are the strength of field and size of the cloud related? 

Does the cloud size have any effect on the lightning formed?

Does varying the amount of dust or impurities in the air have influence on the path taken by the step leaders?

## EXTENDING THE MODEL

Consider other features that influence lightning paths, such as lightning rods. How do these influence the path or amount of lightning produced?

The positive streamers have potential to grow towards the electric field of the cloud. How does their growth influence the paths? 

Currently, the shape of the cloud does not affect the direction that
a step leader starts out traveling. Modify the code to allow a user
to change the shape of the cloud, and cause step leaders to progress
perpendicularly from its surface.

## RELATED MODELS

"Climate Change" has some similarities.  In both cases, there is a relationship to cloud behavior.

"Percolation" also has similar features to the way elements move through their world.

## CREDITS AND REFERENCES

Wikipedia on Lightning:    
http://en.wikipedia.org/wiki/Lightning

How Stuff Works on Lightning:    
http://science.howstuffworks.com/nature/natural-disasters/lightning.htm

National Geographic on Lightning:    
http://environment.nationalgeographic.com/environment/natural-disasters/lightning-profile.html
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

+
false
0
Rectangle -7500403 false true 120 30 180 270
Rectangle -7500403 false true 30 120 270 180
Rectangle -2674135 true false 120 30 180 270
Rectangle -2674135 true false 30 120 270 180

-
false
0
Rectangle -13791810 true false 45 135 225 195

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -11221820 true false 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

cloud
false
2
Circle -7500403 true false 13 118 94
Circle -7500403 true false 86 101 127
Circle -7500403 true false 51 51 108
Circle -7500403 true false 118 43 95
Circle -7500403 true false 158 68 134

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -11221820 true false 45 120 255 285
Rectangle -13345367 true false 120 210 180 285
Polygon -2674135 true false 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

myburnttree
false
1
Circle -7500403 true false 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true false 65 21 108
Circle -7500403 true false 116 41 127
Circle -7500403 true false 45 90 120
Circle -7500403 true false 104 74 152

mypinetree
false
0
Rectangle -6459832 true false 120 225 180 300
Polygon -14835848 true false 150 240 240 270 150 135 60 270
Polygon -14835848 true false 150 75 75 210 150 195 225 210
Polygon -14835848 true false 150 7 90 157 150 142 210 157 150 7

mytree
false
1
Circle -13840069 true false 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -13840069 true false 65 21 108
Circle -13840069 true false 116 41 127
Circle -13840069 true false 45 90 120
Circle -13840069 true false 104 74 152

negativestrm
false
0
Circle -11221820 true false 0 0 300
Circle -16777216 true false 30 30 240
Rectangle -2674135 false false 60 120 240 180
Rectangle -11221820 true false 60 120 240 180

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

positivestrm
true
0
Circle -2674135 false false 15 15 270
Circle -2674135 false false 36 36 228
Rectangle -2674135 true false 120 60 180 240
Rectangle -2674135 true false 60 120 240 180

postivearrow
true
0
Polygon -8630108 true false 150 15 180 75 180 120 255 120 255 180 180 180 180 255 120 255 120 180 45 180 45 120 120 120 120 75 150 15

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

@#$#@#$#@
NetLogo 5.0beta4
@#$#@#$#@
setup
repeat 50 [ go ]
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
1
@#$#@#$#@
