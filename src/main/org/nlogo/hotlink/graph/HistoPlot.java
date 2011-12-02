package org.nlogo.hotlink.graph;

import java.lang.Integer;
import org.nlogo.hotlink.main.MainWindow;
import org.nlogo.hotlink.view.ViewPanel;
import org.nlogo.app.PlotTab;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;

import java.util.*;

public class HistoPlot extends PlotWindow {
    DefaultCategoryDataset activeDataset = new DefaultCategoryDataset();
	ArrayList<DefaultCategoryDataset> tickCollection = new ArrayList<DefaultCategoryDataset>(); // visible stuff
	ArrayList<DefaultCategoryDataset> fullTickCollection = new ArrayList<DefaultCategoryDataset>(); // all stuff
	private NavigableSet<Double> categories = null; // categories from largest range recorded
	private ArrayList<Integer> pens = new ArrayList<Integer>();
	private int tick;
	private double maxFrequency;
	private boolean dirty = false;
	private ArrayList<Integer> visiblePens = new ArrayList<Integer>();

    ViewPanel viewPanel;
	//TODO: make that based on the range, eventually.
	
	// this runs first to make the bars solid with no gradient.
	static { 
        BarRenderer.setDefaultBarPainter(new StandardBarPainter()); 
    }
	
	//constructor
	HistoPlot( String name , PlotTab plotTab ,
            PenCollection penCollection , PlotPanel plotPanel , Plot plot , org.nlogo.plot.Plot netLogoPlot ) {
		super( plotTab.getAppFrame() , penCollection , plotPanel , plot , netLogoPlot );
        System.out.println("I'm in Hotlink histoPlot");
	    setOpaque( true ) ;
        this.netLogoPlot = netLogoPlot;
	    setPreferredSize( new java.awt.Dimension( 300 , 150 ) );
	    setMinimumSize( getPreferredSize() ) ;
	    setMaximumSize( getPreferredSize() ) ;
	    setName( name );
	    
	    final CategoryPlot cPlot = new CategoryPlot(activeDataset,
                new CategoryAxis(),
                new NumberAxis(),
                new StackedBarRenderer());
	    
	    chart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, cPlot, false);

	    chart.setBorderPaint(java.awt.Color.LIGHT_GRAY);
	    final StackedBarRenderer renderer = (StackedBarRenderer) cPlot.getRenderer();
	    
	    renderer.setMinimumBarLength(1);
	    renderer.setIncludeBaseInRange(true);
	    renderer.setItemMargin(0);
	    renderer.setShadowVisible(false);
	    
	    chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize( new java.awt.Dimension(300,150) );
	    setMinimumSize( getPreferredSize() ) ;
	    setMaximumSize( getPreferredSize() ) ;
	    chartPanel.addChartMouseListener( this );
	    
	    add( chartPanel );
	}
	
	public void addNewTick() {
		tickCollection.add( new DefaultCategoryDataset() );
        //System.out.println("histoplot add new tick");
	}
	
    // Ok. So, HistoPlots have to have a defined number of bars/segmentations that they will always show.
    // But the "category" of these bars may change (say you start with an x range of 10 and it
    // during the model run moves to 60). The way we deal with this is by keeping the categorical
    // data and whenever we fill the dataset (even if we are going back in time) we compare each
    // tick of that categorical data to whatever the biggest range for categories has been up to this
    // point and redraw them that way. so (1) store the collection of frequencies, (2) compare those
    // frequencies against the largest range of categories.
    public void addDataPoint( int run, int series, TreeMap<Double,Double> data ) {
        //System.out.println( "run" + run + " series " + series + " data " + data );

        TreeMap<Double, Double> newData = new TreeMap<Double,Double>();

        if( categories == null ) {
            categories = data.navigableKeySet();
        }

        if( pens.size() <= series ) {
            pens.add(series);
            visiblePens.add(series);
        }

        //System.out.println("categories");

        // if the largest category value is larger that the current one
        if( data.lastKey() > categories.last() ) {
            //System.out.println("categories equals current categories");
            categories = data.navigableKeySet(); // replace with the new higher range categories
            recastData();
            newData = data;
        } else {
            for( Double category : categories ) { // for each category, lowest to highest
                //System.out.println("cast to existing categories");
                double freq = 0;
                if( categories.higher(category) != null ) {
                    for( Double frequency : data.subMap(category , categories.higher(category) ).values() ) { // for each entry
                        freq += frequency;
                        //System.out.println("adding frequency");
                    }
                } else {
                    for( Double frequency : data.tailMap(category).values() ) { // for each entry
                        freq += frequency;
                        System.out.println("adding frequency");
                    }
                }
                newData.put(category,freq);
		        if( freq > maxFrequency ) maxFrequency = freq;
                System.out.println("put data" + category + " " + freq );
            }
        }

        System.out.println("newdata is " + newData);

        for( Map.Entry<Double,Double> entry : newData.entrySet() ) {
            System.out.println("new entry");
		    tickCollection.get(tickCollection.size()-1)
			    .addValue(entry.getValue(), (Integer) series, entry.getKey());
        }
		//if( ! categories.contains(category) ) {
		//	categories.add( category);
		//}
		//Collections.sort(categories);
		
		//if( ! rows.contains(series) ) {
		//	rows.add( series );
		//	visible.add( series );
        //    System.out.println("add series?");
		//}
		//Collections.sort(rows);

		
		dirty = true;
        rePaint();
        fillDataset();
	}

    private void recastData() {
        // recast all data to existing high range categories
        for( DefaultCategoryDataset dataset : fullTickCollection ) { // for each tick
            System.out.println("for tick in fulltickcln");
            for( Double category : categories ) { // for each current category
                System.out.println("for category " + category);
                for( int penSeries : pens ) { // for each pen
                    System.out.println("for pen " + penSeries);
                    double freq = 0.0;
                    for( Object oldCategory : dataset.getColumnKeys() ) { // for each previous category
                        if( categories.higher(category) != null ) {
                            if( (Double) oldCategory > category && (Double) oldCategory < categories.higher(category) ) { // if it should be in this one
                                freq += (Double) dataset.getValue( penSeries , (Double) oldCategory );
                            }
                        } else if( (Double) oldCategory > category ) {
                            freq += (Double) dataset.getValue( penSeries , (Double) oldCategory );
                        }
                    }
                    //dataset.addValue( freq , penSeries , category );
                }
            }
        }
    }

	private void fillDataset() {
		for( int index = 0 ; index < tickCollection.size() ; index ++ ) {
            System.out.println("filldataset index " + index );
			fullTickCollection.add( new DefaultCategoryDataset() );
			for( Integer pen : pens ) {
                System.out.println("row " + pen );
				if( visiblePens.contains(pen) ) { // tick
                    System.out.println("visiblepens" );
					for( Double column : categories ) {
                        System.out.println("column " + column );
						//if( ! tickCollection.get(index).getColumnKeys().contains(column ) ||
						//	! tickCollection.get(index).getRowKeys().contains(row ) ) {
						//	fullTickCollection.get(index).addValue(0, row, column);
						//} else {
						//	if( tickCollection.get(index).getValue( row , column ) == null ) {
						//		fullTickCollection.get(index).addValue(0, row, column);
						//	} else {
								fullTickCollection.get(index).addValue(
										tickCollection.get(index).getValue(pen, column),
										pen, column);
                                System.out.println("addValue " + pen + " " + column + " " + tickCollection.get(index).getValue(pen, column));
						//	}
						//}
					//}
				//} else {
				//	if( fullTickCollection.get(index).getRowKeys().contains( row ) ) {
				//		for( Double column : categories ) {
				//			fullTickCollection.get(index).addValue(0, row, column);
				//		}
					}
				}
			}
		}

		dirty = false;
		rePaint();
	}

	public void rePaint() {
		for( Integer series : pens ) {
			chart.getCategoryPlot().getRenderer().setSeriesPaint(
					series, 
					penCollection.getPen( (int) series).getCheckBox().getForeground() );
		}
	}
	
	private void refreshPlot() {
		if( dirty ) {
			fillDataset();
		}
		chart.getCategoryPlot().getRangeAxis().setRange(0, Math.ceil( maxFrequency * 1.05 ) );
		chart.getCategoryPlot().getRenderer();
		chart.getCategoryPlot().setDataset(fullTickCollection.get(tick));  
	}

	// Does the hiding and showing of data when people select 
	// the pen's checkbox.
	public void stateChanged( ChangeEvent e ) { 
		//chart.
		JCheckBox caller = (JCheckBox) e.getSource();
		Integer row = Integer.parseInt( caller.getName() );
		
		if( caller.isSelected() ) {
			if( ! visiblePens.contains( row ) ) {
				visiblePens.add( row );
			}
		} else {
			while( visiblePens.contains( row ) ) {
				visiblePens.remove( row );
			}
		}
		rePaint();
		fillDataset();
	}

	public void chartMouseMoved(ChartMouseEvent arg0) {

	}
	
	
	// for syncing
	public void setTick( int tick ) {
		this.tick = tick;
		refreshPlot();
	}
}