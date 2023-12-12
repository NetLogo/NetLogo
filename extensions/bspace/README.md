
# BehaviorSpace Extension for NetLogo

The BehaviorSpace extension allows you to create and run BehaviorSpace experiments from NetLogo code. You can use the
extension alone or in combination with the GUI BehaviorSpace tool, and you can run experiments created in the GUI tool
with the extension.

## Using the BehaviorSpace Extension

To use the BehaviorSpace extension in a model, include the following line at the top of your code:

```
extensions [bspace]
```

If you are already using another extension, you can just add `bspace` to the list inside the square brackets.

Once the extension is loaded, you can use it to run experiments from anywhere in the code. Note that you must call
```clear-all``` to clear the experiments stored in the code, otherwise they will persist between runs of the model.

## Primitives

Note: unless otherwise specified, any commands or reporters used as input for a BehaviorSpace extension primitive
should be a string.

### Experiment Management

[`bspace:create-experiment`](#bspacecreate-experiment)
[`bspace:delete-experiment`](#bspacedelete-experiment)
[`bspace:run-experiment`](#bspacerun-experiment)
[`bspace:rename-experiment`](#bspacerename-experiment)

### Experiment Parameters

[`bspace:set-variables`](#bspaceset-variables)
[`bspace:set-repetitions`](#bspaceset-repetitions)
[`bspace:set-sequential-run-order`](#bspaceset-sequential-run-order)
[`bspace:set-metrics`](#bspaceset-metrics)
[`bspace:set-run-metrics-every-step`](#bspaceset-run-metrics-every-step)
[`bspace:set-run-metrics-condition`](#bspaceset-run-metrics-condition)
[`bspace:set-pre-experiment-commands`](#bspaceset-pre-experiment-commands)
[`bspace:set-setup-commands`](#bspaceset-setup-commands)
[`bspace:set-go-commands`](#bspaceset-go-commands)
[`bspace:set-stop-condition`](#bspaceset-stop-condition)
[`bspace:set-post-run-commands`](#bspaceset-post-run-commands)
[`bspace:set-post-experiment-commands`](#bspaceset-post-experiment-commands)
[`bspace:set-time-limit`](#bspaceset-time-limit)
[`bspace:set-return-reporter`](#bspaceset-return-reporter)

### Experiment Run Conditions

[`bspace:set-spreadsheet`](#bspaceset-spreadsheet)
[`bspace:set-table`](#bspaceset-table)
[`bspace:set-stats`](#bspaceset-stats)
[`bspace:set-lists`](#bspaceset-list)
[`bspace:set-update-view`](#bspaceset-update-view)
[`bspace:set-update-plots`](#bspaceset-update-plots)
[`bspace:set-parallel-runs`](#bspaceset-parallel-runs)

### Experiment Information

[`bspace:goto-behaviorspace-documentation`](#bspacegoto-behaviorspace-documentation)
[`bspace:goto-bspace-extension-documentation`](#bspacegoto-bspace-extension-documentation)
[`bspace:get-default-parallel-runs`](#bspaceget-default-parallel-runs)
[`bspace:get-recommended-max-parallel-runs`](#bspaceget-recommended-max-parallel-runs)
[`bspace:get-return-value`](#bspaceget-return-value)

### bspace:create-experiment

#### bspace:create-experiment *name*

Create a new experiment with the specified name. An error will be thrown if an experiment already exists with the
provided name.

Example:

```
bspace:create-experiment "my-experiment"
```

### bspace:delete-experiment

#### bspace:delete-experiment *name*

Delete the experiment with the specified name. An error will be thrown if no experiment exists with the provided name.

Example:

```
bspace:delete-experiment "my-experiment"
```

### bspace:run-experiment

#### bspace:run-experiment *name*

Run the experiment with the specified name. An error will be thrown if an experiment does not exist with the provided
name.

Example:

```
bspace:run-experiment "my-experiment"
```

### bspace:rename-experiment

#### bspace:rename-experiment *old-name* *new-name*

Rename the specified experiment. An error will be thrown if an experiment does not exist with the provided name.

Example:

```
bspace:rename-experiment "my-experiment" "another-experiment"
```

### bspace:set-variables

#### bspace:set-variables *name* *variables*

Set the variables to vary for the specified experiment. An error will be thrown if an experiment does not exist with
the provided name.

Example:

```
bspace:set-variables "my-experiment" "[ 'var1' 0 5 20 ]"
```

### bspace:set-repetitions

#### bspace:set-repetitions *name* *repetitions*

Set the repetitions for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-repetitions "my-experiment" 3
```

### bspace:set-sequential-run-order

#### bspace:set-sequential-run-order *name* *boolean*

Set whether the specified experiment uses sequential run order. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-sequential-run-order "my-experiment" true
```

### bspace:set-metrics

#### bspace:set-metrics *name* *commands-list*

Set the metrics commands for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name. This command accepts a list of string commands.

Example:

```
bspace:set-metrics "my-experiment" [ "count turtles", "count patches" ]
```

### bspace:set-run-metrics-every-step

#### bspace:set-run-metrics-every-step *name* *boolean*

Set whether the specified experiment runs metrics every step. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-run-metrics-every-step "my-experiment" false
```

### bspace:set-run-metrics-condition

#### bspace:set-run-metrics-condition *name* *condition*

Set the run metrics condition for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-run-metrics-condition "my-experiment" "count turtles < 5"
```

### bspace:set-pre-experiment-commands

#### bspace:set-pre-experiment-commands *name* *commands*

Set the pre-experiment commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-pre-experiment-commands "my-experiment" "clear-all"
```

### bspace:set-setup-commands

#### bspace:set-setup-commands *name* *setup*

Set the setup commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-setup-commands "my-experiment" "clear-all create-turtles 50"
```

### bspace:set-go-commands

#### bspace:set-go-commands *name* *commands*

Set the go commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-go-commands "my-experiment" "myFunction"
```

### bspace:set-stop-condition

#### bspace:set-stop-condition *name* *condition*

Set the stop condition for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name.

Example:

```
bspace:set-stop-condition "my-experiment" "ticks > 1000"
```

### bspace:set-post-run-commands

#### bspace:set-post-run-commands *name* *commands*

Set the post-run commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-post-run-commands "my-experiment" "print count turtles"
```

### bspace:set-post-experiment-commands

#### bspace:set-post-experiment-commands *name* *commands*

Set the post-experiment commands for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-post-experiment-commands "my-experiment" "ask turtles [ setxy 0 0 ]"
```

### bspace:set-time-limit

#### bspace:set-time-limit *name* *ticks*

Set the time limit in ticks for the specified experiment. An error will be thrown if an experiment does not exist with
the provided name.

Example:

```
bspace:set-time-limit "my-experiment" 1500
```

### bspace:set-return-reporter

#### bspace:set-return-reporter *experiment-name* *value-name* *reporter*

Set a named return value for the specified experiment. An error will be thrown if an experiment does not exist with the
provided name.

Example:

```
bspace:set-return-reporter "my-experiment" "numTurtles" "count turtles"
```

### bspace:set-spreadsheet

#### bspace:set-spreadsheet *name* *path*

Set the path for the spreadsheet file for the specified experiment. An error will be thrown if an experiment does not
exist with the provided name.

Example:

```
bspace:set-spreadsheet "my-experiment" "/Users/johndoe/Documents/exp-sheet.csv"
```

### bspace:set-table

#### bspace:set-table *name* *path*

Set the path for the table file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-table "my-experiment" "/Users/johndoe/Documents/exp-table.csv"
```

### bspace:set-stats

#### bspace:set-stats *name* *path*

Set the path for the stats file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-stats "my-experiment" "/Users/johndoe/Documents/exp-stats.csv"
```

### bspace:set-lists

#### bspace:set-lists *name* *path*

Set the path for the lists file for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-lists "my-experiment" "/Users/johndoe/Documents/exp-lists.csv"
```

### bspace:set-update-view

#### bspace:set-update-view *name* *boolean*

Set whether the specified experiment should update the view. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-update-view "my-experiment" true
```

### bspace:set-update-plots

#### bspace:set-update-view *name* *boolean*

Set whether the specified experiment should update the plots. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-update-view "my-experiment" false
```

### bspace:set-parallel-runs

#### bspace:set-parallel-runs *name* *threads*

Set the number of parallel runs for the specified experiment. An error will be thrown if an experiment does not exist
with the provided name.

Example:

```
bspace:set-parallel-runs "my-experiment" 3
```

### bspace:goto-behaviorspace-documentation

#### bspace:goto-behaviorspace-documentation

Open the BehaviorSpace documentation page in a browser window.

Example:

```
bspace:goto-behaviorspace-documentation
```

### bspace:goto-bspace-extension-documentation

#### bspace:goto-bspace-extension-documentation

Open the bspace extension documentation page in a browser window.

Example:

```
bspace:goto-bspace-extension-documentation
```

### bspace:get-default-parallel-runs

#### bspace:get-default-parallel-runs

Returns the default number of parallel runs for the current device.

Example:

```
print bspace:get-default-parallel-runs
```

### bspace:get-recommended-max-parallel-runs

#### bspace:get-recommended-max-parallel-runs

Returns the recommended maximum number of parallel runs for the current device.

Example:

```
print bspace:get-recommended-max-parallel-runs
```

### bspace:get-return-value

#### bspace:get-return-value *value-name*

Gets the return value with the specified name. Will throw an error if no return value has been set with the specified
name.

Example:

```
print bspace:get-return-value "numTurtles"
```

