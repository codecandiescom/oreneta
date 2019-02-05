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

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class SnapshotFlowsGUI {
	
	GUI gui;
	
	JInternalFrame frame;
	SnapshotFlowTableModel tableModel;
	JTable flowTable;
	TableColumn [] columns;
	
	////////////////////////////////////////////////////////////////////////////
	SnapshotFlowsGUI (GUI gui) {
		this.gui = gui;
	
		create();	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void create() {
		frame = new JInternalFrame ("Snapshot Flows", true, true, true, true);
		frame.setSize (650, 250);
		frame.setLocation (5, 395);
		frame.setVisible(false);
		frame.setFrameIcon (new ImageIcon("icon/SnapshotFlows16.gif"));
		gui.desktop.add(frame);

		// Init flows table
		tableModel = new SnapshotFlowTableModel (gui.analyzer.snapshot);
		//tableSorter = new TableSorter(tableModel);
		//flowTable = new JTable(tableSorter);
		flowTable = new JTable(tableModel);
		JScrollPane scrollpane = new JScrollPane(flowTable);
		//tableSorter.addMouseListenerToHeaderInTable(flowTable);

		tableModel.setupTable(flowTable);
		
		columns = new TableColumn[flowTable.getColumnCount()];
		
		TableColumn column = null;
		for (int i = 0; i < flowTable.getColumnCount(); i++) {
		    column = flowTable.getColumnModel().getColumn(i);
		    columns[i] = column;
		    switch(i) {
		    	case 0: // Src IP
		    	case 1: // Src Host
		    	case 4: // Dst IP
		    	case 5: // Dst Host
		    		column.setPreferredWidth(400);
		    		break;
		    	case 2: // Src Port
		    	case 3: // Src Service
		    	case 6: // Dst Port
		    	case 7: // Dst Service
		    		column.setPreferredWidth(160);
		    		break;
		    	case 8: // Last Time
		    	case 9: // Creation Time
		    		column.setPreferredWidth(200);
		    		break;
		    	case 10: // Monitor
		    		column.setPreferredWidth(120);
		    		break;
		    	case 11: // Color
		    		column.setPreferredWidth(200);
		    		break;
		    	case 12: // Total Bytes
		    	case 13: // Total Packets
		    		column.setPreferredWidth(200);
		    		break;
		    		
		    	default: 
		    		column.setPreferredWidth(200);
		    		break;
		    }
		}

		frame.getContentPane().add(scrollpane);
		//frame.pack();
		frame.toFront();
		
		columnsChanged();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void show() {
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
	public void dataChanged() {
		tableModel.dataChanged();
		//tableSorter.tableChanged();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void columnsChanged() {
		for (int i=0; i<columns.length; i++) {
			flowTable.removeColumn( columns[i] );
		}
			
		for (int i=0; i<columns.length; i++) {
			if (tableModel.selectedColumns[i])
				flowTable.addColumn( columns[i] );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void flowInserted (int pos) {
		tableModel.flowInserted (pos);
	}

}
