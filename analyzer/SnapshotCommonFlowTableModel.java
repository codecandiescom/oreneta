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

//import java.util.*;
//import javax.swing.table.*;
//import java.io.*;

import javax.swing.table.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;
import java.util.Date;
import java.text.*;

public class SnapshotCommonFlowTableModel extends OrenetaTableModel {
	
	Analyzer analyzer;
	CommonFlowSet cfs;
	SimpleDateFormat sdf;
	
	////////////////////////////////////////////////////////////////////////////
	SnapshotCommonFlowTableModel (CommonFlowSet cfs, Analyzer analyzer) {
		this.cfs = cfs;
		this.analyzer = analyzer;
		sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		columnNames = new String[14];
		columnNames[0] = "Src IP";
		columnNames[1] = "Src Host";
		columnNames[2] = "Src Port";
		columnNames[3] = "Src Service";
		columnNames[4] = "Dst IP";
		columnNames[5] = "Dst Host";
		columnNames[6] = "Dst Port";
		columnNames[7] = "Dst Service";
		columnNames[8] = "Last Time";
		columnNames[9] = "Creation Time";
		columnNames[10] = "Monitor";
		columnNames[11] = "Color";
		columnNames[12] = "MaxOwd";
		columnNames[13] = "MinOwd";
		
		// Which columns to show in table
		// Selectable from ColumnsGUI
		selectedColumns = new boolean[columnNames.length];
		selectedColumns[0] = false;
		selectedColumns[1] = true;
		selectedColumns[2] = false;
		selectedColumns[3] = true;
		selectedColumns[4] = false;
		selectedColumns[5] = true;
		selectedColumns[6] = false;
		selectedColumns[7] = true;
		selectedColumns[8] = false;
		selectedColumns[9] = false;
		selectedColumns[10] = true;
		selectedColumns[11] = true;
		selectedColumns[12] = false;
		selectedColumns[13] = false;
	}
	
	
  	////////////////////////////////////////////////////////////////////////////
  	public int getRowCount() {
  		return cfs.getNumCommonFlows();
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public boolean isCellEditable(int row, int col) {
		if (col == 10 || col == 11) return true;
		else return false;
	}
  	
  	////////////////////////////////////////////////////////////////////////////
	public Object getValueAt(int row, int col) {
  		try {
  			CommonFlow cf = (CommonFlow) cfs.getCommonFlowAt(row);
	  		
	  		if (cf != null) {
	  			switch (col) {
	  				case 0:	// Src IP
	  					return cf.src.toString().substring(1);
	  					
	  				case 1: // Src Host
	  					return cf.getSrc();
	
	  				case 2: // Src Port 
	  					return new Integer(cf.src_port);
	  				
	  				case 3:	// Src Service (translated port)
	  					return cf.getSrcService();
	  					
	  				case 4: // Dst IP
	  					return cf.dst.toString().substring(1);
	  					
	  				case 5: // Dst Host
	  					return cf.getDst();
	  					
	  				case 6: // Dst Port
	  					return new Integer(cf.dst_port);
	  					
	  				case 7: // Dst Service
	  					return cf.getDstService();
	  					
	  				case 8: // Last Time
	  					return new String (sdf.format(new Date ((long)cf.lastTime*1000), new StringBuffer(), new FieldPosition(0)).toString());
	  		
	  				case 9:	// Creation Time
	  					return new String (sdf.format(new Date ((long)cf.time*1000), new StringBuffer(), new FieldPosition(0)).toString());
	
	  				case 10: // Monitor
	  					return new Boolean (cf.monitor);
	  				
	  				case 11: // Color
	  					return cf.color;
	  				
	  				case 12: // Max OWD
	  					return new Double (cf.owd_global_max);
	  					
	  				case 13: // Min OWD
	  					if (cf.owd_global_min == Double.MAX_VALUE)
							return new Double (0);
						else
							return new Double (cf.owd_global_min);
	  			}
	  		} else {
	  			columnDefault(col);
	  		}
	  			  			
	  	} catch (IndexOutOfBoundsException e) {
	  		return columnDefault(col);
	  	}
  		
  		return new String("null");
  	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	Object columnDefault (int col) {
  		switch (col) {
		case 0:	// Src IP
			return new String ("null");
			
		case 1: // Src Host
			return new String ("null");

		case 2: // Src Port 
			return new Integer(0);
		
		case 3:	// Src Service (translated port)
			return new String ("null");
			
		case 4: // Dst IP
			return new String ("null");
			
		case 5: // Dst Host
			return new String ("null");
			
		case 6: // Dst Port
			return new Integer(0);
			
		case 7: // Dst Service
			return new String ("null");
			
		case 8: // Last Time
			return new String ("null");
			
		case 9:	// Creation Time
			return new String ("null");
			
		case 10: // Monitor
			return new Boolean (false);
		
		case 11: // Color
			return Color.BLACK;
		
		case 12: // Max OWD
			return new Double (0);
			
		case 13: // Min OWD
			return new Double (0);
			
		}
		
		return new String("null");
	}
  	
  	////////////////////////////////////////////////////////////////////////////
  	public void setValueAt(Object value, int row, int col) {
  		try {
  			CommonFlow cf = (CommonFlow) cfs.getCommonFlowAt(row);
	  		if (cf != null) {
	  			switch (col) {
	  				case 10:
			  			Boolean b = (Boolean) value;
			  			cf.monitor = b.booleanValue();
						if (cf.monitor)
							analyzer.drawCommonFlow(cf);
						else
							analyzer.undrawCommonFlow(cf);
							
			  			break;
			  		case 11:
			  			cf.color = (Color) value;
			  			break;
			  	}
			  	fireTableCellUpdated(row, col);
			  			
	  		}
	  	} catch (IndexOutOfBoundsException e) {}
		//fireTableCellUpdated(row, col);
  	}

}
