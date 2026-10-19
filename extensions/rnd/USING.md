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
