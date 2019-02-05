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

public class ChartNeg extends Chart {
	
  ////////////////////////////////////////////////////////////////////////////
  public ChartNeg (GUI gui, String title, int param, String units) {
  	super(gui, title, param, units);
  }

  ////////////////////////////////////////////////////////////////////////////
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    
    int width  = getSize().width;
    int height = getSize().height;
    
    //AlphaComposite ac_full = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1 );
    //AlphaComposite ac_semi = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float).5);

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
		/*
	    g2.setColor (Color.WHITE);
	    g2.drawString (""+max_value, 5, um+13);
	    g2.drawString (""+max_value*3/4, 5, um + 5 + (int)((float)(height-dm-um)/4.0));
	    g2.drawString (""+max_value/2, 5, um + 5 + (int)((float)(height-dm-um)/2.0));
	    g2.drawString (""+max_value/4, 5, um + 5 + (int)(3.0*(float)(height-dm-um)/4.0));
		*/

		// Paint data
		if (max_value == 0.0) max_value=1; // prevent division by 0
		
		li = models.listIterator(0);
		int x1, y1, x2, y2, y_max, y_min;
		while (li.hasNext()) {
			cm = (ChartModel) li.next();

			g2.setColor (cm.getColor());

			x1 = lm;
			y1 = height/2 + um - (int)((cm.getValue(0,param,0) / max_value) * ((double)(height - dm - um) / 2));

			for (int j=1; j<horzDiv; j++) {
				double aux;
				x2 = x1 + scale;
				//x2 = x1 + (int)((float)(width - lm - rm) / (float)horzDiv);
				y2 = height/2 + um - (int)((cm.getValue(j,param,0) / max_value) * ((double)(height - dm - um) / 2));
				//y_max = height - dm - (int)(cm.getValue(j,param,1) / max_value * (double)(height - dm - um) );
				//aux = cm.getValue(j,param,2);
				//if (aux == Double.MAX_VALUE) y_min = 0;
				//else y_min = height - dm - (int)( aux / max_value * (double)(height - dm - um) );
				
				//g2.setComposite(ac_semi);
				//g2.setColor(Color.WHITE);
				//if (y_max < y_min)
				//	g2.fillRect(x1,y_max,scale,y_min-y_max);
				//g2.setComposite(ac_full);
				//g2.setColor (cm.getColor());
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
	    g2.drawString (""+nf.format(redux(max_value)/2), 5, um + 5 + (int)((float)(height-dm-um)/4.0));
	    g2.drawString ("0", 5, um + 5 + (int)((float)(height-dm-um)/2.0));
	    g2.drawString ("-"+nf.format(redux(max_value)/2), 5, um + 5 + (int)(3.0*(float)(height-dm-um)/4.0));
	    g2.drawString ("-"+nf.format(redux(max_value)), 5, height-dm-2);

		g2.drawString (prefix(max_value)+units, 35, height - dm - 25);
	}
  
}

