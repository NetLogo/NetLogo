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
