/*

	This file is part of ORENETA.

    ORENETA is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    ORENETA is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ORENETA; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.text.*;

public class Chart extends JComponent implements ChartListenerInterface {
	
  // Constants
  public final static int THROUGHPUT = 0;
  public final static int PPS = 1;
  public final static int IPD = 2;
  public final static int PSIZE = 3;
  public final static int PSIZE_MAXMIN = 4;
  public final static int IPD_MAXMIN = 5;
  	
  public final static int OWD = 0;
  public final static int OWD_MAXMIN = 1;
  public final static int PLOSS = 2;
  public final static int IPDV = 3;
  public final static int IPDV_MAXMIN = 4;
  
  JInternalFrame frame;
  GUI gui;
  
  int scale;
  String title;
  
  // Data models to paint in the chart
  LinkedList models;
  int num_models;
  
  // Wich parameter to paint in chart? (throughput, ipdv, owd, ...)
  int param;
  
  // Inner margins
  int lm, rm, um, dm;	// left, right, up and down margins
  
  String units;
  NumberFormat nf;
  

  ////////////////////////////////////////////////////////////////////////////
  public Chart(GUI gui, String title, int param, String units) {
  	this.gui = gui;
  	this.title = title;
  	this.param = param;
  	
  	setDoubleBuffered(true);
	
	// Init data models
	models = new LinkedList();

	// Init margins
	lm = 60;
	rm = 0;
	um = 0;
	dm = 1;
	
	scale = 2; // two pixels per data point
	
	this.units = units;
	
	nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(2);
	
	create();
  }
  
  ////////////////////////////////////////////////////////////////////////////
  public void create () {
  	
  	// Init frame
	//frame = new JInternalFrame ("Chart "+chart_number, true, true, true, true);
	frame = new JInternalFrame (title, true, true, true, true);
	frame.setSize (lm+(Oreneta.MONITOR_LENGTH*scale)+11, 250);
	//frame.setLocation (300-(chart_number*20), 150-(chart_number*20));
	frame.setVisible(false);
	frame.getContentPane().add(this);
	frame.setMaximizable(false);
	frame.setResizable(false);
	frame.setFrameIcon (new ImageIcon("icon/Chart16.gif"));
	frame.setLocation (5, 135);
	gui.desktop.add(frame);
	frame.toFront();
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // we use 'Show' besides 'show' because the second is deprected in
  // java.awt.Component
  public void Show() {
  	if (frame == null || frame.isClosed()) {
  		create();
  		frame.setVisible(true);
  	} else {
  		frame.setVisible(true);
  		try { frame.setIcon(false); } catch (java.beans.PropertyVetoException e) {}
  		frame.toFront();
  	}
  }

  ////////////////////////////////////////////////////////////////////////////
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    int tick = 10;
    
    int width  = getSize().width;
    int height = getSize().height;

    // Fill background
    g2.setColor (Color.BLACK);
    g2.fillRect (0,0,width,height);
    
    // Draw grid
    g2.setColor ((Color.GREEN).darker());
    
    // Horizontal
    for (float i=um; (int)i<=(height-dm-um); i+=(float)(height-um-dm)/4.0) {
    	g2.drawLine (0 + lm, (int)i, lm+(Oreneta.MONITOR_LENGTH*scale), (int)i);
    }
    
    // Vertical
    for (int i=lm; i<=(Oreneta.MONITOR_LENGTH*scale)+lm; i+=(Oreneta.MONITOR_LENGTH*scale)/10) {
    	g2.drawLine (i, 0 + um, i, height - dm);
    }
    
    // Is there any data model to draw?
	if (models.size() > 0) {

		// Calculate max value to paint in all the models
		// and the number of horizontal divisions
		double max_value=0;
		int horzDiv = 0;
		ChartModel cm;
		ListIterator li = models.listIterator(0);
		while (li.hasNext()) {
			cm = (ChartModel) li.next();
			if (cm.getMaxValue(param) > max_value)
				max_value = cm.getMaxValue(param);
			if (horzDiv == 0)
				horzDiv = cm.getNumHorzDiv();
		}
		
		// Draw max value
		drawUnits(g2, max_value, height);

		// Paint data
		if (max_value == 0.0) max_value=1; // prevent division by 0
		
		li = models.listIterator(0);
		int x1, y1, x2, y2;
		while (li.hasNext()) {
			cm = (ChartModel) li.next();

			g2.setColor (cm.getColor());

			x1 = lm;
			y1 = height - dm - (int)((cm.getValue(0,param,0) / max_value) * (double)(height - dm - um));

			for (int j=1; j<horzDiv; j++) {
				x2 = x1 + scale;
				//x2 = x1 + (int)((float)(width - lm - rm) / (float)horzDiv);
				y2 = height - dm - (int)(cm.getValue(j,param,0) / max_value * (double)(height - dm - um) );

				g2.drawLine (x1, y1, x2, y2);
				//else draw bar

				x1 = x2;
				y1 = y2;
			}

		}
	
	}

	// Draw box grid
	// Horizontal
	g2.setColor ((Color.GREEN).darker());
   	g2.drawLine (0 + lm, height-dm, lm+(Oreneta.MONITOR_LENGTH*scale), height-dm);

  }

	////////////////////////////////////////////////////////////////////////////
	void drawUnits (Graphics2D g2, double max_value, int height) {
		g2.setColor (Color.WHITE);
	    g2.drawString (""+nf.format(redux(max_value)), 5, um+13);
	    g2.drawString (""+nf.format(redux(max_value)*3/4), 5, um + 5 + (int)((float)(height-dm-um)/4.0));
	    g2.drawString (""+nf.format(redux(max_value)/2), 5, um + 5 + (int)((float)(height-dm-um)/2.0));
	    g2.drawString (""+nf.format(redux(max_value)/4), 5, um + 5 + (int)(3.0*(float)(height-dm-um)/4.0));

		g2.drawString (prefix(max_value)+units, 5, height - dm - 15);
	}
	
	////////////////////////////////////////////////////////////////////////////
	double redux (double value) {
		if (value >= 1000000) return value/1000000;
		if (value >= 1000) return value/1000;
		if (value >= 1) return value;
		if (value >= 0.001) return value*1000;
		if (value >= 0.000001) return value*1000000;
		return value;
	}

	////////////////////////////////////////////////////////////////////////////
	String prefix (double value) {
		if (value >= 1000000) return "M";
		if (value >= 1000) return "K";
		if (value >= 1) return "";
		if (value >= 0.001) return "m";
		if (value >= 0.000001) return "u";
		return "";
	}
  
  ////////////////////////////////////////////////////////////////////////////
  public Dimension getPreferredSize() {
    return new Dimension(700, 300);
  }

  ////////////////////////////////////////////////////////////////////////////
  public void addDataModel (ChartModel cm) {
  	models.add(cm);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  public void removeDataModel (ChartModel cm) {
  	ChartModel aux;
    	ListIterator li = models.listIterator();
    	
	while (li.hasNext()) {
		aux = (ChartModel) li.next();
		if (aux.extraEquals(cm))
			li.remove();
	}
    //models.remove(cm);
  }

  ////////////////////////////////////////////////////////////////////////////
  public void addChartListener(ChartListener listener) {
    listenerList.add( ChartListener.class, listener );
  }
  
  ////////////////////////////////////////////////////////////////////////////
  public void removeChartListener(ChartListener listener) {
    listenerList.remove( ChartListener.class, listener );
  }

  ////////////////////////////////////////////////////////////////////////////
  void fireEvent() {
    Object[] listeners = listenerList.getListenerList();
    for ( int i = 0; i < listeners.length; i += 2 )
      if ( listeners[i] == ChartListenerInterface.class )
        ((ChartListenerInterface)listeners[i + 1]).chartAdjusted(
          new ChartEvent(this, 0) );
  }
  
  ////////////////////////////////////////////////////////////////////////////
  public void repaint () {
  	int width  = getSize().width;
    int height = getSize().height;
    repaint (0, 0, 0, width, height);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  public boolean isClosed() {
  	return frame.isClosed();
  }
  
  
  ////////////////////////////////////////////////////////////////////////////
  // Listener methods
  ////////////////////////////////////////////////////////////////////////////
    
  ////////////////////////////////////////////////////////////////////////////
  public void chartAdjusted( ChartEvent e ) {
		// nothing
	}

	/*
	addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) { spin(e); }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) { spin(e); }
    */

  
}

