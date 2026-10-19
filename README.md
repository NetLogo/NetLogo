
# LS Extension for NetLogo

LevelSpace is an extension for NetLogo that allows you to run several models concurrently and have them "talk" with each other. LevelSpace models are hierarchical, in that models always belong hierarchically to another model. In this documentation, we will refer to models that have loaded LevelSpace and have opened models as 'parents', and to the models they have opened as 'children' or 'child models'.

## LevelSpace fundamentals

LevelSpace must be loaded in a model using ```extensions [ls]``` at the top of your model. Once this is done, a model will be able to load up other models using the LevelSpace primitives, run commands and reporters in them, and close them down when they are no longer needed.

Asking and reporting in LevelSpace is conceptually pretty straight forward: You pass blocks of code to child models, and the child models respond as if you had typed that code into their Command Center. LevelSpace allows you to report strings, numbers, and lists from a child to its parent. It is not possible to directly report turtles, patches, links, or any of their respective sets. Further, it is not possible to push data from a child to its parent - parents must ask their children to report. This mimicks the way in which turtles cannot "push" data to the observer, but rely on the observer to ask them for it.

In general, the LevelSpace syntax has been designed to align with existing NetLogo primitives whenever possible.

### Headless and Interactive Models

LevelSpace has two different child model types; headless models and interactive models. They each have their strengths and weaknesses:

Interactive models
* are full-fledged models that give full access to their interface and widgets,
* run a bit slower, and use more memory
* are visible by default

Headless Models
* only give you access to their view and command center
* are faster and use less memory than interactive models.
* are hidden by default

Typically you will want to use headless models when you are running a large number of models, or if you simply want to run them faster. Interactive models are good if you run a small amount of models, if you are writing a LevelSpace model and need to be able to debug, or if you need access to widgets during runtime.

### Keeping Track of Models

Child models are kept track of in the extension with an id number, starting with 0, and all communication from parent to child is done by referencing this number, henceforth referred to as `model-id`.

The easiest way to work with multiple models is to store their `model-id` in a list, and use NetLogo's list primitives to sort, filter, etc. them during runtime.

Keeping track of models is important: Most LevelSpace primitives will fail and cause a runtime interruption if provided a `model-id` to a non-existing model. You can use `ls:model-exists? model-id` to check if `model-id` refers to an existing model.

### A general use case: Asking and Reporting

This use case is based on the Model Visualizer and Plotter Example-model from the NetLogo Models Library.

A simple thing we can do is to open up some models, run them concurrently, and calculate the average of some reporter. Let's say that we are interested in finding the mean number of sheep in a bunch of Wolf Sheep Predation models. First we would open up some of these models, and set them up:

```
to setup
  ls:reset
  ca
  ls:create-models 30 "Wolf Sheep Predation.nlogox"
  ls:ask ls:models [ set grass? true setup ]
  reset-ticks
end
```
We then want to run all our child models, and then find out what the mean number of sheep is:
```
to go
    ls:ask ls:models [ go ]
    show mean [ count sheep ] ls:of ls:models
end
```

### A general use case: Inter-Model Interactions

This use case is based on the Model Interactions Example-model from the NetLogo Models Library.

Let's imagine that we have two models: a Wolf Sheep Predation-model called `WSP`, and a Climate Change model called `CC`. Now let's imagine that we want the regrowth time in the wSP model to depend on the temperature in the CC model. Using LevelSpace's primitives, we could do something like this:

```
  ; save new regrowth time in a temporary LevelSpace let-variable
  ls:let new-regrowth-time 25 + ( abs [ temperature - 55 ] ls:of CC ) / 2

  ; remove decimals, pass it to the wolf sheep predation model and change the time
  ls:ask WSP [
    set grass-regrowth-time round new-regrowth-time
  ]

  ; finally ask both models to go
  ls:ask ls:models [ go ]
```

### A general Usecase: Tidying up "Dead" Child Models

As previously mentioned, it is important to keep track of "living" and "dead" models when you dynamically create and dispose of models. Let us imagine we have some lists of models of different kinds, and we want to make sure that we only keep the models that are alive. After running code that kills child models we can use the `ls:model-exists?` primitive to clean up our list of models like this:

```
to-report remove-dead-models [list-of-models]
  report filter [ [ model-id ] -> ls:model-exists model-id] list-of-models
end
```

We then reassign each list of models with this, e.g.

```

set a-list-of-models remove-dead-models a-list-of-models
set another-list-of-models remove-dead-models a-list-of-models
```

## Citing LevelSpace in Research

If you use LevelSpace in research, we ask that you cite us,

Hjorth, A.  Head, B. & Wilensky, U. (2015). “LevelSpace NetLogo extension”. http://ccl.northwestern.edu/rp/levelspace/index.shtml Evanston, IL: Center for Connected Learning and Computer Based Modeling, Northwestern University.


## Primitives

### Commanding and Reporting

[`ls:ask`](#lsask)
[`ls:of`](#lsof)
[`ls:report`](#lsreport)
[`ls:with`](#lswith)
[`ls:let`](#lslet)
[`ls:assign`](#lsassign)

### Logic and Control

[`ls:models`](#lsmodels)
[`ls:show`](#lsshow)
[`ls:show-all`](#lsshow-all)
[`ls:hide`](#lshide)
[`ls:hide-all`](#lshide-all)
[`ls:path-of`](#lspath-of)
[`ls:name-of`](#lsname-of)
[`ls:model-exists?`](#lsmodel-exists?)

### Opening and Closing Models

[`ls:create-models`](#lscreate-models)
[`ls:create-interactive-models`](#lscreate-interactive-models)
[`ls:close`](#lsclose)
[`ls:reset`](#lsreset)



### `ls:create-models`

```NetLogo
ls:create-models *number* *path*
(ls:create-models *number* *path* *arrow-command*)
```


Create the specified number of instances of the given model.  The path can be absolute, or relative to the main model. Compared with `ls:create-interactive-models`, this primitive creates lightweight models that are hidden by default. You should use this primitive if you plan on having many instances of the given model. The models may be shown using `ls:show`; when visible, they will have a view and command center, but no other widgets, e.g. plots or monitors.

If given a command, LevelSpace will call the command after loading each instance of the model with the `model-id` as the argument. This allows you to easily store model ids in a variable or list when loading models, or do other initialization. For example, to store a model id in a variable, you can do:

```NetLogo
let model-id 0
(ls:create-models "My-Model.nlogox" [ [id] -> set model-id id ])
```

Child model RNGs are seeded from the parent models RNG when they are created.
Thus, if you seed the parent's model RNG before child model before child models are created, the simulation as a whole will be reproducible.
Use the `ls:random-seed` primitive to seed the model system's RNGs after child models have been created.



### `ls:create-interactive-models`

```NetLogo
ls:create-interactive-models *number* *path*
(ls:create-interactive-models *number* *path* *arrow-command*)
```


Like `ls:create-models`, creates the specified number of instances of the given model. Unlike `ls:create-models`, `ls:create-interactive-models` creates models that are visible by default, and have all widgets. You should use this primitive if you plan on having only a handful of instances of the given model, and would like to be able to interact with the instances through their interfaces during runtime.

Child model RNGs are seeded from the parent models RNG when they are created.
Thus, if you seed the parent's model RNG before child model before child models are created, the simulation as a whole will be reproducible.
Use the `ls:random-seed` primitive to seed the model system's RNGs after child models have been created.



### `ls:close`

```NetLogo
ls:close *model-or-list-of-models*
```


Close the model or models with the given `model-id`.



### `ls:reset`

```NetLogo
ls:reset
```


Close down all child models (and, recursively, their child models). You'll often want to call this in your setup procedure.

Note that `clear-all` does *not* close LevelSpace models.



### `ls:ask`

```NetLogo
ls:ask *model-or-list-of-models* *command* *argument*
```


Ask the given child model or list of child models to run the given command. This is the primary of doing things with child models. For example:

```NetLogo
ls:ask model-id [ create-turtles 5 ]
```

You can also ask a list of models to all do the same thing:

```NetLogo
ls:ask ls:models [ create-turtles 5 ]
```

You may supply the command with arguments, just like you would with arrow commands:

```NetLogo
let turtle-id 0
let speed 5
(ls:ask model-id [ [t s] -> ask turtle t [ fd s ] ] turtle-id speed)
```

Note that the commands cannot access variables in the parent model directly. You must either pass information in through arguments or using `ls:let`.



### `ls:of`

```NetLogo
ls:of *reporter* *model-or-list-of-models*
```


Run the given reporter in the given model and report the result.

`ls:of` is designed to work like NetLogo's inbuilt `of`: If you send `ls:of` a `model-id`, it will report the value of the reporter from that model. If you send it a list of model-ids, it will report a list of values of the reporter string from all models. You cannot pass arguments to `ls:of`, but you can use `ls:let`.

```NetLogo
[ count turtles ] ls:of model-id
```



### `ls:report`

```NetLogo
ls:report *model-or-list-of-models* *reporter* *argument*
```


Run the given reporter in the given model and report the result. This form exists to allow you to pass arguments to the reporter.

```NetLogo
let turtle-id 0
(ls:report model-id [ [a-turtle] -> [ color ] of turtle a-turtle ] turtle-id)
```



### `ls:with`

```NetLogo
ls:with *list-of-models* *reporter*
```


Reports a new list of models containing only those models that report `true` when they run the reporter block.

```NetLogo
ls:models ls:with [ count turtles > 100 ]
```



### `ls:let`

```NetLogo
ls:let *variable-name* *value*
```


Creates a variable containing the given data that can be accessed by the child models.

```NetLogo
ask turtles [
  ls:let my-color color
  ls:ask my-model [
    ask turtles [ set color my-color ]
  ]
]
```

`ls:let` works quite similar to `let` in that the variable is only locally accessible:

```NetLogo
ask turtles [
  ls:let my-color color
]
;; my-color is innaccessible here
```

`ls:let` is very similar to `let`, except in a few cases.

- `ls:let` will overwrite previous values in the variable

If you do

```NetLogo
ls:let my-var 5
ls:let my-var 6
```

`my-var` will be set equal to `6`. There is no `ls:set`.

- `ls:let` supports variable shadowing

If you do

```NetLogo
ls:let my-var 5
ask turtles [
  ls:let my-var 6
  ls:ask child-model [ show my-var ]
]
ls:ask child-model [ show my-var ]
```

`child-model` will show `6` and then `5`. This is known as [variable shadowing](https://en.wikipedia.org/wiki/Variable_shadowing).

- The parent model cannot directly read the value of an ls variable

For example, this does *not* work.

```NetLogo
ls:let my-var 5
show my-var
```

This is intentional. ls variables are meant to be used for sharing data with child models. The parent model already has access to the data.

Furthermore, changing the value of an ls let variable in a child model will not affect it in any other model. For example:

```NetLogo
ls:let my-var 0
ls:ask ls:models [
  set my-var my-var + 1
  show my-var
]
```

All models will print `1`.



### `ls:assign`

```NetLogo
ls:assign *model-or-list-of-models* *global-variable* *value*
```


Sets the given global variable in child model to given value. For instance

```netlogo
ls:assign ls:models glob1 count turtles
```

sets the global variable `glob1` in all models to the parent's model `count turtles`.



### `ls:models`

```NetLogo
ls:models
```


Report a list of model-ids for all existing models.



### `ls:show`

```NetLogo
ls:show *model-or-list-of-models*
```


Makes all of the given models visible.



### `ls:show-all`

```NetLogo
ls:show-all *model-or-list-of-models*
```


Makes all of the given models *and their descendents* visible.



### `ls:hide`

```NetLogo
ls:hide *model-or-list-of-models*
```


Hide all of the given models. Hiding models is a good way of making your simulation run faster.



### `ls:hide-all`

```NetLogo
ls:hide-all *model-or-list-of-models*
```


Hide all of the given models *and their descendents*. Hiding models is a good way of making your simulation run faster.



### `ls:path-of`

```NetLogo
ls:path-of *model-or-list-of-models*
```


Report the full path, including the file name of the model. If a list of models is given, a list of paths is reported.



### `ls:name-of`

```NetLogo
ls:name-of *model-or-list-of-models*
```


Reports the name of the model file. This is the name of the window in which the model appears when visible. If a list of models is given, a list of names is reported.



### `ls:model-exists?`

```NetLogo
ls:model-exists? *model-or-list-of-models*
```


Report a boolean value for whether there is a model with that model-id. This is often useful when you are dynamically generating models, and want to ensure that you are not asking models that no longer exist to do stuff.



### `ls:random-seed`

```NetLogo
ls:random-seed *seed*
```


Behaves exactly like NetLogo's built-in primitive `random-seed`, except that child models have their RNGs seeded based on the given seed as well (as well their child models, and their child models' child models, and so forth).
This primitive should almost always be used instead of NetLogo's built-in one for seeding RNG when using LevelSpace.



## Terms of Use

Copyright 1999-2016 by Uri Wilensky.

This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License](http://www.gnu.org/copyleft/gpl.html) as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

