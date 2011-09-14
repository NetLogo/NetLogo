;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;    The hard coded sample questions are the first section of code
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Variable and Breed declarations ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

globals
[
  current-question  ;; index of the current question in the question list
  question-list     ;; list of all the questions.  Each question is itself a list.

  steps-locked?     ;; are the clients locked to the host's question?
  voted?-color      ;; color of turtles which have voted
  not-voted?-color  ;; color of turtles which haven't voted

  activity          ;; how many votes have been cast since last autosave?
  auto-saving?      ;; autosave poll data?
  auto-save-file    ;; base file name for the autosave files
  auto-save-directory ;; directory for autosave files
  auto-saving-web?
  auto-save-web     ;; full directory information for auto-saving polls as HTML

  web-response-file
  web-responding?
  web-response-data
  web-response-how-many?
  web-processing?
  web-questions

  names-showing?    ;; are client names being shown in the view?
  graphics-display  ;; what mode is the graphics display in?

  plot-showing?     ;; are the votes being shown in the plot?
  plot-dirty?       ;; does the plot need a redraw?
  plot-first        ;; first item included in the special mode data plot
  plot-last         ;; last item included in the special mode data plot
  plot-mode-is?     ;; what plot mode is the model in?

  sort-up           ;; used to sort turtles up / down according to if the word is in their string response
  sort-right        ;; used to sort turtles right / left according to if the word is in their string response

  save-state-var
  save-state-value

  tmp1
  tmp2
  tmp3
  tmp4

]

breed [ poll-clients poll-client ]
breed [ non-clients non-client ]

turtles-own
[
  user-id       ;; unique id, input by the client when they log in, to identify each student turtle
  slider-value  ;; the value of the client's choice slider
  my-choices    ;; list of my choices for each question
  my-current-question   ;; which question is the student currently on?
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Sample Questions Section
;;;;;;     at the beginning to make them easy to find and edit
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to load-sample-questions
    set question-list
    [
      ["Sample Question 1" "Poller"]
      ["Sample Question 2" "Web Text"]
      ["Sample Question 3" "Likert" 5]
      ["Sample Question 4" "Web Number"]
    ]
    set current-question 0
    ask turtles [clear-my-data  set-my-current-question 0]
end






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;         Setup Functions
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;  Startup ONLY runs when the model is first loaded.  It initializes the
;;  model's network capabilities and clears everything.
to startup
  setup
end

;; give the user some information about what the setup button does so they can
;; know whether they want to proceed before actually doing the setup
;; If you are tired of this prompt, change the code in the SETUP button from
;; setup-prompt to setup.
to setup-prompt
  if user-yes-or-no? (word "The SETUP button should only be used when starting "
              "over with a new group and new questions (such as a"
              "different course) since ALL data is lost.\n"
              "Are you sure you want to SETUP?")
  [ setup ]
end

;; Initializes the display and variables.
;; Also loads the data lists, if so desired.
;; Only called by SETUP-PROMPT, which is called by the setup button, and STARTUP.
to setup
  hubnet-reset
  ca
  clear-output
  setup-vars
  load-questions-prompt
;  load-sample-questions ;this is here to make life easier on Walter
end

to walkthrough
  startup-walkthrough
  setup-vars-walkthrough
end

to startup-walkthrough
    if user-yes-or-no? "Would you like to load questions?"
    [load-questions-prompt]
    ifelse user-yes-or-no? "Would you like to AUTOSAVE world data?"
    [auto-save-prompt]
    [set auto-save? false]
end

to setup-vars-walkthrough
    ifelse user-yes-or-no? "Allow students to change their responses?"
    [  set allow-change? true]
    [  set allow-change? false]
    ifelse user-yes-or-no? "Plot student responses?"
    [  set show-data? true   set plot-showing? show-data?]
    [  set show-data? false  set plot-showing? show-data?]
    ifelse user-yes-or-no? "Allow students to see questions at their own pace?"
    [  set lock-step? false  set steps-locked? false]
    [  set lock-step? true  set steps-locked? true]
    ifelse user-yes-or-no? "See student names?"
    [  set see-names? true]
    [  set see-names? false]
end

;; initialize global variables
to setup-vars
  set not-voted?-color green - 2
  set voted?-color red + 2
  set plot-showing? show-data?
  set plot-dirty? true
  set steps-locked? lock-step?

  set activity 0
  set auto-saving? false
  set auto-save-file ""
  set auto-save-directory ""
  set auto-saving-web? false
  set auto-save-web ""

  set names-showing? see-names?
  set graphics-display Turtle-display
  set plot-mode "Histogram"
  set plot-mode-is? plot-mode
  set sort-up ""
  set sort-right ""

  set web-response-file ""
  set web-responding? false

  clear-all-data-and-questions
end









;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;        Clearing Data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; give the user some information about what the clear clients button does so they can
;; know whether they want to proceed before actually doing the setup
;; If you are tired of this prompt, change the code in the CLEAR CLIENTS button from
;; clear-clients-prompt to clear-clients.
to clear-clients-prompt
  if user-yes-or-no? (word "The CLEAR CLIENTS button should only be used when starting "
              "over with a new group (such as a new set of students) since "
              "all STUDENT data is lost, but questions are saved.\n"
              "Are you sure you want to CLEAR CLIENTS?")
  [ clear-clients ]
end

;;  Clears the plots, clears the command center, and kills all turtles and patches.
;;  Clients are dead in the water - they must exit and relogin.
to clear-clients
  clear-plot
  cp ct
  clear-output
end


;; give the user some information about what the clear questions button does so they can
;; know whether they want to proceed before actually doing the clear questions
;; If you are tired of this prompt, change the code in the CLEAR QUESTIONS button from
;; clear-all-data-and-questions-prompt to clear-all-data-and-questions-clients.
to clear-all-data-and-questions-prompt
  if user-yes-or-no? (word "The CLEAR QUESTIONS button should only be used when starting "
              "over with the same students but new questions since "
              "all question and poll data is lost, but clients are saved.\n"
              "Are you sure you want to CLEAR QUESTIONS?")
  [ clear-all-data-and-questions ]
end

to clear-all-data-and-questions
  clear-plot
  hubnet-broadcast "Question" ""
  hubnet-broadcast "Question Two" ""
  hubnet-broadcast "Question Three" ""
  hubnet-broadcast "Question Number" ""
  hubnet-broadcast "Current Choice" ""
  hubnet-broadcast "Typed-Response" "Click on the CHANGE button\n(in the top right corner of this widget)\nto type a response."
  set activity 0
  set current-question 0
  set question-list []
  set sort-up ""
  set sort-right ""
  ask turtles [ clear-my-data ]
end

to clear-my-data  ;; turtle procedure
  set color not-voted?-color
  set my-choices []
  repeat length question-list
  [ set my-choices lput false my-choices ]
  ifelse length question-list = 0
  [  set my-current-question -1]
  [  set my-current-question 0]
end


;; give the user some information about what the clear current data button does so they can
;; know whether they want to proceed before actually doing the clear current data
;; If you are tired of this prompt, change the code in the CLEAR CURRENT DATA button from
;; clear-current-data-prompt to clear-current-data.
to clear-current-data-prompt
  if user-yes-or-no? (word "The CLEAR CURRENT DATA button should only be used when clearing "
              "the responses to the question that the TEACHER is currently viewing. "
              "Student responses to this question will be lost, but the question"
              "itself and student logins are unchanged.\n"
              "Are you sure you want to CLEAR CURRENT DATA?")
  [ clear-current-data ]
end

to clear-current-data
  clear-plot
  ask turtles
  [
    set my-choices replace-item current-question my-choices false
    if my-current-question = -1 [set-my-current-question 0]
  ]
  set-current-question current-question
end








;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;        Runtime Procedures
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to go
  lock-step-listener
  see-names-listener
  see-typed-listener

  ;;  Anything from the clients?
  if length question-list > 0 [listen-clients]

  plot-mode-listener

  every 0.5
  [ ;;  The turtles do whatever MOVEMENTS they're gonna do.
    turtle-movement-listener
    show-data-listener
  ]

  auto-save-listener
  if auto-saving? or auto-saving-web?
  [  if activity >= auto-save-every
     [  auto-save ]
  ]

  display
end

to lock-step-listener
  ;;check to see if lock-step? status has changed.  If it has changed to TRUE, then adjust clients.
  if lock-step? = true and steps-locked? = false
  [    set steps-locked? true
      ask turtles [ set-my-current-question current-question ]
  ]
  if lock-step? = false and steps-locked? = true
  [  set steps-locked? false
  ]
end

to see-names-listener
  ifelse see-names?
  [  if not names-showing?
     [  ask turtles [ set label word user-id "   "]  set names-showing? see-names? ]
  ]
  [  if names-showing?
     [  ask turtles [ set label ""]  set names-showing? see-names? ]
  ]
end

to see-typed-listener
  let show-teacher ""
  if mouse-down?
  [ wait .35
    ;this wait is necessary to keep the popup from interrupting the mouse-down? reporter,
    ;which would prevent it from resetting to false
    if any? turtles-on patch mouse-xcor mouse-ycor
    [ ask one-of turtles-on patch mouse-xcor mouse-ycor
      [ set show-teacher item current-question my-choices ]
      user-message show-teacher
    ]
    wait .25
  ]
end

to plot-mode-listener
    if plot-mode-is? != plot-mode
    [  if plot-mode = "Average" [ plot-average-prompt ]
       set plot-mode-is? plot-mode
       set plot-dirty? true
    ]
end

to show-data-listener
    ;;  Check to see if the poll data is to be displayed.
    ifelse show-data?
    [ if not plot-showing?
      [ set plot-showing? true
        set plot-dirty? true
      ]
      if plot-dirty?
      [ set plot-dirty? false
        do-plot
      ]
    ]
    [ if plot-showing?
      [ set plot-showing?
        false clear-plot
      ]
    ]
end

to turtle-movement-listener
  if not (graphics-display = Turtle-display)
  [ set graphics-display Turtle-display
    if graphics-display = "Word-Sort"
    [ if sort-up = "" [set sort-up user-input "Sort up / down word -"]
      if sort-right = "" [set sort-right user-input "Sort right / left word -"]
    ]
  ]
    if graphics-display = "Sit" [ ask turtles [sit] ]
    if graphics-display = "Wander" [ ask turtles [wander] ]
    if graphics-display = "Line-Up" [ ask turtles [line-up] ]
    if graphics-display = "Word-Sort" [ ask turtles [word-sort] ]
end

to auto-save-listener
  if auto-save? and not auto-saving?
  [  set auto-saving? auto-save?
     auto-save-prompt
  ]
  if auto-saving? and not auto-save?
  [  ifelse user-yes-or-no? "Do you really want to turn off auto-save world data?"
     [  set auto-saving? false]
     [  set auto-save? true ]
  ]

end

to save-button-prompt
  let option user-one-of (word "save QUESTIONS\n"
    "save all responses to a WEB page\n"
    "save INDividual responses to separate web pages\n"
    "save ALL data to a file\n"
    "auto-save NOW\n"
    "CANCEL" ) ["QUESTIONS" "ALL" "NOW" "CANCEL"]
  if option = "QUESTIONS" [save-questions-prompt]
  if option = "ALL" [save-world-prompt]
  if option = "NOW" [auto-save-now]
end









;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;   Turtle Movement procedures
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to sit
  ifelse color = voted?-color
  [  while [count turtles-here > 1 or xcor <= 0]
     [ setxy random-xcor random-ycor ]
  ]
  [  while [count turtles-here > 1 or xcor >= 0]
     [ setxy random-float min-pxcor random-ycor ]
  ]
end

to wander  ;; turtle procedure
  rt 90 * random-float 4
  ifelse color = voted?-color
  [  if xcor <= 0 [ set heading 90 ]
  ]
  [  if xcor >= 0 [ set heading 270]
  ]
  if patch-ahead 1 != nobody and not any? turtles-on patch-ahead 1
  [ fd 1 ]
end

to line-up
  set heading 0
  ifelse is-number? item current-question my-choices
  [ ifelse min [item current-question my-choices] of turtles < 0 or max [item current-question my-choices] of turtles > 10
    [ set xcor ((1 + item current-question my-choices - min [item current-question my-choices] of turtles) * world-width / (1 + max [item current-question my-choices] of turtles - min [item current-question my-choices] of turtles) + min-pxcor)]
    [ set xcor ((1 + item current-question my-choices) * world-width / 12 + min-pxcor)]
  ]
  [ set xcor -1 * max-pxcor ]
  if count turtles-here > 1  [ jump random-float 4 ]
  if ((count turtles-at 0 -1 = 0) and (ycor > -1 * max-pycor)) [back 1]
end

to word-sort
  ifelse not is-string? item current-question my-choices
  [  while [(count turtles-here > 1) or not (abs xcor <= 3 and abs ycor <= 3) ]
     [setxy random-float 7 - 3 random-float 7 - 3]
  ]
  [  if not (sort-up = "")
     [ ifelse member? sort-up (item current-question my-choices)
       [ while [(count turtles-here > 1) or (ycor <= 0) or (abs xcor <= 3 and ycor >= 0)]
         [ set ycor (random-float max-pycor) + 1
           set xcor xcor - 1 + random-float 3
         ]
       ]
       [ while [(count turtles-here > 1) or (ycor >= 0) or (abs xcor <= 3 and ycor <= 0)]
         [ set ycor (random-float max-pycor) + min-pycor - 1
           set xcor xcor - 1 + random-float 3
         ]
       ]
     ]
     if not (sort-right = "")
     [ ifelse member? sort-right item current-question my-choices
       [ while [(count turtles-here > 1) or (xcor <= 0) or (xcor >= 0 and abs ycor <= 3)]
         [ set xcor (random-float max-pxcor) + 1
           set ycor ycor - 1 + random-float 3
         ]
       ]
       [ while [(count turtles-here > 1) or (xcor >= 0) or (xcor <= 0 and abs ycor <= 3)]
         [ set xcor (random-float max-pxcor) + min-pxcor - 1
           set ycor ycor - 1 + random-float 3
         ]
       ]
     ]
  ]
end










;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;   Client Message Processing Procedures
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ execute-create ]
    [
      ifelse hubnet-exit-message?
      [
        ask turtles with [user-id = hubnet-message-source] [ die ]
        set plot-dirty? true
      ]
      [
        ;;  If the client hits the Choose button, then run this code.
        if hubnet-message-tag = "Choose"
        [ execute-choose ]
        ;;  Whenever the client adjusts the slider Choice, run this code.
        if hubnet-message-tag = "Choice"
        [ change-choice ]
        if hubnet-message-tag = "Typed-Response"
        [ execute-type ]
        ;;  If the clients can view questions as they like, then run this code.
        if steps-locked? = false
        [
          ;;  When the client hits the PREV button
          if hubnet-message-tag = "<<PREV"
          [ execute-prev ]
          ;;  When the client hits the NEXT button
          if hubnet-message-tag = "NEXT>>"
          [ execute-next ]
        ]
      ]
    ]
  ]
end

to execute-create
  create-poll-clients 1
  [
    let pos one-of patches with [not any? turtles-here]
    ifelse pos != nobody
    [ move-to pos ]
    [ user-message "Too many students. Make a bigger view." ]
    set user-id hubnet-message-source
    if names-showing? [ set label word user-id "   "]
    set slider-value 25
    clear-my-data
    if any? non-clients with [user-id = hubnet-message-source]
    [ set my-choices [my-choices] of one-of non-clients with [user-id = hubnet-message-source]]
    ask non-clients with [user-id = hubnet-message-source] [die]
    if steps-locked? = true
    [  set my-current-question current-question]
    set-my-current-question my-current-question
  ]
end

to execute-choose
  ask poll-clients with [user-id = hubnet-message-source]
  [
    if (allow-change? or color = not-voted?-color)
    [
      if my-current-question = current-question [set color voted?-color]
      set my-choices replace-item my-current-question my-choices slider-value
      hubnet-send hubnet-message-source "Current Choice" slider-value
      set activity activity + 1
    ]
  ]
  if plot-showing?
  [ set plot-dirty? true ]
end

to execute-type
  ask poll-clients with [user-id = hubnet-message-source]
  [
    if (allow-change? or color = not-voted?-color)
    [
      if my-current-question = current-question [set color voted?-color]
      set my-choices replace-item my-current-question my-choices safer-read-from-string hubnet-message
      set activity activity + 1
    ]
  ]
  if plot-showing?
  [ set plot-dirty? true ]
end

;;  Everytime you change the slider "Choice"
;;  it changes the value of slider-value for the associated turtle.
to change-choice
  ask poll-clients with [user-id = hubnet-message-source]
  [ set slider-value hubnet-message ]
end

to execute-prev
  ask poll-clients with [user-id = hubnet-message-source]
  [  if my-current-question > 0
    [ set-my-current-question my-current-question - 1]
  ]
end

to execute-next
  ask poll-clients with [user-id = hubnet-message-source]
  [  if my-current-question + 1 < length question-list
    [ set-my-current-question my-current-question + 1]
  ]
end

to update-my-choice-list
    while [length my-choices < length question-list]    ;;be sure that my-choices has enough spots
    [ set my-choices lput false my-choices ]
end

to set-my-current-question [n]
  if n >= 0 and n < length question-list    ;;check to be sure that the question number is valid
  [
    update-my-choice-list

    set my-current-question n               ;;if it is, adjust the number the turtle has

    if breed = poll-clients
    [ hubnet-send user-id "Question Number" my-current-question
      send-question (item 0 item my-current-question question-list)

      ifelse (item my-current-question my-choices) = false    ;;display the current choice on the client
      [  hubnet-send user-id "Current Choice" ""
         hubnet-send user-id "Typed-Response" "Click on the CHANGE button\n(in the top right corner of this widget)\nto type a response."
      ]
      [  if is-number? item my-current-question my-choices
         [ hubnet-send user-id "Current Choice" (item my-current-question my-choices)
           hubnet-send user-id "Typed-Response" "Click on the CHANGE button\n(in the top right corner of this widget)\nto type a response."
         ]
         if is-string? item my-current-question my-choices
         [ hubnet-send user-id "Current Choice" ""
           hubnet-send user-id "Typed-Response" (item my-current-question my-choices)
         ]
      ]
    ]
  ]
end

to send-question [question]
    ifelse length question <= 80
    [  hubnet-send user-id "Question" question
       hubnet-send user-id "Question Two" ""
       hubnet-send user-id "Question Three" ""
    ]
    [  ifelse length question > 80 and length question <= 160
       [  hubnet-send user-id "Question" substring question 0 80
          hubnet-send user-id "Question Two" substring question 80 (length question)
          hubnet-send user-id "Question Three" ""
       ]
       [  hubnet-send user-id "Question" substring question 0 80
          hubnet-send user-id "Question Two" substring question 80 160
          hubnet-send user-id "Question Three" substring question 160 (length question)
       ]
    ]
end









;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;   Host Questions
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to-report build-question [what-type?]
  ifelse what-type? = "Likert"
  [ report (sentence (user-input "Input new question?") (list "Likert" read-from-string user-input "How many items?"))]
  [ report sentence user-input "Input new question?" what-type? ]
end

to add-question [what-type?]
  ;;  If the question list is empty as indicated with "", then set to empty list.
  ;;  Add the typed question to question-list
  ;;  Set the host viewed question to be the one just entered.  Is this necessary?
  set question-list lput (build-question what-type?) question-list
  set-current-question (length question-list - 1)
  clear-current-data
end

to set-current-question [n]
  if n >= 0 and n < length question-list
  [
    set current-question n
    ask turtles
    [
      ;;  If all the clients are supposed to be on the same question,
      ;;  then they switch to the host's question.
      ;;  If not, they only check to be sure that
      ifelse steps-locked? = true
      [  set-my-current-question current-question]
      [  update-my-choice-list ]

      ifelse (item current-question my-choices) = false
      [  set color not-voted?-color      ]
      [  set color voted?-color      ]
    ]

    if plot-showing?
    [ set plot-dirty? true ]

  ]
end

to prev-question
  if current-question > 0
  [ set-current-question current-question - 1]
end

to next-question
  if current-question + 1 < length question-list
  [ set-current-question current-question + 1]
end

to edit-question
    let option user-one-of (word "REPLACE - replace this question with a new one.  This will clear the poll results for the question\n"
      "INSERT  - insert a new question and move the current question one position later in the list\n"
      "DELETE  - delete this question and it's poll results\n")
      ["REPLACE" "INSERT" "DELETE" "CANCEL"]
      if option = "REPLACE" [replace-question]
      if option = "INSERT" [insert-question]
      if option = "DELETE" [delete-question]
end

to-report choose-type
  let option user-one-of (word "What type of question?\n"
    "POLLER \n"
    "web TEXT \n"
    "LIKERT scale \n"
    "web NUMBER \n")
    ["POLLER" "TEXT" "LIKERT" "NUMBER"]
  if option = "POLLER" [report "Poller"]
  if option = "TEXT" [report "Web Text"]
  if option = "LIKERT" [report "Likert"]
  if option = "NUMBER" [report "Web Number"]
end

to replace-question
  set question-list replace-item current-question question-list build-question choose-type
  clear-current-data
  ask turtles [set-my-current-question my-current-question]
end

to insert-question
  set question-list (sentence (lput (build-question choose-type) (items 0 current-question question-list)) (items current-question length question-list question-list))
  ask turtles
  [  set my-choices sentence (sentence items 0 current-question my-choices false) items current-question length my-choices my-choices
     if my-current-question >= current-question and steps-locked? = false [set my-current-question my-current-question + 1]
  ]
  clear-current-data
  ask turtles [set-my-current-question my-current-question]
end

to delete-question
  set question-list sentence (items 0 current-question question-list) (items (current-question + 1) (length question-list) question-list)
  ask turtles
  [  set my-choices sentence (items 0 current-question my-choices) (items (current-question + 1) (length my-choices) my-choices)
  ]
  if current-question = length question-list [set current-question (length question-list - 1)]
  ask turtles [ if my-current-question > current-question [set my-current-question (my-current-question - 1)]]
  set-current-question current-question
  ask turtles [set-my-current-question my-current-question]
  if length question-list = 0 [ clear-all-data-and-questions]
end

;; reports items x through y - 1 of the list somelist
;; if x and y are the same, it reports an empty list
to-report items [x y somelist]
    let n 0
    let buildlist []
    repeat (y - x)
    [  set buildlist lput (item (x + n) somelist) buildlist
       set n n + 1
    ]
    report buildlist
end















;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Plotting Procedures
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to do-plot
  if plot-mode-is? = "Histogram" [do-one-item-histogram]
  if plot-mode-is? = "Average" [do-multi-item-average]
end

to do-one-item-histogram
  clear-plot
  if length filter [is-number? ?] [item current-question my-choices] of turtles > 0
  [ if max [item current-question my-choices] of turtles > 10
    [ set-plot-x-range plot-x-min (ceiling max [item current-question my-choices] of turtles + 1) ]
    if min [item current-question my-choices] of turtles < 0
    [ set-plot-x-range (floor min [item current-question my-choices] of turtles) plot-x-max]
    let current-data map [item current-question [my-choices] of ? ] sort turtles
    if not empty? remove false current-data
    [
      set-current-plot-pen "data"
      histogram current-data
  ;;    set-current-plot-pen "mean"
  ;;    plot-vline mean current-data
  ;;    set-current-plot-pen "median"
  ;;    plot-vline median current-data
  ;;    set-current-plot-pen "mode"
  ;;    plot-modes current-data
    ]
  ]
end

to plot-vline [value]
  plotxy value 0
  plotxy value plot-y-max
end

to plot-modes [lst]
  set lst remove false lst
  let values remove-duplicates lst
  ifelse length values = length lst
  [ histogram lst ]  ;; no duplicates so all items are modes
  [
    let counts []
    let i 0
    repeat length values
    [
      set counts lput ((length lst) - (length remove (item i values) lst)) counts
      set i i + 1
    ]
    let n max counts  ;; how many votes for the most frequent choice(s)?
    while [member? n counts]
    [
      set values (replace-item (position n counts) values false)
      set counts (replace-item (position n counts) counts 0)
    ]
    set values remove false values
    set i 0
    repeat length values
    [
      set lst (remove (item i values) lst)
      set i i + 1
    ]
    histogram lst
  ]
end

to plot-average-prompt
    let startitem read-from-string user-input "First Question Number?"
    let enditem read-from-string user-input "Last Question Number?"
    if startitem >= 0 and enditem >= startitem and enditem < length question-list
    [ set plot-first startitem
      set plot-last enditem
      set show-data? true
    ]
    if startitem > enditem or enditem >= length question-list
    [ set plot-mode plot-mode-is?]
end

to do-multi-item-average
    clear-plot
    set-plot-x-range (plot-first) (plot-last + 1)
    set-plot-y-range 0 10
    let index plot-first
    while [index <= plot-last]
    [  if count turtles with [is-number? item index my-choices] > 0
       [  plotxy index ((sum [item index my-choices] of turtles with [is-number? item index my-choices]) / (count turtles with [is-number? item index my-choices]))]
       set index index + 1
    ]
end










;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Saving Questions
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;Saving questions
;ALWAYS makes a web page.  Poller data is hidden in comments at the bottom.
to save-questions-prompt
  user-message "Select the directory and filename for your questions.  You should add an extension - either .htm or .html\nIf you don't add an extension, NetLogo will automatically add .html"
  let option user-new-file
  ifelse is-string? option
  [ if not (substring option (length option - 5) (length option) = ".html")
    [ if not (substring option (length option - 4) (length option) = ".htm")
      [ set option word option ".html"
      ]
    ]
    save-questions-page option
  ]
  [ user-message "Save has been cancelled."
  ]
end

to save-questions-page [web-page]

   if file-exists? web-page
   [ file-delete web-page
   ]

   file-open web-page
   file-print "<HTML>"
   file-print "<head>"
   file-print (word "<title>Poller Questions " (substring remove ":" date-and-time 0 6) "</title>")
   file-print "</head>"
   file-print "<body>"
   file-print "<center>"
   file-print "<P><FORM ACTION=\".FormSaver\" METHOD=POST>"
   file-print "<P><INPUT TYPE=hidden NAME=command VALUE=save></P>"
   file-print "<P><B>User name (identical to NetLogo username)</B>"
   file-print "<p><INPUT TYPE=text NAME=Field1 VALUE=\"\">"        ;this will later be read as item 4 in the list, when responses are read in from the web
   let counter-q 0
   let counter-f 2
   repeat length question-list
   [
     file-print (word "<h2> " item 0 item counter-q question-list " </h2>")
     if item 1 item counter-q question-list = "Web Text"
     [ file-print (word "<P><TEXTAREA NAME=Field" counter-f " ROWS=8 COLS=81 WRAP=virtual></TEXTAREA>")
       set counter-f counter-f + 1
     ]
     if item 1 item counter-q question-list = "Likert"
     [ let counter-l 0
       file-print "<p>"
       repeat item 2 item counter-q question-list
       [ file-print (word "<INPUT TYPE=radio NAME=Field" counter-f " VALUE=\"" counter-l "\">" counter-l)
         set counter-l counter-l + 1
       ]
       set counter-f counter-f + 1
     ]
     if item 1 item counter-q question-list = "Web Number"
     [ file-print (word "<P><input type=TEXT NAME=Field" counter-f " VALUE=\"\">")
       set counter-f counter-f + 1
     ]
     file-print "<p>"
     set counter-q counter-q + 1
   ]

   file-print (word "<INPUT TYPE=submit NAME=Field" counter-f " VALUE=\"Submit\">&lt;---------&gt;<INPUT TYPE=reset VALUE=\"Reset\">")
   file-print "</center>"
   file-print "</body>"
   file-print "</html>"

   file-print "<!-- NETLOGO POLLER QUESTION LIST"
   file-write question-list
   file-print ""
   file-print " POLLER QUESTION LIST END -->"

   file-close
 end













;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Loading Questions
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to load-questions-prompt
  let chosen user-one-of (word "Load questions from a local FILE\n"
              "Load questions from a WEB page\n"
              "Load the BUILT-IN questions\n"
              "Load NO questions") ["FILE" "BUILT-IN" "NO"]
  if chosen = "FILE"
  [  user-message "Select the saved questions file."
     let load-file user-file
     ifelse is-string? load-file
     [  load-local load-file
     ]
     [  set chosen "NO"
     ]
  ]
  if chosen = "BUILT-IN"
  [ load-sample-questions
  ]
  if chosen = "NO"
  [  ;do nothing, just end
  ]
end

;load the data file locally, and extract the question information from it
to load-local [from-where?]
  ifelse file-exists? from-where?
  [  file-open from-where?
     let z-data ""
     output-show word "load-local ---- " z-data
     while [not file-at-end?]
     [ set z-data word z-data file-read-characters 100
          output-show word "load-local ---- " z-data

     ]
     file-close
     clear-all-data-and-questions
     output-show read-setup z-data
     set web-questions which-questions-are-web? question-list
     set-current-question 0
     ask turtles
     [ clear-my-data
       set-my-current-question 0
     ]
  ]
  [  user-message "NetLogo can't load a non-existent file.  Load canceled."
  ]
end

;to-report is-url? [str]
;   ifelse ("" = __check-syntax ("show read-url \"" + str + "\""))
;     [ report true ]
;     [ report false ]
;end

;does all of the setting of variables and stuff.
;ALSO, reports true is successful, false if not
to-report read-setup [some-data]
  let z-start position "<!-- NETLOGO POLLER QUESTION LIST" some-data
  ifelse is-number? z-start
  [ set z-start z-start + length "<!-- NETLOGO POLLER QUESTION LIST"
    let z-end position "POLLER QUESTION LIST END -->" some-data
    ifelse is-number? z-end
    [ let z-data substring some-data z-start z-end
      clear-all-data-and-questions
      set question-list read-from-string z-data
      set-current-question 0
      ask turtles
      [ clear-my-data
        set-my-current-question 0
      ]
      set question-list read-from-string z-data
      report true
    ]
    [ report false
    ]
  ]
  [ report false
  ]
end

to-report web-data-where [response-file]
  if substring response-file (length response-file - 5) (length response-file) = ".html"
  [ report word substring response-file 0 (length response-file - 5) ".fs"]
  if substring response-file (length response-file - 4) (length response-file) = ".htm"
  [ report word substring response-file 0 (length response-file - 4) ".fs"]
  report response-file
end

to-report which-questions-are-web? [z-list]
  let a-list []
  let index 0
  repeat length z-list
  [ if not (item 1 item index z-list = "Poller")
    [ set a-list lput index a-list
    ]
    set index index + 1
  ]
  report a-list
end


















to-report safer-read-from-string [str]
   ifelse ("" = __check-syntax (word "output-show read-from-string \"\" + " str))
     [ report (read-from-string str) ]
     [ report str ]
end


















;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Save World (to capture EVERYTHING that is going on)
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to save-world-prompt
   user-message "Select the directory and filename for your saved world.  The extension .csv will be automatically added."
   let save-file word user-new-file ".csv"

   ifelse is-string? save-file
   [  export-world save-file
   ]
   [  user-message "Save has been cancelled."
   ]
end

to load-world-prompt
  if user-yes-or-no? (word "LOAD WORLD will clear all current data and remove all students.\n\n"
              "Do you want to continue?")
  [  user-message "Select the saved world file."
     let load-file user-file
     ifelse is-string? load-file
     [  import-world load-file
     ]
     [  user-message "Load world has been cancelled."
     ]
  ]
end

to auto-save-prompt
   if ((auto-save-directory = "") or (not is-string? auto-save-directory))
   [ auto-save-directory-change
   ]
   if is-string? auto-save-directory
   [  if ((auto-save-file = "") or (not is-string? auto-save-file))
      [ auto-save-file-change
      ]
      ifelse ((auto-save-file = "") or (not is-string? auto-save-file))
      [ user-message "Auto-save setup has been cancelled.  Use the AUTO-SAVE? switch to turn on the autosave feature."
        set auto-save? false
      ]
      [ set auto-saving? true
      ]
   ]
end

to auto-save-directory-change
   let option false
   user-message "Select a directory for the auto-save files.  "
   set option user-directory
   ifelse is-string? option
   [ set auto-save-directory option]
   [ user-message "World auto save directory has been left UNCHANGED"]
end

to auto-save-file-change
   let option false
   user-message "Choose a new base file name for the auto-save files.  "
   set option user-input "Base filename"
   ifelse is-string? option
   [ set auto-save-file option]
   [ user-message "World auto save base filename has been left UNCHANGED"]
end

to auto-save
   if auto-saving?
   [ export-world (word auto-save-directory "/" auto-save-file (substring remove ":" date-and-time 0 6) ".csv")]
   set activity 0
end

to auto-save-now
   if not auto-saving?
   [ set auto-save? user-yes-or-no? "Auto-save all data to file?"
     if auto-save?
     [ auto-save-prompt
     ]
   ]
   if auto-saving?
   [ export-world (word auto-save-directory "/" auto-save-file (substring remove ":" date-and-time 0 6) "NOW.csv")
   ]
end











to save-responses [to-where?]
  let my-file (word to-where? "/" user-id (substring remove ":" date-and-time 0 6) ".html")

  if file-exists? my-file
  [ file-delete my-file
  ]

  file-open my-file
  file-print "<HTML>"
  file-print "<head>"
  file-print (word "<title>Poller Responses " user-id " " (substring remove ":" date-and-time 0 6) "</title>")
  file-print "</head>"
  file-print "<body>"
  file-print "<table border=\"2\" align=\"center\" >"
  let counter-q 0
  repeat length question-list
  [
    file-print "<tr>"
    file-print "<td width=\"200\">"
    file-print (word "<b>" item 0 item counter-q question-list "</b>")
    file-print "</td>"
    file-print "<td>"
    file-print item counter-q my-choices
    file-print "</td>"
    file-print "</tr>"
    set counter-q counter-q + 1
  ]
  file-print "</table>"
  file-print "</body>"
  file-print "</html>"
  file-close
end










;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Export poll responses as spreadsheet file
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


to save-polls-prompt
   user-message "Select the directory and filename for your saved poll results.  You may want to add an extension (like .txt or .poll)"
   let save-file user-new-file

   ifelse is-string? save-file
   [  save-polls save-file
   ]
   [  user-message "Save has been cancelled."
   ]
end

to save-polls [ towhere? ]
    if count turtles > 0
    [   file-open towhere?
       file-type "User-ID\t"
       let counter-t 0
       repeat count turtles
       [  file-type [ user-id ] of turtle counter-t
          file-type "\t"
          set counter-t counter-t + 1
       ]
       file-print ""
       let counter-q 0
       repeat length question-list
       [  file-type item 0 item counter-q question-list
          file-type "\t"
          set counter-t 0
          repeat count turtles
          [  file-type [item counter-q my-choices] of turtle counter-t
             file-type "\t"
             set counter-t counter-t + 1
          ]
          file-print ""
          set counter-q counter-q + 1
       ]
       file-close
    ]
end







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;     Pop Up Help
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

to help-autosave
  user-message (word "HELP: Autosave settings\n\n"
    "When turned on, autosave will periodically automatically save your classroom data.\n"
    "The slider 'auto-save-every' determines how often auto-save happens.  "
    "You can autosave all of the current data (student data and your current viewing settings) "
    "using the regular auto-save feature.  You can turn this on using 'auto-save?'\n"
    "You can also autosave questions and students responses to an html file, for viewing on the web.  "
    "You can turn this on using 'auto-save-web?'")
end

to help-save
  user-message (word "HELP: Save options : Page 1 of 2\n\n"
    "You can save all of the current data (student data and your current viewing settings) "
    "using the 'Save ALL' button.  This saves data in the same format as 'auto-save' above.\n"
    "You can load this data for later viewing using the 'Load ALL' button.  This will overwrite "
    "everything in the current model, so you may want to save current data before using this.")
  user-message (word "HELP: Save options : Page 2 of 2\n\n"
    "'Save RESPONSES as SPREADSHEET' will save the questions and student responses in a format "
    "readable by most spreadsheet programs.\n\n"
    "'Save RESPONSES as WEBPAGE' saves all questions and student responses to an html file.  "
    "This file can be viewed in any web browser, and is the same format used by 'auto-save-web?' above.\n\n"
    "'Save INDIVIDUAL responses' saves each students responses (along with the questions) to a separate "
    "html file.")
end

to help-web
  user-message (word "HELP: Web Response settings : Page 1 of 2\n\n"
    "This tool can read responses that students have submitted to a web page "
    "(also generated by the discussion tool)\n"
    "When you have 'web-responses?' on, this tool will periodically check for new submissions "
    "to the web page. The web pages generated by this tool require that you use Formsaver "
    "(a free application for Macintosh servers)  This is not an endorsement of said product, but a "
    "historical artifact of the originating project.")
  user-message (word "HELP: Web Response settings : Page 2 of 2\n\n"
    "To use web questions in class, you must first prepare the questions.  Go to the third panel "
    "(scroll to the right) to find the preparation section.  After the questions are prepared, you "
    "must put the web page on the server, where students can access the page.  You should also prepare "
    "the page by submitting a set of default responses.  In class, when you setup the activity, you can "
    "choose to load the questions from a web page.  Put in the URL for this question page, and NetLogo "
    "will automatically load the questions you prepared and the students' responses.")
end

to help-prepare
  user-message (word "HELP: Preparation : Page 1 of 3\n\n"
    "In this screen, you can prepare questions for class.  You can prepare in class questions that "
    "use HubNet to gather real time responses from your students, and you can prepare web-based "
    "questions to allow students to give responses out of class, too "
    "(or to share information not easily shared using the in class polling)\n\n"
    "'SETUP' will clear everything in the current activity, and give you the option of loading an existing "
    "set of questions.\n\n"
    "'New Poller Question' will generate a new question that uses the HubNet client.  "
    "Using the HubNet client, students will be able to share integer responses from 0 to 50 and typed "
    "responses in real-time in class.\n\n"
    "'New Web Text Question' creates a new web based text entry question.  Students will be able to go to the "
    "web page and submit a typed response to this question.\n\n")
  user-message (word "HELP: Preparation : Page 2 of 3\n\n"
    "'New Likert Scale Question' creates a question for which students choose a number from 0 to n.\n\n"
    "New Web Number Question' creates a question for which students supply a typed number response."
    "The arrow buttons allow you to move between the questions you've created.\n\n"
    "'Edit Question' allows you to Delete, Replace, or Insert a new question at the current "
    "spot in the question list.\n\n"
    "'Clear Questions' clears all of the current questions.")
  user-message (word "HELP: Preparation : Page 3 of 3\n\n"
    "When you are satisfied with the question list, hit 'PREPARATIONS FINISHED.'  "
    "This will generate an html file in which you can view the questions (and to which you can add "
    "pictures or other materials).  This file also contains the question data in a format that this "
    "NetLogo model can work with.\n\n"
    "If you have web based questions, you should copy this preparation file to a Formsaver equipped "
    "web server.  You should then go to that page in your web browser and submit a sample set of responses "
    "using the name QUESTIONS.  If you do not have web based questions, just be sure to remember where you "
    "put the file.\n\n"
    "In class, when the activity starts up, you can choose to load a question file.  If you used "
    "web questions, then choose WEB.  If you did not, just load a LOCAL file.  Either way, tell "
    "NetLogo where to find your file.  It will automatically load the questions, and you'll be ready to go.")
end
@#$#@#$#@
GRAPHICS-WINDOW
317
142
600
446
10
10
13.0
1
10
1
1
1
0
0
0
1
-10
10
-10
10
1
1
0
ticks

BUTTON
270
10
380
63
GO
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

PLOT
10
142
310
396
Data
choice
count
0.0
11.0
0.0
5.0
true
false
"" ""
PENS
"data" 1.0 1 -16777216 true "" ""
"mean" 1.0 0 -10899396 true "" ""
"median" 1.0 0 -13345367 true "" ""
"mode" 1.0 1 -2674135 true "" ""

SWITCH
587
69
719
102
allow-change?
allow-change?
0
1
-1000

MONITOR
64
78
461
123
Current Question
item 0 item current-question question-list
3
1
11

BUTTON
1257
363
1358
396
Clear Questions
clear-all-data-and-questions-prompt
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
462
78
517
127
<<<
prev-question
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
518
78
573
127
>>>
next-question
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
10
403
148
436
show-data?
show-data?
0
1
-1000

SWITCH
587
102
719
135
lock-step?
lock-step?
1
1
-1000

BUTTON
977
199
1175
232
Save RESPONSES as SPREADSHEET
save-polls-prompt
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
1151
363
1252
396
Edit Question
edit-question
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
9
78
65
123
Number
current-question
0
1
11

SWITCH
601
142
720
175
see-names?
see-names?
1
1
-1000

CHOOSER
602
179
721
224
Turtle-Display
Turtle-Display
"Sit" "Wander" "Line-Up" "Word-Sort"
0

MONITOR
602
227
721
272
Who is it?
[user-id] of one-of turtles-on patch mouse-xcor mouse-ycor
0
1
11

SWITCH
815
55
979
88
auto-save?
auto-save?
1
1
-1000

SLIDER
815
115
979
148
auto-save-every
auto-save-every
5
100
5
5
1
votes
HORIZONTAL

BUTTON
819
201
888
234
Save ALL
save-world-prompt
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
897
199
964
232
Load ALL
load-world-prompt
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
171
403
309
448
plot-mode
plot-mode
"Histogram" "Average"
0

BUTTON
8
10
102
62
SETUP
setup-prompt
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
106
10
214
62
LOAD QUESTIONS
load-questions-prompt
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
434
10
536
62
ADD QUESTION
add-question \"Poller\"
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
614
10
719
62
SAVE
save-button-prompt
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
602
335
720
368
^ CHANGE ^
set sort-up user-input \"Sort up / down word -\"\nset Turtle-Display \"Word-Sort\"
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
602
421
720
454
^ CHANGE ^
set sort-right user-input \"Sort right / left word -\"\nset Turtle-Display \"Word-Sort\"
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
602
286
720
331
NIL
sort-up
0
1
11

MONITOR
602
372
720
417
NIL
sort-right
0
1
11

BUTTON
1232
277
1409
310
New Poller Question
add-question \"Poller\"
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
916
312
1448
357
Current Question
item 0 item current-question question-list
3
1
11

MONITOR
852
312
915
357
Number
current-question
3
1
11

BUTTON
988
363
1051
396
<<<
prev-question
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
1054
363
1117
396
>>>
next-question
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
1070
55
1529
100
Auto-save directory
auto-save-directory
3
1
11

BUTTON
987
55
1068
104
CHANGE ->
auto-save-directory-change
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
1070
106
1222
151
Auto-save base name
auto-save-file
3
1
11

BUTTON
987
106
1068
155
CHANGE ->
auto-save-file-change
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
1367
362
1545
395
PREPARATIONS FINISHED
save-questions-prompt
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
1249
106
1358
151
Type
item 1 item current-question question-list + ifelse-value (item 1 item current-question question-list = \"Likert\") [word \"  \" item 2 item current-question question-list] [\"\"]
3
1
11

BUTTON
815
10
1529
43
AUTOSAVE SETTINGS
help-autosave
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
819
162
1524
195
SAVE OPTIONS
help-save
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
820
242
1526
275
PREPARE FOR CLASS
help-prepare
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
1105
277
1230
310
SETUP
setup-prompt
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
1186
200
1353
233
Save INDIVIDUAL responses
NIL
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

OUTPUT
816
419
1321
473
12

@#$#@#$#@
## WHAT IS IT?

This model can be used to poll data from a set of students using HubNet Clients.  The teacher can input questions to ask and then the students can input their numerical choice (from 0 to 50) in response to the question.  The collective data can then be plotted.

## HOW TO USE IT

The simplest way to use this tool is to hit the GO button (to allow students to place their votes) and use the NEW QUESTION button repeatedly to input questions.  With the default settings, students will respond at their own pace, and their responses will be histogrammed in the plot.  The teacher can use the <<< and >>> buttons to move among the questions and see the students' responses.

However, this activity has many features that can be useful in preparing for discussion, inciting discussion, and later analyzing discussions.

Preparation:
The teacher can load this model before class and prepare questions for loading in class.  By scrolling the window to the right-most screen, the teacher can use the Preparation features.  The teacher can create questions that use the HubNet Client (New Poller Question).  After entering a set of questions and editing them as necessary (using the Edit Question button) the teacher can save those questions in a web-ready HTML file (using the PREPARATIONS FINISHED button).  Then, in class, the teacher can load the questions using the LOAD QUESTIONS button to load the questions either from the local harddrive.

If the teacher makes a mistake while typing a question, he can correct it using the EDIT QUESTION button.  He can insert a question before the current one, re-type the current question, or delete a question.  (These can all be done in class, on the fly, as well)

Inciting Discussion:
In the default settings, students can move through questions at their own pace, they can change their answers as often as they want, and the class results are histogrammed on the teacher's computer in real time.  What if the teacher wants students to make their decisions without seeing what their classmates have chosen?  The teacher can turn off SHOW-DATA?.  The teacher can show the average response to several questions side-by-side by changing PLOT-MODE to "Average."  If the teacher wants to call everyone's attention to a particular question, he can turn on LOCK-STEP?.  If the teacher wants students to place a vote and stick to it, he can turn off ALLOW-CHANGE?.

The ability for students to give anonymous responses can help fuel discussions.  Teachers should feel free to allow students to log in with fake names, or turn off SEE-NAMES?  When students see that others share their opinions, they tend to be more willing to discuss the reasoning behind their views, and avoid the classic problems of "I don't want to seem stupid" or "I don't want to seem smart."

In addition to the plot, turtle positions can be used to present information about students' responses.  By setting the TURTLE-DISPLAY chooser to "Sit" or "Wander", the teacher can have the turtles sort left and right according to if they've voted or not.  "Line Up" causes the turtles to duplicate a histogram of their choices.  "Word Sort" will cause the turtles to sort left / right and up /down according to the presence of a string in their responses (so, for example, all of the students who included the word "gravity" would move to the top half of the view, and all the students who included the word "air resistance" would move to the right, if the teacher chose those words for the sorting).

While students are voting, the teacher can have the model automatically save their data, allowing for later analysis or use in discussion.  When the teacher turns on AUTO-SAVE?,  prompts help him pick the directory and file name for the saved data.

Analyzing Discussion:
After a discussion has occurred, the teacher can load the class data from different times in the discussion and view the responses.  The teacher can also export the data in a format that can be conveniently opened in any spreadsheet.  Just LOAD "world data" to revisit the students' responses in the tool, and use "save RESPONSES to SPREADSHEET" to save interesting data to a spreadsheet readable format.
Widgets:

Screen 1 (left)
SETUP - clears everything including logged in clients.  This should only be pressed when starting out with a new group of users since all data and questions are lost.  Presents the load questions menu
LOAD QUESTIONS - presents a menu of options for loading questions
GO - processes data from the clients.  THIS MUST BE ON to receive responses or use the interface.
ADD QUESTION - adds a new HubNet Client question
SAVE - presents a menu of options for saving questions and student responses

Number - shows the current question number (starts at 0)
Current Question - shows the current question being voted on by the clients
<<< - changes the current question to the previous question
>>> - changes the current question to the next question.
allow-change? - controls whether clients are able to change their choices after they've chosen.  Can be changed at any time.
lock-step? - controls whether clients are forced to view only the question the teacher is currently viewing.  Can be changed at any time.

show-data? - controls whether the data collected are shown in the plot window.  Can be changed at any time
plot-mode - controls whether a histogram of current responses is shown, or a plot showing the average to several questions

see-names? - controls whether the turtle shows the corresponding user ID.  Can be changed at any time
turtle-display - controls how the turtles position themselves.  With "sit" and "wander," turtles who have voted move to the right half of the screen.  With "line up," the turtles line up according to the number value of their responses (and turtles who haven't voted or who have a string response move to the far left).  With "word sort," turtles are sorted up / down and right / left according to the presence of a substring in their string response (turtles who've not voted or who have a numerical response gather in the middle)
Who Is It? - shows the user id of the turtle under the mouse
Sort-Up - shows the word used to sort turtles up / down
^ CHANGE ^ - changes the sort-up value
Sort-Right - shows the word used to sort turtles right / left
^ CHANGE ^ - changes the sort-right value

Screen 2 (middle)
auto-save? - determines whether autosave is on or off
auto-save-every - determines how often the model autosaves, based on the number of votes cast between saves
auto-save directory - the directory in which autosave files are written
auto-save base name - the base name for autosave files.  The time is added to this name.

Save ALL - save all of the current information - questions, student responses, interface settings.  This is the format used by auto-save
Load ALL - load an ALL file
Save RESPONSES as SPREADSHEEET - saves student questions and responses in a spreadsheet compatible format
Save INDIVIDUAL responses - creates a web page for each student with the questions and that individual's responses

Screen 3 (right)
SETUP - clears everything including logged in clients.  This should only be pressed when starting out with a new group of users since all data and questions are lost.  Presents the load questions menu
Number - shows the current question number (starts at 0)
Current Question - shows the current question
Type - shows the type of the current questions (Poller, Web Text, Likert, or Web Number)
<<< - changes the current question to the previous question
>>> - changes the current question to the next question.
Edit Question - calls a menu of options for editing the current question.  REPLACE replaces the current question, DELETE deletes it, and INSERT inserts a new question
Clear Questions - clears all of the questions
New Poller Question - adds a new HubNet Client question

## EXTENDING THE MODEL

There are many more ways the turtles could be used to convey information.  Change the model so they set their shape according to type of response they have to the current question.  Or have the turtles adjust their size according to how many responses they have.  There could also be a turtle-display mode where they sit according to their responses to two questions.

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
MONITOR
14
128
558
177
Question Three
NIL
0
1

MONITOR
14
14
123
63
Question Number
NIL
0
1

BUTTON
174
187
275
220
<<PREV
NIL
NIL
1
T
OBSERVER
NIL
NIL

BUTTON
285
187
391
220
NEXT>>
NIL
NIL
1
T
OBSERVER
NIL
NIL

SLIDER
122
305
428
338
Choice
Choice
0
50
25
1
1
NIL
HORIZONTAL

BUTTON
206
345
351
378
Choose
NIL
NIL
1
T
OBSERVER
NIL
C

INPUTBOX
13
386
484
557
Typed-Response
Click on the CHANGE button \n(in the top right corner of this widget)\nto type a response.
1
1
String

MONITOR
204
252
350
301
Current Choice
NIL
0
1

MONITOR
14
99
558
148
Question Two
NIL
0
1

MONITOR
14
69
558
118
Question
NIL
3
1

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
