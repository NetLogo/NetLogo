## Usage

The first thing that one needs to understand in order to work with the network extension is how to tell the extension _which_ network to work with. Consider the following example situation:

```
breed [ bankers banker ]
breed [ clients client ]

undirected-link-breed [ friendships friendship ]
directed-link-breed [ accounts account ]
```

Basically, you have bankers and clients. Clients can have accounts with bankers. Bankers can probably have account with other bankers, and anyone can be friends with anyone.

Now we might want to consider this whole thing as one big network. If that is the case, there is nothing special to do: by default, the NW extension primitives consider all turtles and all links to be part of the current network.

We could also, however, be only interested in a subset of the network. Maybe we want to consider only friendship relations. Furthermore, maybe we want to consider only the friendships _between bankers_. After all, having a very high centrality in a network of banker friendships is very different from having a high centrality in a network of client friendships.

To specify such networks, we need to tell the extension _both_ which turtles _and_ which links we are interested in. All the turtles from the specified set of turtles will be included in the network, and only the links from the specified set of links that are between turtles of the specified set will be included. For example, if you ask for `bankers` and `friendships`, even the lonely bankers with no friends will be included, but friendship links between bankers and clients will **not** be included. The way to tell the extension about this is with the [`nw:set-context`](/nw/set-context) primitive, which you must call _prior_ to doing any operations on a network.

Some examples:

- `nw:set-context turtles links` will give you everything: bankers and clients, friendships and accounts, as one big network.
- `nw:set-context turtles friendships` will give you all the bankers and clients and friendships between any of them.
- `nw:set-context bankers friendships` will give you all the bankers, and only friendships between bankers.
- `nw:set-context bankers links` will give you all the bankers, and any links between them, whether these links are friendships or accounts.
- `nw:set-context clients accounts` will give you all the clients, and accounts between each other, but since in our fictional example clients can only have accounts with bankers, this will be a completely disconnected network.

### Special agentsets vs normal agentsets

It must be noted that NetLogo has two types of agentsets that behave slightly differently, and that this has an impact on the way `nw:set-context` works. We will say a few words about these concepts here but, for a thorough understanding, it is highly recommended that you read [the section on agentsets in the NetLogo programming guide](http://ccl.northwestern.edu/netlogo/docs/programming.html#agentsets).

The "special" agentsets in NetLogo are `turtles`, `links` and the different "breed" agentsets. What is special about them is that they can grow: if you create a new turtle, it will be added to the `turtles` agentset. If you have a `bankers` breed and you create a new banker, it will be added to the `bankers` agentset and to the `turtles` agentset. Same goes for links. Other agentsets, such as those created with the `with` primitive (e.g., `turtles with [ color = red ]`) or the `turtle-set` and `link-set` primitives) are never added to. The content of normal agentsets will only change if the agents that they contain die.

To show how different types of agentsets interact with [`nw:set-context`](/nw/set-context), let's create a very simple network:

```NetLogo
clear-all
create-turtles 3 [ create-links-with other turtles ]
```

Let's set the context to `turtles` and `links` (which is the default anyway) and use [`nw:get-context`](/nw/get-context) to see what we have:

```NetLogo
nw:set-context turtles links
show map sort nw:get-context
```

 We get all three turtles and all three links:

```NetLogo
[[(turtle 0) (turtle 1) (turtle 2)] [(link 0 1) (link 0 2) (link 1 2)]]
```

Now let's kill one turtle:

```NetLogo
ask one-of turtles [ die ]
show map sort nw:get-context
```

As expected, the context is updated to reflect the death of the turtle and of the two links that died with it:

```NetLogo
[[(turtle 0) (turtle 1)] [(link 0 1)]]
```

What if we now create a new turtle?

```NetLogo
create-turtles 1
show map sort nw:get-context
```

Since our context is using the special `turtles` agentset, the new turtle is automatically added:

```NetLogo
[[(turtle 0) (turtle 1) (turtle 3)] [(link 0 1)]]
```

Now let's demonstrate how it works with normal agentsets. We start over with a new network of red turtles:

```NetLogo
clear-all
create-turtles 3 [
  create-links-with other turtles
  set color red
]
```

And we set the context to `turtles with [ color = red ])` and `links`

```NetLogo
nw:set-context (turtles with [ color = red ]) links
show map sort nw:get-context
```

Since all turtles are red, we get everything in our context:

```NetLogo
[[(turtle 0) (turtle 1) (turtle 2)] [(link 0 1) (link 0 2) (link 1 2)]]
```

But what if we ask one of them to turn blue?

```NetLogo
ask one-of turtles [ set color blue ]
show map sort nw:get-context
```

No change. The agentset used in our context remains unaffected:

```NetLogo
[[(turtle 0) (turtle 1) (turtle 2)] [(link 0 1) (link 0 2) (link 1 2)]]
```

If we kill one of them, however...

```NetLogo
ask one-of turtles [ die ]
show map sort nw:get-context
```

It gets removed from the set:

```NetLogo
[[(turtle 0) (turtle 2)] [(link 0 2)]]
```

What if we add a new red turtle?

```NetLogo
create-turtles 1 [ set color red ]
show map sort nw:get-context
```

Nope:

```NetLogo
[[(turtle 0) (turtle 2)] [(link 0 2)]]
```

## A note regarding floating point calculations

Neither [JGraphT](https://github.com/jgrapht) nor [Jung](http://jung.sourceforge.net/), the two network libraries that we use internally, use [`strictfp` floating point calculations](https://en.wikipedia.org/wiki/Strictfp). This does mean that exact reproducibility of results involving floating point calculations _between different hardware architectures_ is not fully guaranteed. (NetLogo itself [always uses strict math](http://ccl.northwestern.edu/netlogo/docs/faq.html#are-netlogo-models-runs-scientifically-reproducible) so this only applies to some primitives of the NW extension.)

## Performance

In order to be fast in as many circumstances as possible, the NW extension tries hard to never calculate things twice. It remembers all paths, distances, and centralities that it calculates. So, while the first time you ask for the distance between `turtle 0` and `turtle 3782` may take some time, after that, it should be almost instantaneous. Furthermore, it keeps track of values it just happened to calculate along the way. For example, if `turtle 297` is closer to `turtle 0` than `turtle 3782` is, it may just happen to figure out the distance between `turtle 0` and `turtle 297` while it figures out the distance between `turtle 0` and `turtle 3782`. It will remember this value, so that if you ask it for the distance between `turtle 0` and `turtle 297`, it doesn't have to do all that work again.

There are a few circumstances where the NW extension has to forget things. If the network changes at all (you add turtles or links, or remove turtles or links), it has to forget everything. For weighted primitives, if the value of the weight variable changes for any of the links in the network, it will forget the values associated with that weight variable.

If you're working on a network that can change regularly, try to do all your network calculations at once, then all your network changes at once. The more your interweave network calculations and network changes, the more the NW extension will have to recalculate things. For example, if you have a traffic model, and cars need to figure out the shortest path to their destination based on the traffic each tick, have all the cars find their shortest paths, then change the network weights to account for how traffic has changed.

There may be rare occasions in which you don't want the NW extension to remember values. For example, if you're working on an extremely large network, remembering all those values may take more memory than you have. In that case, you can just call `nw:set-context (first nw:get-context) (last nw:get-context)` to force the NW extension to immediately forget everything.
