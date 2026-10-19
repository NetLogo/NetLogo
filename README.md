
# NetLogo `rnd` Extension

The `rnd` extension comes bundled with NetLogo 6.0 and later. You can find versions the latest version of `rnd` as well as archives of releases compatible with older NetLogo versions [in the project's Github releases page here](https://github.com/NetLogo/Rnd-Extension/releases). Just unzip the file under NetLogo's `extensions/` folder.

This extension adds the ability to do [roulette wheel selection](https://en.wikipedia.org/wiki/Fitness_proportionate_selection) in NetLogo. It provides a simpler way to accomplish the same thing as the [Lottery Example](https://github.com/NetLogo/models/blob/master/Code%20Examples/Lottery%20Example.nlogo) from the NetLogo Models Library.

## Usage

Which primitive to use depends on whether you want to select an item from a list or from an agenset. It also depends on whether you want one or many items and, if you want many, if repeats are allowed or not. The following table summarizes the situation:

| | From an AgentSet | From a List |
|---|---|---|
| One item | [`rnd:weighted-one-of`](#rndweighted-one-of) | [`rnd:weighted-one-of-list`](#rndweighted-one-of-list) |
| Many items, without repeats | [`rnd:weighted-n-of`](#rndweighted-n-of) | [`rnd:weighted-n-of-list`](#rndweighted-n-of-list) |
| Many items, with repeats | [`rnd:weighted-n-of-with-repeats`](#rndweighted-n-of-with-repeats) | [`rnd:weighted-n-of-list-with-repeats`](#rndweighted-n-of-list-with-repeats) |

(**Note:** the initial version of the extension had a single set of primitives for both lists and agentsets, but it turned out to be confusing, so we changed it. If you were using the old version of the extension, you will need to modify your code to use the new primitives.)

In all cases, you will need to provide two things to the primitive:

- The "candidates": the items that the primitive will select from.
- The "weight": how likely it is for each candidate to be selected.

If you want to select more than one items, you will also need to tell it:

- How many items to select.

## A note about performance

The extension uses Keith Schwarz's implementation of Vose's Alias Method (see Schwarz's [Darts, Dice, and Coins](http://www.keithschwarz.com/darts-dice-coins/) page). Assuming you are choosing _n_ candidates for a collection of size _m_ **with repeats**, this method has an initialization cost of _O(m)_ followed by a cost of _O(1)_ for each item you pick, so _O(m + n)_ overall.

For example, in the following code:

    let candidates n-values 500 [ [n] -> n ]
    rnd:weighted-n-of-list-with-repeats 100 candidates [ [w] -> w ]
    n-values 100 [ rnd:weighted-one-of-list candidates [ [w] -> w ] ]

...the line using `rnd:weighted-n-of-list-with-repeats` will likely run 100 times faster than the line using a combination of `n-values` and `rnd:weighted-one-of-list`. This is because `rnd:weighted-n-of-list-with-repeats` only initializes the algorithm once and `rnd:weighted-one-of` does it each time it is called.

(Note that composing `n-values` with `rnd:weighted-one-of-list` does not preserve the order of the original candidate list, while `rnd:weighted-n-of-list-with-repeats` does.)

Things are a bit more complicated if you are choosing **without repeats**, however. In this case, the algorithm may have to discard some picks because the candidates have already been selected. When this starts happening too often (maybe because some weights are much bigger than others), the extension re-initializes the algorithm with the already-picked candidates excluded. This should not happen too often, however, so while picking without repeats has an upper bound of _O(m * n)_ in theory, it should usually not be much more than _O(m + n)_ in practice.

The previous remarks apply to agentset primitives as much as they apply to list primitives.

## Primitives

[`rnd:weighted-one-of`](#rndweighted-one-of)
[`rnd:weighted-n-of`](#rndweighted-n-of)
[`rnd:weighted-n-of-with-repeats`](#rndweighted-n-of-with-repeats)
[`rnd:weighted-one-of-list`](#rndweighted-one-of-list)
[`rnd:weighted-n-of-list`](#rndweighted-n-of-list)
[`rnd:weighted-n-of-list-with-repeats`](#rndweighted-n-of-list-with-repeats)


#### `rnd:weighted-one-of`

> <tt>rnd:weighted-one-of <i>agentset</i> <i>reporter</i></tt>


Reports a random agent from <tt><i>agentset</i></tt>.

The probability of each agent being picked is proportional to the weight given by the <tt><i>reporter</i></tt> for that agent. The weights must not be negative.

If the agentset is empty, it reports [`nobody`](https://docs.netlogo.org/dictionary.html#nobody).

Here is a full rewrite of the **Lottery Example** model using the `rnd:weighted-one-of` primitive:

```
extensions [ rnd ]

to setup
  clear-all
  ; create a turtle on every fifth patch
  ask patches with [ pxcor mod 5 = 0 and pycor mod 5 = 0 ] [
    sprout 1 [
      set size 2 + random 6 ; vary the size of the turtles
      set label 0           ; start them out with no wins
      set color color - 2   ; make turtles darker so the labels stand out
    ]
  ]
  reset-ticks
end

to go
  ask rnd:weighted-one-of turtles [ size ] [
    set label label + 1
  ]
  tick
end
```



***


#### `rnd:weighted-n-of`

> <tt>rnd:weighted-n-of <i>size</i> <i>agentset</i> <i>[ reporter ]</i></tt>


Reports an agentset of the given <tt><i>size</i></tt> randomly chosen from the <tt><i>agentset</i></tt>, with no repeats.

The probability of each agent being picked is proportional to the weight given by the <tt><i>reporter</i></tt> for that agent. The weights must be non-negative numbers.

It is an error for <tt><i>size</i></tt> to be greater than the size of the <tt><i>agentset</i></tt>.

If, at some point during the selection, there remains only candidates with a weight of `0.0`, they all have an equal probability of getting picked.



***


#### `rnd:weighted-n-of-with-repeats`

> <tt>rnd:weighted-n-of-with-repeats <i>size</i> <i>agentset</i> <i>[ reporter ]</i></tt>


Reports a **list** of the given <tt><i>size</i></tt> randomly chosen from the <tt><i>agentset</i></tt>, with repeats. (Why a list instead of an agentset? Because an agentset cannot contain the same agent more than once.)

The probability of each agent being picked is proportional to the weight given by the <tt><i>reporter</i></tt> for that agent. The weights must be non-negative numbers.

It is **not** an error for <tt><i>size</i></tt> to be greater than the size of the <tt><i>agentset</i></tt>, but there has to be at least one candidate.

If, at some point during the selection, there remains only candidates with a weight of `0.0`, they all have an equal probability of getting picked.

If all weights are `0.0`, each candidate has an equal probability of being picked.



***


#### `rnd:weighted-one-of-list`

> <tt>rnd:weighted-one-of-list <i>list</i> <i>arrow-reporter</i></tt>


Reports a random item from <tt><i>list</i></tt>.

The probability of each item being picked is proportional to the weight given by the <tt><i>arrow-reporter</i></tt> for that item. The weights must not be negative. The first argument passed to the arrow procedure refers to the list item. (See the [Arrow Procedures section](https://docs.netlogo.org/programming.html#arrow-procedures) of the Programming Guide for more details.)

It is an error for the list to be empty.

A common way to use the primitive is to have a list of lists, where the first item of each sublist is the thing you want to choose and the second item is the weight. Here is a short example:

```
let pairs [ [ "A" 0.2 ] [ "B" 0.8 ] ]
repeat 25 [
  ; report the first item of the pair selected using
  ; the second item (i.e., `last p`) as the weight
  type first rnd:weighted-one-of-list pairs [ [p] -> last p ]
]
```

This should print `B` roughly four times more often than it prints `A`.

If you happen to have your items and your weights in two separate lists, you can combine them into pairs by using a combination of [`map`](https://docs.netlogo.org/dictionary.html#map) and [`list`](https://docs.netlogo.org/dictionary.html#list):

```
let items [ "A" "B" "C" ]
let weights [ 0.1 0.2 0.7 ]
let pairs (map list items weights)
```

Since we apply [`map`](https://docs.netlogo.org/dictionary.html#map) to both the `items` list and the `weights` list, the parentheses are needed in `(map list items weights)`. We also use the concise arrow procedure syntax (see the [programming guide](https://docs.netlogo.org/programming.html#arrow-procedures)) to pass [`list`](https://docs.netlogo.org/dictionary.html#list) as the reporter for [`map`](https://docs.netlogo.org/dictionary.html#map). The same thing could have been written `(map [ [a b] -> list a b ] items weights)`.



***


#### `rnd:weighted-n-of-list`

> <tt>rnd:weighted-n-of-list <i>size</i> <i>list</i> <i>arrow-reporter</i></tt>


Reports a list of the given <tt><i>size</i></tt> randomly chosen from the <tt><i>list</i></tt> of candidates, with no repeats.

The probability of each item being picked is proportional to the weight given by the <tt><i>arrow-reporter</i></tt> for that item. The weights must not be negative. The first argument passed to the arrow procedure refers to the list item. (See the [Arrow Procedures section](https://docs.netlogo.org/programming.html#arrow-procedures) of the Programming Guide for more details.)

It is an error for <tt><i>size</i></tt> to be greater than the size of the <tt><i>list</i> of candidates</tt>.

If, at some point during the selection, there remains only candidates with a weight of `0.0`, they all have an equal probability of getting picked.

The items in the resulting list appear in the same order that they appeared in the list of candidates. (If you want them in random order, use [`shuffle`](https://docs.netlogo.org/dictionary.html#shuffle) on the result).

Example:
```
let candidates n-values 8 [ [n] -> 2 ^ (n + 1) ] ; make a list with the powers of two
print rnd:weighted-n-of-list 4 candidates [ [w] -> w ]
```

This should print a list of four numbers, where the bigger numbers (32, 64, 128, 256) have a much better chance to show up than the smaller ones (2, 4, 8, 16).



***


#### `rnd:weighted-n-of-list-with-repeats`

> <tt>rnd:weighted-n-of-list-with-repeats <i>size</i> <i>list</i> <i>arrow-reporter</i></tt>


Reports a list of the given <tt><i>size</i></tt> randomly chosen from the <tt><i>list</i></tt> of candidates, with repeats.

The probability of each item being picked is proportional to the weight given by the <tt><i>arrow-reporter</i></tt> for that item. The weights must not be negative. The first argument passed to the arrow procedure refers to the list item. (See the [Arrow Procedures section](https://docs.netlogo.org/programming.html#arrow-procedures) of the Programming Guide for more details.)

It is **not** an error for <tt><i>size</i></tt> to be greater than the size of the <tt><i>list</i></tt> of candidates, but there has to be at least one candidate.

If, at some point during the selection, there remains only candidates with a weight of `0.0`, they all have an equal probability of getting picked.

If all weights are `0.0`, each candidate has an equal probability of being picked.

The items in the resulting list appear in the same order that they appeared in the list of candidates. (If you want them in random order, use [`shuffle`](http://docs.netlogo.org/dictionary.html#shuffle) on the result).

Example:
```
let pairs [ [ "A" 0.2 ] [ "B" 0.8 ] ]
print map first rnd:weighted-n-of-list-with-repeats 25 pairs [ [p] -> last p ]
```

This should print a list of 25 `A`s and `B`s, with roughly four times as many `B`s than `A`s.



***


## Building

If you want to build the extension from the source, it should be sufficient to run `sbt package` from the extension's source directory. If the build succeeds, `rnd.jar` will be created.
## Credits

Authored by Nicolas Payette.

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo Rnd extension is in the public domain. To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
