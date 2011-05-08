to setup
  system-dynamics-setup
end

to go
  system-dynamics-go
end
@#$#@#$#@
GRAPHICS-WINDOW
210
10
649
470
16
16
13.0
1
10
1
1
1
0
1
1
1
-16
16
-16
16
0
0
1
ticks

@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
NetLogo 4.1RC1
@#$#@#$#@
@#$#@#$#@
2.0
    org.nlogo.sdm.gui.AggregateDrawing 3
        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 388 270 60 40
            org.nlogo.sdm.gui.WrappedStock "stock" "1" 0
        org.nlogo.sdm.gui.ReservoirFigure "attributes" "attributes" 1 "FillColor" "Color" 192 192 192 81 274 30 30
        org.nlogo.sdm.gui.RateConnection 3 111 289 243 289 376 289 NULL NULL 0 0 0
            org.jhotdraw.figures.ChopEllipseConnector REF 3
            org.jhotdraw.standard.ChopBoxConnector REF 1
            org.nlogo.sdm.gui.WrappedRate "stock + 3" "inflow"
                org.nlogo.sdm.gui.WrappedReservoir  REF 2 0
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
0
@#$#@#$#@
