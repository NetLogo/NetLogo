
# BehaviorSpace Extension for NetLogo

The BehaviorSpace extension allows you to create and run BehaviorSpace experiments from NetLogo code. You can use the
extension alone or in combination with the GUI BehaviorSpace tool, and you can run experiments created in the GUI tool
with the extension.

## Using the BehaviorSpace Extension

To use the BehaviorSpace extension in a model, include the line ```extensions [bspace]``` at the top of the code. If
you are already using another extension, you can add ```bspace``` to the list inside the square brackets.

Once the extension is loaded, you can use it to run experiments from anywhere in the code. Note that you must call
```clear-all``` to clear the experiments stored in the code, otherwise they will persist between runs of the model.

## Primitives

### Experiment Management

[`bspace:create-experiment`](#bspacecreate-experiment)
[`bspace:run-experiment`](#bspacerun-experiment)

### Experiment Modification

[`bspace:set-name`](#bspaceset-name)
[`bspace:set-pre-experiment-commands`](#bspaceset-pre-experiment-commands)
[`bspace:set-setup-commands`](#bspaceset-setup-commands)
[`bspace:set-go-commands`](#bspaceset-go-commands)
[`bspace:set-post-run-commands`](#bspaceset-post-run-commands)
[`bspace:set-post-experiment-commands`](#bspaceset-post-experiment-commands)
[`bspace:set-repetitions`](#bspaceset-repetitions)
[`bspace:set-sequential-run-order`](#bspaceset-sequential-run-order)
[`bspace:set-run-metrics-every-step`](#bspaceset-run-metrics-every-step)
[`bspace:set-run-metrics-condition`](#bspaceset-run-metrics-condition)
[`bspace:set-time-limit`](#bspaceset-time-limit)
[`bspace:set-exit-condition`](#bspaceset-exit-condition)
[`bspace:set-metrics`](#bspaceset-metrics)
[`bspace:set-variables`](#bspaceset-variables)
[`bspace:set-thread-count`](#bspaceset-thread-count)
[`bspace:set-table`](#bspaceset-table)
[`bspace:set-spreadsheet`](#bspaceset-spreadsheet)
[`bspace:set-stats`](#bspaceset-stats)
[`bspace:set-lists`](#bspaceset-list)
[`bspace:set-update-view`](#bspaceset-update-view)
[`bspace:set-update-plots`](#bspaceset-update-plots)

### `bspace:create-experiment`

```NetLogo
bspace:create-experiment *name*
```

Create a new experiment with the specified name. An error will be thrown if an experiment already exists with the
provided name.

### `bspace:run-experiment`

```NetLogo
bspace:run-experiment *name*
```

Run the experiment with the specified name. An error will be thrown if an experiment does not exist with the provided
name.

### `bspace:set-name`

```NetLogo
bspace:set-name *old-name* *new-name*
```

Rename the specified experiment. An error will be thrown if an experiment does not exist with the provided name.

### `bspace:set-pre-experiment-commands`

```NetLogo
bspace:set-pre-experiment-commands *name* *commands*
```

Set the pre-experiment commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-setup-commands`

```NetLogo
bspace:set-setup-commands *name* *commands*
```

Set the setup commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-go-commands`

```NetLogo
bspace:set-go-commands *name* *commands*
```

Set the go commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-post-run-commands`

```NetLogo
bspace:set-post-run-commands *name* *commands*
```

Set the post-run commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-post-experiment-commands`

```NetLogo
bspace:set-post-experiment-commands *name* *commands*
```

Set the post-experiment commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-repetitions`

```NetLogo
bspace:set-repetitions *name* *repetitions*
```

Set the repetitions for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-sequential-run-order`

```NetLogo
bspace:set-sequential-run-order *name* *boolean*
```

Set whether the specified experiment uses sequential run order. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-run-metrics-every-step`

```NetLogo
bspace:set-run-metrics-every-step *name* *boolean*
```

Set whether the specified experiment runs metrics every step. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-run-metrics-condition`

```NetLogo
bspace:set-run-metrics-condition *name* *condition*
```

Set the run metrics condition for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-time-limit`

```NetLogo
bspace:set-time-limit *name* *ticks*
```

Set the time limit in ticks for the specified experiment. An error will be thrown if an experiment does not exist with
the provided name.

### `bspace:set-exit-condition`

```NetLogo
bspace:set-exit-condition *name* *condition*
```

Set the exit condition for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name.

### `bspace:set-metrics`

```NetLogo
bspace:set-metrics *name* *commands*
```

Set the metrics commands for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name.

### `bspace:set-variables`

```NetLogo
bspace:set-variables *name* *variables*
```

Set the variables to vary for the specified experiment. An error will be thrown if an experiment does not exist with
the provided name.

### `bspace:set-thread-count`

```NetLogo
bspace:set-thread-count *name* *threads*
```

Set the thread count for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name.

### `bspace:set-table`

```NetLogo
bspace:set-table *name* *path*
```

Set the path for the table file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-spreadsheet`

```NetLogo
bspace:set-spreadsheet *name* *path*
```

Set the path for the spreadsheet file for the specified experiment. An error will be thrown if an experiment does not
exist with the provided name.

### `bspace:set-stats`

```NetLogo
bspace:set-stats *name* *path*
```

Set the path for the stats file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-lists`

```NetLogo
bspace:set-lists *name* *path*
```

Set the path for the lists file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-update-view`

```NetLogo
bspace:set-update-view *name* *boolean*
```

Set whether the specified experiment should update the view. An error will be thrown if an experiment does not exist
with the provided name.

### `bspace:set-update-plots`

```NetLogo
bspace:set-update-view *name* *boolean*
```

Set whether the specified experiment should update the plots. An error will be thrown if an experiment does not exist
with the provided name.

