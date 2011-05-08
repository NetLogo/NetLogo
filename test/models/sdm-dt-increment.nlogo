globals [results]
to setup
 clear-all
 set results ""
 system-dynamics-setup
 system-dynamics-do-plot
 record
end
to go
 if ticks >= 0.5 [stop]
 system-dynamics-go
 system-dynamics-do-plot
 record
end
to record
 set results (word results "t= " precision ticks 3)
 set results (word results " y= " precision y 3)
 set results (word results " y-exact= " precision (2 + 2 * ticks  + exp(ticks)) 5)
 set results word results "\n"
end
@#$#@#$#@
GRAPHICS-WINDOW
303
10
733
77
17
1
12.0
1
10
1
1
1
0
1
1
1
-17
17
-1
1
0
1

PLOT
316
87
670
324
populations
NIL
NIL
0.0
1.0
3.0
7.0
false
false
PENS
"y" 1.0 0 -16777216 true

BUTTON
45
23
111
56
NIL
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

BUTTON
148
30
248
63
NIL
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL

TEXTBOX
47
135
258
303
                  \nUsing\n\n          f = y - 2 * (system-dynamics-t)\n\nSimulate:\n\ndy/dt = y - 2 * t,\ny(0) = 3\n\nExact solution:\ny-exact = 2 + 2*t + exp(t)
11
0.0

OUTPUT
34
75
274
129

@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
NetLogo 4.0pre9
@#$#@#$#@
@#$#@#$#@
0.1 
    org.nlogo.aggregate.gui.AggregateDrawing 3 
        org.nlogo.aggregate.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 346 159 60 40 
            org.nlogo.aggregate.gui.WrappedStock "y" "3" 0   
        org.nlogo.aggregate.gui.ReservoirFigure "attributes" "attributes" 1 "FillColor" "Color" 192 192 192 194 159 30 30  
        org.nlogo.aggregate.gui.RateConnection 2 224 174 334 177 NULL NULL 0 0 0 
            org.jhotdraw.figures.ChopEllipseConnector REF 3  
            org.jhotdraw.standard.ChopBoxConnector REF 1  
            org.nlogo.aggregate.gui.WrappedRate "( y - 2 * ticks )" "f" 
                org.nlogo.aggregate.gui.WrappedReservoir  REF 2 0    
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
