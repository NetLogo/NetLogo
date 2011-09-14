globals [
  genes-to-phenotype                        ;; list that contains a map of genotype to phenotype
  bird-body-color  your-cage-color
  user-1-color  user-2-color  user-3-color
  this-site-id my-id                        ;; ids that keep track of user and breeding sites
  parent-female  parent-male                ;; the two parents of any potential eggs

  frequency-allele-dominant-second-trait    ;; used for keeping track of allele frequencies
  frequency-allele-dominant-first-trait
  frequency-allele-recessive-second-trait
  frequency-allele-recessive-first-trait
  frequency-allele-dominant-third-trait
  frequency-allele-recessive-third-trait
  frequency-allele-dominant-forth-trait
  frequency-allele-recessive-forth-trait

  second-trait-shape-1 second-trait-shape-2 second-trait-shape-3
  forth-shape-1 forth-shape-2 forth-shape-3

  bird-size
  current-funds                             ;; money the user has available
  instruction                               ;; which instruction the user is viewing
]



breed [birds bird]
breed [first-traits  first-trait]   ;; shape that is the crest in birds and the wings in dragons
breed [second-traits second-trait]  ;; shape that is the wings in birds and breath in dragons
breed [third-traits  third-trait]   ;; shape that determines breast in birds and in dragons
breed [forth-traits  forth-trait]   ;; shape that determines tail in birds and in dragons
breed [cages cage]
breed [users user]




patches-own [site-id owner]
birds-own         [first-genes second-genes third-genes forth-genes fifth-genes sex-gene selected? owned-by]
first-traits-own  [first-genes second-genes third-genes forth-genes fifth-genes sex-gene selected? owned-by]
second-traits-own [first-genes second-genes third-genes forth-genes fifth-genes sex-gene selected? owned-by]
third-traits-own  [first-genes second-genes third-genes forth-genes fifth-genes sex-gene selected? owned-by]
forth-traits-own  [first-genes second-genes third-genes forth-genes fifth-genes sex-gene selected? owned-by]

users-own [user-id moving-a-bird?]


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  setup procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to make-users
  create-users 1 [set user-id 1 set hidden? true]
  create-users 1 [set user-id 2 set hidden? true]
  create-users 1 [set user-id 3 set hidden? true]
end


to setup
    clear-all
    make-users
    set instruction 0
    set this-site-id 0
    set parent-female nobody
    set parent-male nobody
    set current-funds 500
    set bird-size 0.8
    ask patches [set pcolor white]
    set your-cage-color (turquoise + 4)
    set user-1-color    (pink + 4)
    set user-2-color    (green + 4)
    set user-3-color    (brown + 4)
    set bird-body-color (gray + 2.5)
    set-scenario
    set-default-shape cages "cage"
    setup-my-cages
    setup-breeding-sites
    set my-id 4
    calculate-all-alleles
    visualize-genetics
    give-instructions
end



to set-scenario

   if scenario = "birds" [

    set-default-shape birds "bird"
    set-default-shape first-traits "bird-cap"
    set-default-shape second-traits "bird-wing"
    set-default-shape third-traits "bird-breast"
    set-default-shape forth-traits "bird-tail"

    set-default-shape cages "cage"
    set genes-to-phenotype
      [
        ["AA" "set color gray"] ["Aa" "set color gray"] ["aA" "set color gray"] ["aa" "set color sky"]        ;; sets crest colors
        ["BB" "set color gray"] ["Bb" "set color gray"] ["bB" "set color gray"] ["bb" "set color red"]        ;; sets wing colors
        ["CC" "set color gray"] ["Cc" "set color gray"] ["cC" "set color gray"] ["cc" "set color magenta"]    ;; sets breast colors
        ["DD" "set color gray"] ["Dd" "set color gray"] ["dD" "set color gray"] ["dd" "set color red" ]       ;; sets tail colors
      ]
      ]
   if scenario = "dragons" [

    set-default-shape birds "dragon-body"          ;; one shape for the dragon body
    set second-trait-shape-1 "dragon-breath-fire"          ;; three variations of shape for the breath trait
    set second-trait-shape-2 "dragon-breath-cloud"
    set second-trait-shape-3 "dragon-breath-ice"
    set-default-shape third-traits "dragon-chest"          ;; one shape for the chest trait
    set forth-shape-1 "dragon-tail-spade"                  ;; three variations of shape for the tail trait
    set forth-shape-2 "dragon-tail-rope-spade"
    set forth-shape-3 "dragon-tail-rope"

    set genes-to-phenotype
      [
        ["AA"  "set color (yellow - 1)"] ["Aa"  "set color orange"] ["aA"  "set color orange"] ["aa"  "set color red"]                                                       ;; sets body color
        ["BB"  "set shape second-trait-shape-3"] ["Bb"  "set shape second-trait-shape-2"] ["bB"  "set shape second-trait-shape-2"] ["bb"  "set shape second-trait-shape-1"]  ;; fire shape
        ["CC"  "set color (gray - 2.5)"] ["Cc"  "set color (gray + 1)"] ["cC"  "set color (gray + 1)"] ["cc"  "set color white"]                                             ;; sets breast color
        ["DD"  "set shape forth-shape-1"] ["Dd"  "set shape forth-shape-2"] ["dD"  "set shape forth-shape-2"] ["dd"  "set shape forth-shape-3" ]                             ;; sets tail shape
      ]
    ]
end



to give-instructions
    set instruction (instruction + 1)
    if instruction = 11 [set instruction 1]
    if instruction = 1 [
      output-print " "
      output-print " "

      output-print "You will be running a selective"
      output-print "breeding program, attempting to"
      output-print "develop a breed of fancy looking"
      output-print "birds."
      output-print "Press NEXT INSTRUCTION to continue."
    ]
    if instruction = 2 [
      output-print " "
      output-print " "

      output-print "There are 4 players in this"
      output-print "selective breeding program and "
      output-print "you are one of them."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
    if instruction = 3 [
      output-print " "
      output-print " "

      output-print "You are player 4, (blue).  You"
      output-print "start with 3 birds you own in"
      output-print "your six cages (at the bottom"
      output-print "of the world)."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
    if instruction = 4 [
      output-print " "
      output-print " "
      output-print "There are 6 breeding locations"
      output-print "(which are color coded) in the"
      output-print "middle of the world."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
     if instruction = 5 [
      output-print " "
      output-print " "

      output-print "If you move (using your mouse)"
      output-print "one male and one female bird"
      output-print "into a breeding location and"
      output-print "and press BREED-BIRDS, eggs"
      output-print "will hatch."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
    if instruction = 6 [
      output-print " "
      output-print " "

      output-print "You may drag the eggs back to"
      output-print "your cages to see what the birds"
      output-print "look like and keep them."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
    if instruction = 7 [
      output-print " "
      output-print ""
      output-print "To set a bird free (that you own),"
      output-print "click the mouse button and hold"
      output-print "and drag into the white space"
      output-print "the mouse button."
      output-print "Press NEXT INSTRUCTION to continue."
        ]
     if instruction = 8 [
      output-print " "
      output-print " "
      output-print " "

      output-print "You start with $500 and are"
      output-print (word "trying to earn at least $" reward-for-goal-bird ".")
      output-print "Press NEXT INSTRUCTION to continue."
            ]
     if instruction = 9 [
      output-print " "
      output-print " "

      output-print (word "Each breeding event cost $" cost-for-breeding ".")
      output-print "If you wish to breed your birds"
      output-print "with another player's you may "
      output-print "press REQUEST CONTRIBUTION BIRDS."
      output-print (word "This will cost $" cost-contribution " for each bird")
      output-print "contributed."
      output-print "Press NEXT INSTRUCTION to continue."

    ]
   if instruction = 10 [
      output-print " "
      output-print " "
      output-print " "
      output-print "Press GO if you are ready to begin."
      output-print "These instructions can be repeated"
      output-print "by pressing NEXT INSTRUCTION."
    ]
end


to setup-birds
       let this-cage owner
       sprout 1 [
         build-body-base
         set owned-by this-cage
         assign-all-genes
         build-body-parts
       ]
end


to setup-my-cages
   let these-cages nobody

   set these-cages patches with [pxcor = -4 and pycor <= 3 and pycor >= -2]
   ask these-cages
     [set pcolor user-1-color  set owner 1 sprout 1 [set breed cages set shape "cage"]]
   ask n-of 3 these-cages  [setup-birds]

   set these-cages patches with [pxcor <= 3 and pxcor >= -2 and pycor = 5]
   ask these-cages
     [set pcolor user-2-color set owner 2 sprout 1 [set breed cages set shape "cage"]]
   ask n-of 3 these-cages  [setup-birds]

   set these-cages patches with [pxcor = 5 and pycor >= -2 and pycor <= 3]
   ask these-cages
     [set pcolor user-3-color  set owner 3 sprout 1 [set breed cages set shape "cage"]]
   ask n-of 3 these-cages  [setup-birds]


   set these-cages patches with [pycor = -4 and pxcor >= -2 and pxcor <= 3]
   ask these-cages
     [set pcolor your-cage-color set owner 4 sprout 1 [set breed cages set shape "cage"]]
   ask n-of 3 these-cages  [setup-birds]

  ask patch 1 -3 [set plabel "These are your cages" set plabel-color black]
  ask patch 2 4 [set plabel "These are the six breeding sites          " set plabel-color black]
  ask patch -4 4 [set plabel "Player 1  " set plabel-color black]
  ask patch 5 4 [set plabel "Player 3  " set plabel-color black]
  ask patch -3 5 [set plabel "Player 2  " set plabel-color black]

end


to setup-breeding-sites
   ask patches with [pxcor >= -2 and pxcor <= -1 and pycor <= 3 and pycor >= 1]
     [set pcolor user-1-color   set site-id 1]
   ask patches with [pxcor >= 0 and pxcor <= 1 and pycor <= 3 and pycor >= 1]
     [set pcolor user-2-color  set site-id 2]
   ask patches with [pxcor >= 2 and pxcor <= 3 and pycor <= 3 and pycor >= 1]
     [set pcolor user-3-color  set site-id 3]

   ask patches with [pxcor >= -2 and pxcor <= -1 and pycor <= 0 and pycor >= -2]
     [set pcolor (your-cage-color - .2)  set site-id 4]
   ask patches with [pxcor >= 0 and pxcor <= 1 and pycor <= 0 and pycor >= -2]
     [set pcolor (your-cage-color + .2) set site-id 5]
   ask patches with [pxcor >= 2 and pxcor <= 3 and pycor <= 0 and pycor >= -2]
     [set pcolor (your-cage-color - .2) set site-id 6]
end



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  runtime procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to go
  listen-move-birds
  listen-free-birds
  hatch-eggs
  visualize-genetics
  calculate-all-alleles
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  check for meeting goals procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



to check-for-goal
  let winning-text ""
  let potential-winners birds with [my-id = owned-by and shape != "egg" and is-goal-bird?]
  let winner nobody

  if any? potential-winners    [
    set winner one-of potential-winners

    set current-funds (current-funds + reward-for-goal-bird)
    set winning-text "You bred the desired bird and received $1000 from a bird collector! "
    if current-funds > 500 [set winning-text (word winning-text " And you ended up with more money ($" current-funds ") than you started with!")]
    if current-funds < 500 [set winning-text (word winning-text " But you ended up with less money ($" current-funds ") than you started with!")]
    if current-funds <  0 [set winning-text (word winning-text " And actually are in debt at this point.")]

    ask winner [ask out-link-neighbors [die] die]
    user-message winning-text
  ]
    ;;[set winning-text "You have not bred the desired bird yet.  No one wants to buy any of the other birds you have."]

end



to visualize-genetics
  ask birds [
  set label-color black
    ifelse show-genetics?
       [
       set label (word first-genes " " second-genes " " forth-genes " "
          third-genes " " sex-gene) ]         ;; show the representation of the genes this bird has
       [set label owned-by]                   ;; show the player id that owns this bird
  ]
end


to contribute-birds
  ;; select birds from other players to put in breeding sites
  ;; (each other player will contribute one bird to one of three breeding sites)
  let open-sites patches with [not any? birds-here and site-id >= 1 and site-id <= 3]
  let target-site nobody
  let this-user-id 0
  let distance-to-target 0

  ask users [
     set current-funds (current-funds - cost-contribution)
     set this-user-id user-id
     set target-site one-of open-sites with [this-user-id = site-id and not any? users-here]
     ask one-of birds with [this-user-id = owned-by]
        [setxy ([pxcor] of target-site)  ([pycor] of target-site)]
     set moving-a-bird? true
  ]
end


to return-birds
  ;; return other players birds to their cages
   let target-cage nobody
   let this-owner 0
   ask birds with [owned-by != my-id and owned-by = site-id] [
     set this-owner owned-by
     set target-cage one-of patches with [owner = this-owner and not any? birds-here]
     setxy ([pxcor] of target-cage)  ([pycor] of target-cage)
   ]
end


to share-others-birds  ;; request for other players to share their birds for breeding involves two steps
 return-birds
 contribute-birds
end


to calculate-all-alleles
  ;; check to make sure one of each allele exists somewhere in the starting population
  ;; otherwise breeding for the target bird would be impossible
  set frequency-allele-dominant-first-trait (count birds with [(item 0 first-genes) = "A"]) + (count birds with [(item 0 first-genes) = "A"])
  set frequency-allele-recessive-first-trait (count birds with [(item 1 first-genes) = "a"]) + (count birds with [(item 1 first-genes) = "a"])
  set frequency-allele-dominant-second-trait (count birds with [(item 0 second-genes) = "B"]) + (count birds with [(item 0 second-genes) = "B"])
  set frequency-allele-recessive-second-trait (count birds with [(item 1 second-genes) = "b"]) + (count birds with [(item 1 second-genes) = "B"])
  set frequency-allele-dominant-third-trait (count birds with [(item 0 third-genes) = "C"]) + (count birds with [(item 0 third-genes) = "C"])
  set frequency-allele-recessive-third-trait (count birds with [(item 1 third-genes) = "c"]) + (count birds with [(item 1 third-genes) = "c"])
  set frequency-allele-dominant-forth-trait (count birds with [(item 0 forth-genes) = "D"]) + (count birds with [(item 0 forth-genes) = "D"])
  set frequency-allele-recessive-forth-trait (count birds with [(item 1 forth-genes) = "d"]) + (count birds with [(item 1 forth-genes) = "d"])
  ifelse (both-second-trait-alleles-exist? and both-first-trait-alleles-exist? and both-forth-alleles-exist? and both-third-trait-alleles-exist? and both-sexes-exist?)
   []
   [user-message (word "The current of birds in all the cages of all the player does not have"
    "enough genetic diversity for it to be possible for you to find a way to develop the desired breed."
    "  Press SETUP to start the model over and try again.")
    ]

end



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  breed birds ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to breed-birds
  ask turtles with [shape = "egg"] [die]
  ask birds with [is-female? and site-id != 0 and not selected? ] [
      set this-site-id site-id
      set parent-female self
     if (has-one-mate?) [ make-eggs ]
   ]
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  make and convert eggs;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to hatch-eggs
  ask birds with [shape = "egg" and in-cage? and not selected?] [
    build-body-base
    build-body-parts
  ]
end

to make-eggs
   set current-funds (current-funds - cost-for-breeding)
   let open-patches patches with [site-id = this-site-id and not any? birds-here]
   ask open-patches [sprout 1 [make-an-egg]]
end


to make-an-egg
  set breed birds
  assign-genetics-from-parents
  set shape "egg"
  set color blue
  set selected? false
  set size 0.5
  set owned-by my-id
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  build birds ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to build-body-base
  set breed birds
  if scenario = "birds" [ set shape "bird" ]
  if scenario = "dragons" [set shape "dragon-body"]
  set size bird-size
  set selected? false
  set color bird-body-color
end


to assign-all-genes
  set first-genes (word random-first-genes random-first-genes)
  set second-genes (word random-second-genes random-second-genes)
  set third-genes (word random-third-genes random-third-genes)
  set forth-genes (word random-forth-genes random-forth-genes)
  set sex-gene random-sex-genes
end


to build-body-parts
  let body-label label
  set label ""  ;; temporarily remove the label during building the body parts
  set size bird-size
 if scenario = "birds"
  [
    if is-male? [
      hatch 1 [run lookup-phenotype-for-gene first-genes set breed first-traits   create-link-from myself  [tie] ]
    ]
   hatch 1 [run lookup-phenotype-for-gene second-genes set breed second-traits  create-link-from myself  [tie] ]
   hatch 1 [run lookup-phenotype-for-gene forth-genes  set breed forth-traits   create-link-from myself  [tie] ]
   hatch 1 [run lookup-phenotype-for-gene third-genes  set breed third-traits   create-link-from myself  [tie] ]
  ]

 if scenario = "dragons"
  [
    run lookup-phenotype-for-gene first-genes

    hatch 1 [set breed second-traits run lookup-phenotype-for-gene second-genes   create-link-from myself  [tie] ]

    ifelse is-male?
        [hatch 1 [set breed first-traits set shape "dragon-wing-male" create-link-from myself  [tie]  ]]
        [hatch 1 [set breed first-traits set shape "dragon-wing-female" create-link-from myself  [tie]  ]]

    hatch 1 [set breed second-traits run lookup-phenotype-for-gene second-genes   create-link-from myself  [tie] ]
    hatch 1 [set breed third-traits run lookup-phenotype-for-gene third-genes    create-link-from myself  [tie] ]
    hatch 1 [set breed forth-traits run lookup-phenotype-for-gene forth-genes   create-link-from myself  [tie] ]
 ]
  set label body-label
end



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  move the birds ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




to listen-move-birds
 let snap-xcor mouse-xcor
 let snap-ycor mouse-ycor
 let birds-selected nobody
 let birds-available nobody


 if mouse-inside? [
    set birds-selected (birds with [selected?])
    ask birds-selected  [setxy snap-xcor snap-ycor]   ;; move previously selected birds to the mouse location
    if (mouse-down? and not any? birds-selected) [    ;; if the mouse is down and nothing was selected, then select a bird

       set birds-available birds with [pxcor = round snap-xcor and pycor = round snap-ycor and is-my-bird?]
       if any? birds-available [
        ask n-of 1 birds-available [ set selected? true ]
       ]
   ]

   if (count birds with [pxcor = round snap-xcor and pycor = round snap-ycor] = 1) [  ;; there is only one bird at this patch (the one being moved there)
       if (not mouse-down?) [         ;; bird is released to this patch if it owned by the user or is communal
           ask (birds-selected with [my-id = owner or owner = 0]) [set selected? false setxy pxcor pycor ]
       ]
   ]
  ]

end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  free the  birds or eggs  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to listen-free-birds
   ask birds with [owner = 0 and site-id = 0 and not selected?] [remove-bird]
end

to remove-bird
  ask other turtles-here [die]
  die
end

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;  reporters  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to assign-genetics-from-parents
  set first-genes inherited-first-genes
  set third-genes inherited-third-genes
  set second-genes inherited-second-genes
  set forth-genes inherited-forth-genes
  set sex-gene inherited-sex-genes
end

to-report inherited-first-genes
  let mother-loci (random 2)
  let father-loci (random 2)
  let mother-allele ""
  let father-allele ""
  ask parent-female [set mother-allele (item mother-loci first-genes)]
  ask parent-male [set father-allele  (item father-loci first-genes)]
  report (word mother-allele father-allele)
end

to-report inherited-third-genes
  let mother-loci (random 2)
  let father-loci (random 2)
  let mother-allele ""
  let father-allele ""
  ask parent-female [set mother-allele (item mother-loci third-genes)]
  ask parent-male [set father-allele  (item father-loci third-genes)]
  report (word mother-allele father-allele)
end

to-report inherited-second-genes
  let mother-loci (random 2)
  let father-loci (random 2)
  let mother-allele ""
  let father-allele ""
  ask parent-female [set mother-allele (item mother-loci second-genes)]
  ask parent-male [set father-allele  (item father-loci second-genes)]
  report (word mother-allele father-allele)
end


to-report inherited-forth-genes
  let mother-loci (random 2)
  let father-loci (random 2)
  let mother-allele ""
  let father-allele ""
  ask parent-female [set mother-allele (item mother-loci forth-genes)]
  ask parent-male [set father-allele  (item father-loci forth-genes)]
  report (word mother-allele father-allele)
end

to-report inherited-sex-genes
  let mother-loci (random 2)
  let father-loci (random 2)
  let mother-allele ""
  let father-allele ""
  ask parent-female [set mother-allele (item mother-loci sex-gene)]
  ask parent-male [set father-allele  (item father-loci sex-gene)]
  report (word mother-allele father-allele)
end


to-report random-first-genes
   ifelse random 2 = 0
     [report "A"]
     [report "a"]
end

to-report random-second-genes
   ifelse random 2 = 0
     [report "B"]
     [report "b"]
end

to-report random-third-genes
   ifelse random 2 = 0
     [report "C"]
     [report "c"]
end

to-report random-forth-genes
   ifelse random 2 = 0
     [report "D"]
     [report "d"]
end


to-report random-sex-genes
  ifelse random 2 = 0
    [report "WZ"]
    [report "ZZ"]
end

to-report is-male?
  ifelse sex-gene = "ZZ"
    [report true]
    [report false]
end

to-report is-female?
  ifelse sex-gene = "WZ"
    [report true]
    [report false]
end


to-report has-one-mate?
     let birds-at-site-id birds-on patches with [site-id = this-site-id]
     let report? false
     let eligible-males birds-at-site-id with [is-male?]
     if (count eligible-males = 1)  [
       set report? true
       set parent-male one-of eligible-males
     ]
     report report?
end

to-report in-cage?
  ifelse (owner != 0)
      [report true]
      [report false]
end

to-report both-sexes-exist?
  ifelse (any? birds with [is-male?] and any? birds with [is-female?])
    [report true]
    [report false]
end

to-report both-second-trait-alleles-exist?
  ifelse (frequency-allele-dominant-second-trait > 0 and frequency-allele-recessive-second-trait  > 0)
    [report true]
    [report false]
end

to-report both-third-trait-alleles-exist?
  ifelse (frequency-allele-dominant-third-trait > 0 and frequency-allele-recessive-third-trait  > 0)
    [report true]
    [report false]
end

to-report both-forth-alleles-exist?
  ifelse (frequency-allele-dominant-forth-trait > 0 and frequency-allele-recessive-forth-trait  > 0)
    [report true]
    [report false]
end

to-report both-first-trait-alleles-exist?
  ifelse (frequency-allele-dominant-first-trait > 0 and frequency-allele-recessive-first-trait  > 0)
    [report true]
    [report false]
end

to-report is-my-bird?
  ifelse my-id = owned-by [report true] [report false]
end


to-report is-goal-bird?
  ifelse ( first-genes = "aa" and second-genes = "bb" and third-genes = "cc" and forth-genes = "dd" )
    [report true]
    [report false]
end



to-report lookup-phenotype-for-gene [x]
  let item-counter 0
  let target-phenotype 0
  let target-item 0
  repeat length genes-to-phenotype [
    if (item 0 (item item-counter genes-to-phenotype)) = x
      [set target-phenotype (item 1 (item item-counter genes-to-phenotype))]
    set item-counter (item-counter + 1)
  ]
  set item-counter 0
  report target-phenotype
end
@#$#@#$#@
GRAPHICS-WINDOW
296
10
806
541
-1
-1
50.0
1
10
1
1
1
0
1
1
1
-4
5
-4
5
0
0
1
ticks

BUTTON
8
10
79
43
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
81
10
153
43
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

BUTTON
5
311
250
344
breed current birds at breeding sites
breed-birds
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SWITCH
6
203
150
236
show-genetics?
show-genetics?
1
1
-1000

BUTTON
6
383
250
416
remove all eggs from breeding sites
ask birds with [shape = \"egg\"] [die]
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
153
203
288
236
cost-for-breeding
cost-for-breeding
0
20
10
5
1
NIL
HORIZONTAL

SLIDER
4
274
189
307
reward-for-goal-bird
reward-for-goal-bird
200
1000
1000
100
1
NIL
HORIZONTAL

BUTTON
6
347
250
380
request contribution birds for breeding
share-others-birds
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
3
239
188
272
cost-contribution
cost-contribution
0
20
10
5
1
NIL
HORIZONTAL

MONITOR
195
242
288
287
your funds $
current-funds
3
1
11

OUTPUT
3
84
293
199
12

TEXTBOX
13
424
283
466
Bird Scenario Goal:  breed 3 birds each with a blue head cap, purple breast, red wing, and red tail
11
0.0
0

BUTTON
6
47
153
80
next instruction
give-instructions
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

CHOOSER
166
10
278
55
scenario
scenario
"birds" "dragons"
0

TEXTBOX
13
460
269
516
Dragon Scenario Goal:  breed 3 dragons each fire breathing, with a white breast, a red body, and a rope tail.
11
0.0
0

BUTTON
153
507
264
540
sell goal bird
\ncheck-for-goal
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

@#$#@#$#@
## WHAT IS IT?

This is a model of a selective breeding program of birds (or dragons).  In the scenario presented in the model the user assumes the role of a bird breeder, whose goal is breed a line of "fancy" looking birds or a specific type of dragon through managing a selective breeding program.

## HOW IT WORKS

In the "birds" scenario the user breeds have a simple genetic representation for five traits:  Crest color, wing color, breast color, tail color, and sex.

In the "birds" scenario these traits are represented with genes that have one of two possible alleles each (A and a, B and b, C and c, D and d, and W and Z, respectively).  Upper case letters represent dominant genes and lower case represent recessive genes.  Therefore the three combinations AA, Aa, and aA result in expression of the trait for A (e.g. gray crest), and only aa results in the expression of the trait for a (e.g. red crest).  Males and Females are determined by whether the bird has ZZ  (male) or WZ (female) genetic information.  One trait, crest color, is sex linked.  Male birds (ZZ) display a crest on their head, while females (WZ) do not (though they still carry the genetic information for how it should be expressed if they were male).

In the "dragons" scenario the user breeds have a simple genetic representation for four traits:  body color, breath type, breast color, tail shape, and sex.

In the "dragons" scenario these traits are represented with genes that have one of two possible alleles each (A and a, B and b, C and c, D and d, and W and Z, respectively). Females and males can be distinguished by the shape of their wings.  Upper case letters do not represent dominant genes in this scenario.  Gene expression follows rules for co-dominance, where both genes are expressed, resulting in a mixed or dual expression of the genes.  For example, A represents yellow coloring, a represents red coloring, so AA will be expressed as yellow, Aa will be expressed as orange, aA also will be expressed as orange, and aa will be expressed as red.

Here is the genotype to phenotype maps for both scenarios:

Bird Scenario:
Crest color:   (AA, Aa, aA) grey   or   (aa) blue
Wing color:
    (BB, Bb, bB) grey   or   (bb) red

Breast color:  (CC, Cc, cC) grey   or   (cc) purple
Tail color:
    (DD, Dd, dD) grey   or   (dd) red

Dragon Scenario:
Body color:
    (AA) yellow         or  (Aa, aA) orange           or  (aa)  red

Breath type:   (BB) frost breath   or  (Bb, bB) steam breath
     or  (bb) fire breath

Breast color:  (CC) black
          or  (Cc, cC) grey             or  (cc) white

Tail shape:
    (DD) spade          or  (Dd, dD) rope and spade   or  (dd) rope


## HOW TO USE IT

There are 4 players in this selective breeding scenario and you are one of them.  The other three are computer players.  Each of the computer players take a passive role in the breeding of birds, but serve as sources for out-breeding your own stock of birds.  You start with 3 birds you own in your six cages (at the bottom of the world).]  There are 6 breeding locations (color coded) in the middle of the world.  When move (using your mouse) one male and one female bird into a breeding location and press BREED-BIRDS, eggs will hatch.  You may drag the eggs back to your cages to see what the birds look like and keep them.   To set a bird free, just click drag it into the white space in the world and release the mouse button.  You can only set birds free that you own.

You start with $500 and are trying to earn at least the target $ reward for success (set with the slider REWARD-FOR-SUCCESS.  Each breeding event also has an assigned cost $ (COST-FOR-BREEDING).  If you wish to breed your birds with another player's, you may press the REQUEST CONTRIBUTION BIRDS button.  This will cost COST-CONTRIBUTION (set by this slider) for each bird contributed.

Initial settings (chooser):
- SCENARIO: chooser that determines whether you will be breeding "birds" or "dragons".

Buttons:
- SETUP:  Press this first to assign the SCENARIO you will be playing
- GO:
     Press this second to start the breeding challenge

- NEXT INSTRUCTION:  Use this to display a series of instructions about how to user the interface and mouse interactions with the birds.

- REQUEST CONTRIBUTION BIRDS FOR BREEDING:  When pressed one bird from each computer player is loaned for breeding to one of the top three breeding sites.
- BREED CURRENT BIRDS AT BREEDING SITE:  When pressed, all breeding sites that have at least one male and one female in them will produce a set of eggs in the remaining available spaces (up to four at that breeding site).  If more than one male or females are at that site, only one female and male will breed.
- REMOVE ALL EGGS FROM BREEDING SITES:  Removes all eggs currently in all breeding sites.
- SELL GOAL BIRD:  Attempts to sell the bird you have been trying to breed.  This will generate either a warning (if you don't have the bird yet in your cages) or a reward message if you do have the bird.  If you have the bird, the bird is removed from your cages and you will be given $, set by the REWARD-FOR-GOAL-BIRD slider.

Sliders:
- COST-BREEDING: cost in $ for every pair of birds that you breed.
- COST-CONTRIBUTION: cost in $ of pressing the REQUEST CONTRIBUTION BIRDS.
- REWARD-FOR-GOAL-BIRD: $ rewarded for selling one goal bird.

Switches:
- SHOW-GENETICS?: Show the Mendelian representation of the genes that each bird or egg has.

Monitors:
- Your funds $:  Shows the money you currently have in your bank account.

## THINGS TO NOTICE

Even though birds produce four eggs when they mate, the four eggs may or may not produce the expected probabilities of a theoretical Punnett square.  This is because, of course that expected probabilities, represent what would result after an infinite set of crosses.

## THINGS TO TRY

See if you can breed for the fancy bird in the least number of generations.

Write down the breeding plan you followed to create a line of the fancy bird.  Create a pedigree diagram to show the series of generations and breeding events that led to the fancy bird.

## EXTENDING THE MODEL

The model shows two different scenarios, "birds" and "dragons".  Other possible could be added such as virtual dogs, cats, corn, etc...

The model could be extended to a HubNet version, where all four players are active competitors in the selective breeding challenge.

## RELATED MODELS

Plant Hybridization model.

## CREDITS AND REFERENCES
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

bird
false
0
Polygon -7500403 true true 0 120 45 90 75 90 105 120 150 120 240 135 285 120 285 135 300 150 240 150 195 165 255 195 210 195 150 210 90 195 60 180 45 135
Circle -16777216 true false 38 98 14

bird side
false
0
Polygon -7500403 true true 0 120 45 90 75 90 105 120 150 120 240 135 285 120 285 135 300 150 240 150 195 165 255 195 210 195 150 210 90 195 60 180 45 135
Circle -16777216 true false 38 98 14

bird-all
false
0
Polygon -2674135 true false 45 90 15 60 45 75 30 45 60 75 60 30 75 75 90 45 90 75 120 60 90 90 135 90 90 105
Polygon -7500403 true true 0 120 45 90 75 90 105 120 150 120 240 135 285 120 285 135 300 150 240 150 195 165 255 195 210 195 150 210 90 195 60 180 45 135
Circle -16777216 true false 38 98 14
Polygon -2674135 true false 255 195 195 165 165 150 120 150 150 180 210 195
Polygon -2674135 true false 60 180 54 163 105 180 150 210 90 195
Polygon -2674135 true false 240 150 300 150 285 135 285 120 240 135 255 135

bird-breast
false
1
Polygon -2674135 true true 60 180 54 163 105 180 150 210 90 195

bird-cap
false
1
Polygon -2674135 true true 45 90 15 60 45 75 30 45 60 75 60 30 75 75 90 45 90 75 120 60 90 90 135 90 90 105

bird-cap-extreme
false
1
Polygon -2674135 true true 43 88 30 73 43 73 44 58 58 73 59 36 58 43 73 58 93 44 88 58 134 64 103 73 124 78 138 81 213 100 149 94 103 88 212 115 110 102 105 105 75 90

bird-tail
false
1
Polygon -2674135 true true 240 150 300 150 285 135 285 120 240 135 255 135

bird-tail-delta
false
1
Polygon -2674135 true true 240 150 300 150 285 135 285 120 240 135 255 135

bird-tail-frayed
false
1
Polygon -2674135 true true 240 135 255 75 258 112 283 80 270 120 293 107 275 135 297 142 270 150 300 165 270 165 290 207 236 151 255 135

bird-tail-split
false
1
Polygon -2674135 true true 240 135 300 60 270 135 300 210 240 150 255 135

bird-tails
false
1
Polygon -2674135 true true 240 150 300 150 285 135 285 120 240 135 255 135
Polygon -2674135 true true 240 135 285 90 270 135 300 195 240 150 255 135
Polygon -1184463 true false 240 135 255 75 258 112 283 80 270 120 293 107 275 135 297 142 270 150 300 165 270 165 290 207 236 151 255 135

bird-wing
false
1
Polygon -2674135 true true 255 195 195 165 165 150 120 150 150 180 210 195

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

cage
false
0
Rectangle -16777216 false false 0 0 300 300

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

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

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

dragon-all
false
0
Polygon -10899396 true false 105 150 75 135 60 165 45 150 45 165 30 150 15 165 0 135 30 90 45 75 75 75 105 105 120 150
Polygon -955883 true false 45 30 1 50 15 60 45 60 75 75 90 60 105 120 90 150 60 180 45 240 90 285 150 285 210 255 225 225 255 210 270 210 255 195 225 195 180 210 165 225 150 225 165 165 150 105 120 45 90 30 105 0 75 15 75 30 60 30 60 15 30 0
Circle -1 true false 30 15 30
Circle -16777216 true false 33 22 14
Circle -1 true false 45 15 30
Circle -16777216 true false 53 22 14
Line -1 false 49 240 124 240
Line -1 false 60 180 120 180
Line -1 false 75 270 120 270
Line -1 false 90 150 120 150
Line -1 false 105 120 120 120
Line -1 false 52 210 106 210
Polygon -10899396 true false 150 135 180 105 210 120 210 90 240 120 255 90 270 120 300 90 270 30 240 15 195 15 150 45 135 90
Polygon -10899396 true false 207 297 237 252 237 282 267 297
Polygon -10899396 true false 236 278 263 264 283 240 289 218 281 206 266 209 255 195 281 193 297 207 298 228 290 248 273 268 239 289
Polygon -10899396 true false 260 202 233 181 293 196 248 241

dragon-body
false
2
Polygon -955883 true true 45 30 1 50 15 60 45 60 75 75 90 60 105 120 90 150 60 180 45 240 90 285 150 285 210 255 225 225 255 210 270 210 255 195 225 195 180 210 165 225 150 225 165 165 150 105 120 45 90 30 105 0 75 15 75 30 60 30 60 15 30 0
Circle -1 true false 25 10 40
Circle -16777216 true false 30 17 24
Circle -1 true false 45 10 40
Circle -16777216 true false 56 17 24
Line -955883 true -60 255 -45 270
Line -955883 true -60 255 -45 270

dragon-breath-cloud
false
0
Circle -1 true false 28 58 32
Circle -1 true false -2 103 32
Circle -13345367 false false 28 58 32
Circle -13345367 false false -2 103 32
Circle -1 true false 15 75 58
Circle -13345367 false false 15 75 58
Circle -1 true false 0 120 58
Circle -13345367 false false 0 120 58
Polygon -1 true false 30 75 15 150 45 150 45 120 45 75

dragon-breath-fire
false
0
Polygon -2674135 true false 45 60 30 60 0 135 15 120 0 180 15 150 15 225 30 180 45 210 30 150 75 195 45 135 75 150 45 105
Polygon -1184463 true false 45 60 15 120 30 150 30 105 45 135 37 96

dragon-breath-ice
false
0
Polygon -11221820 true false 0 120 15 165 30 135 30 195 45 135 60 180 60 135 75 165 75 105 60 75 60 60 45 60 30 75 15 90
Polygon -13345367 false false 45 60 15 90 0 120 15 165 30 135 30 195 45 135 60 180 60 135 75 165 75 105 60 75 60 60

dragon-chest
false
13
Polygon -2064490 true true 105 90 120 120 120 150 120 180 105 210 120 240 120 270 150 285 90 285 45 240 60 180 90 150 105 120 96 91
Line -16777216 false 49 240 118 240
Line -16777216 false 60 180 120 180
Line -16777216 false 75 270 120 270
Line -16777216 false 90 150 120 150
Line -16777216 false 105 120 120 120
Line -16777216 false 52 210 106 210

dragon-eyes
false
0
Circle -1 true false 30 15 30
Circle -16777216 true false 33 22 14
Circle -1 true false 45 15 30
Circle -16777216 true false 53 22 14

dragon-tail-rope
false
0
Polygon -16777216 true false 240 285 263 264 283 240 289 218 281 206 266 209 255 195 281 193 297 207 298 228 290 248 273 268 239 289

dragon-tail-rope-spade
false
0
Polygon -16777216 true false 207 297 237 252 237 282 267 297
Polygon -16777216 true false 236 278 263 264 283 240 289 218 281 206 266 209 255 195 281 193 297 207 298 228 290 248 273 268 239 289

dragon-tail-spade
false
0
Polygon -16777216 true false 260 202 233 181 293 196 248 241

dragon-tails
false
0
Polygon -10899396 true false 207 297 237 252 237 282 267 297
Polygon -10899396 true false 236 278 263 264 283 240 289 218 281 206 266 209 255 195 281 193 297 207 298 228 290 248 273 268 239 289
Polygon -10899396 true false 260 202 233 181 293 196 248 241

dragon-wing-female
false
8
Polygon -10899396 true false 152 146 176 95 202 70 239 58 276 68 296 99 300 159 294 197 270 156 241 140 205 136 168 151 153 170
Polygon -10899396 true false 118 146 94 95 68 70 31 58 -6 68 -26 99 -30 159 -24 197 0 156 29 140 65 136 102 151 117 170

dragon-wing-male
false
9
Polygon -10899396 true false 107 169 119 147 96 112 87 72 95 36 60 60 15 75 -15 60 -25 25 -36 86 -38 152 -33 209 -9 243 -22 196 -14 167 3 147 24 153 36 174 52 154 66 154 74 172 74 182 93 163 103 164
Polygon -10899396 true false 148 169 136 147 159 112 168 72 160 36 195 60 240 75 270 60 280 25 291 86 293 152 288 209 264 243 277 196 269 167 252 147 231 153 219 174 203 154 189 154 181 172 181 182 162 163 152 164

egg
false
0
Circle -7500403 true true 96 76 108
Circle -7500403 true true 72 104 156
Polygon -7500403 true true 221 149 195 101 106 99 80 148

empty
true
0

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
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
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

rooster-all
false
0
Polygon -955883 true false 45 30 15 45 45 60 60 165 135 240 150 240 180 210 270 180 240 135 135 120 105 75 90 45 60 30
Circle -16777216 true false 38 38 14
Polygon -2674135 true false 240 180 195 165 165 150 120 150 150 180 210 195
Polygon -2674135 true false 45 30 30 0 45 15 45 0 60 15 75 0 75 15 90 0 73 39
Polygon -2674135 true false 31 50 25 75 33 99 49 78 46 61
Polygon -2674135 true false 225 135 240 75 270 30 300 30 285 45 300 45 285 60 285 90 300 75 285 105 300 105 285 120 300 135 285 150 285 165 270 165 270 180 255 180 240 150
Line -1184463 false 135 240 120 285
Line -1184463 false 150 240 145 270
Line -1184463 false 120 285 75 285
Line -1184463 false 120 285 105 300
Line -1184463 false 120 285 150 300
Line -1184463 false 146 269 171 277
Line -1184463 false 145 270 130 270

site
false
0
Rectangle -7500403 true true 0 0 300 300

site-ne
false
0
Rectangle -7500403 true true 0 0 300 300
Line -16777216 false 300 300 300 0
Line -16777216 false 0 0 300 0

site-new
false
0
Rectangle -7500403 true true 0 0 300 300

site-nw
false
0
Rectangle -7500403 true true 0 0 300 300
Line -16777216 false 0 300 0 0
Line -16777216 false 0 0 300 0

site-se
false
0
Rectangle -7500403 true true 0 0 300 300
Line -16777216 false 300 300 300 0
Line -16777216 false -15 300 285 300

site-sw
false
0
Rectangle -7500403 true true 0 0 300 300
Line -16777216 false 0 300 0 0
Line -16777216 false 0 300 300 300

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

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

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

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0beta1
@#$#@#$#@
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
0
@#$#@#$#@
