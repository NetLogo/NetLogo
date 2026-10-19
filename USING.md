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
