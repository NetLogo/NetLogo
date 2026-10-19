# NetLogo Profiler Extension

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 6.0). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `profiler.jar` will be created.

## Using the Profiler Extension

If you'd like your model to run faster, the profiler extension
may be useful to you. It includes primitives that measure how many
times the procedures in your model are called during a run and how
long each call takes. You can use this information to where to focus
your speedup efforts.

**Caution:**

The profiler extension is experimental. It is not yet well tested or
user friendly. Nonetheless, we think some users will find it useful.

### How to use

The profiler extension comes preinstalled. To use the extension in
your model, add a line to the top of your Code tab:

```NetLogo
extensions [profiler]
```

If your model already uses other extensions, then it already has an
`extensions` line in it, so just add `profiler` to the list.

For more information on using NetLogo extensions,
see the [Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)

### Example

```NetLogo
setup                  ;; set up the model
profiler:start         ;; start profiling
repeat 20 [ go ]       ;; run something you want to measure
profiler:stop          ;; stop profiling
print profiler:report  ;; view the results
profiler:reset         ;; clear the data
```

Another way to use the profiler is to export its raw data using
the [`csv` extension](https://docs.netlogo.org/csv.html)
and the `profiler:data` primitive:

```NetLogo
extensions [ csv profiler ]

to profile
  setup                                          ;; set up the model
  profiler:start                                 ;; start profiling
  repeat 20 [ go ]                               ;; run something you want to measure
  profiler:stop                                  ;; stop profiling
  csv:to-file "profiler_data.csv" profiler:data  ;; save the results
  profiler:reset                                 ;; clear the data
end
```

Running the above procedure will write a `profiler_data.csv` file that you can then load into your
favorite data analysis program. Here is an example data file produced using
the [Wolf Sheep Predation](https://ccl.northwestern.edu/netlogo/models/WolfSheepPredation) model:

```CSV
procedure,calls,inclusive_time,exclusive_time
EAT-SHEEP,1085,1.237283,11.637364
DISPLAY-LABELS,20,17.389512,17.389512
MOVE,3578,14.164451,14.164451
GO,20,683.414619,604.773837
GRASS,3,0.05384,0.05384
DEATH,1085,3.172517,3.172517
REPRODUCE-SHEEP,2493,10.265135,10.265135
REPRODUCE-WOLVES,1039,5.599509,5.599509
```

Thanks to Roger Peppe for his contributions to the code.

## Primitives

[`profiler:calls`](#profilercalls)
[`profiler:exclusive-time`](#profilerexclusive-time)
[`profiler:inclusive-time`](#profilerinclusive-time)
[`profiler:start`](#profilerstart)
[`profiler:stop`](#profilerstop)
[`profiler:reset`](#profilerreset)
[`profiler:report`](#profilerreport)
[`profiler:data`](#profilerdata)

### `profiler:calls`

```NetLogo
profiler:calls procedure-name
```

Reports the number of times that _procedure-name_ was called. If
_procedure-name_ is not defined, then reports 0.

### `profiler:exclusive-time`

```NetLogo
profiler:exclusive-time procedure-name
```

Reports the exclusive time, in milliseconds, that
_procedure-name_ was running for. Exclusive time is the time
from when the procedure was entered, until it finishes, but does not
include any time spent in other user-defined procedures which it
calls.

If _procedure-name_ is not defined, then reports 0.

### `profiler:inclusive-time`

```NetLogo
profiler:inclusive-time procedure-name
```

Reports the inclusive time, in milliseconds, that
_procedure-name_ was running for. Inclusive time is the time
from when the procedure was entered, until it finishes.

If _procedure-name_ is not defined, then reports 0.

### `profiler:start`

```NetLogo
profiler:start
```

Instructs the profiler to begin recording user-defined procedure calls.

### `profiler:stop`

```NetLogo
profiler:stop
```

Instructs the profiler to stop recording user-defined procedure calls.

### `profiler:reset`

```NetLogo
profiler:reset
```

Instructs the profiler to erase all collected data.

### `profiler:report`

```NetLogo
profiler:report
```

Reports a string containing a breakdown of all user-defined procedure
calls. The `Calls` column contains the number of times a
user-defined procedure was called. The `Incl T(ms)` column
is the total time, in milliseconds, it took for the call to complete,
including the time spent in other user-defined procedures. The
`Excl T(ms)` column is the total time, in milliseconds,
spent within that user-defined procedure, not counting other
user-define procedures it called. The <code>Excl/calls</code> column
is an estimate of the time, in milliseconds, spent in that
user-defined procedure for each call.

Here is example output:

```
Sorted by Exclusive Time
Name                               Calls Incl T(ms) Excl T(ms) Excl/calls
CALLTHEM                              13     26.066     19.476      1.498
CALLME                                13      6.413      6.413      0.493
REPORTME                              13      0.177      0.177      0.014

Sorted by Inclusive Time
Name                               Calls Incl T(ms) Excl T(ms) Excl/calls
CALLTHEM                              13     26.066     19.476      1.498
CALLME                                13      6.413      6.413      0.493
REPORTME                              13      0.177      0.177      0.014

Sorted by Number of Calls
Name                               Calls Incl T(ms) Excl T(ms) Excl/calls
CALLTHEM                              13     26.066     19.476      1.498
```

### `profiler:data`

```NetLogo
profiler:data
```

Reports a list of lists containing the results of the profiler in a format that is suitable
for exporting with the [`csv` extension](https://docs.netlogo.org/csv.html).

The first sublist contains the name of the data columns: `procedure`, `calls`, `inclusive_time` and
`exclusive_time`. This is followed by one sublist containing the profiler data for each user-defined
procedure. The reported times are in milliseconds.

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo profiler extension is in the public domain. To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
