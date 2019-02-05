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


// User interface - Graphical

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class GUI {

	String prompt = "Oreneta> ";
	public Analyzer analyzer;
	boolean cont;
	
	// Main frame
	public JFrame frame;
	
	// Meters
	MeterGUI [] meter_gui;  // View - Controller

	// Flows
	public FlowsGUI [] flows;
	//JInternalFrame [] flowFrame;
	//FlowTableModel [] tableModel;
	//TableSorter [] tableSorter;
	//JTable [] flowTable;
	
	// Common flows
	CommonFlowGUI common_flow_gui; // View - Controller
	
	// Charts
	public Chart chart_throughput;
	public Chart chart_pps;
	public Chart chart_ipd;
	public ChartMaxMin chart_ipd_maxmin;
	public Chart chart_psize;
	public ChartMaxMin chart_psize_maxmin;
	public Chart chart_owd;
	public ChartMaxMin chart_owd_maxmin;
	public ChartNeg chart_ipdv;
	public ChartNegMaxMin chart_ipdv_maxmin;
	public Chart chart_ploss;
	//Chart [] chart;	// View - Controller
	
	// Console
	ConsoleGUI console;	// View - Controller
	
	// Snapshot
	SnapshotFlowsGUI snapshot_flows;
	SnapshotCommonFlowGUI snapshot_common;
	
	// Menu
	MenuGUI menu;
	
	// File chooser
	JFileChooser fc;
	
	// Preferences
	PreferencesGUI prefs;
	
	// Column chooser
	ColumnsGUI columns;
	
	// Other window objects
	public JDesktopPane desktop;
	//JWindow splashWindow;
	
	// Timer
	javax.swing.Timer timer;
	public int timers;

	
	// // Methods \\ \\
	
	////////////////////////////////////////////////////////////////////////////
	public void welcome () {
		SplashWindow3 sw = new SplashWindow3("oreneta.splash.gif", null, 5000);
	}

	////////////////////////////////////////////////////////////////////////////
	private void prompt () {
		System.out.print(prompt);
	}
	
	////////////////////////////////////////////////////////////////////////////
	GUI () {
		// Init meter arrays
		meter_gui = new MeterGUI[Oreneta.NUM_METERS];
		
		// Init flow arrays
		flows = new FlowsGUI[Oreneta.NUM_METERS];
		
		//flowFrame = new JInternalFrame[Oreneta.NUM_METERS];
		//tableModel = new FlowTableModel[Oreneta.NUM_METERS];
		//tableSorter = new TableSorter[Oreneta.NUM_METERS];
		//flowTable = new JTable[Oreneta.NUM_METERS];
		
		// Init chart arrays
		//chart = new Chart[Oreneta.NUM_METERS];
		
		// Init timer
		timer = new javax.swing.Timer (1000, new TimeStep(this));
		timers = 0;
	}

	////////////////////////////////////////////////////////////////////////////
	public void start () {
		
		// Init analyzer
		analyzer = new Analyzer(this);
		
		// Main frame
		//JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame("Oreneta");
		desktop = new JDesktopPane();
		
		// Menu Bar
	    	menu = new MenuGUI (this);
				
		// Console frame
		console = new ConsoleGUI (this);
		
		// Preferences
        	prefs = new PreferencesGUI (this);
        	
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			
			// Create meter and its gui representation
			meter_gui[i] = new MeterGUI (this, analyzer.meter[i]);    // View - Controller
			
			// Flows
			flows[i] = new FlowsGUI (this, i);	
		}
		
		// Flow Charts
		chart_throughput = new Chart(this, "Throughput", Chart.THROUGHPUT, "bps");
		chart_pps = new Chart(this, "Packets per second", Chart.PPS, "pps");
		chart_ipd = new Chart(this, "IPD", Chart.IPD, "s");
		chart_ipd_maxmin = new ChartMaxMin(this, "IPD MaxMin",Chart.IPD_MAXMIN,"s");
		chart_psize = new Chart(this, "Packet Size",Chart.PSIZE, "Bytes");
		chart_psize_maxmin = new ChartMaxMin(this, "Packet Size MaxMin",Chart.PSIZE_MAXMIN, "Bytes");
		
		// Common Flow Charts
		chart_owd = new Chart (this, "One Way Delay", Chart.OWD, "s");
		chart_owd_maxmin = new ChartMaxMin(this, "One Way Delay MaxMin",Chart.OWD_MAXMIN, "s");
		chart_ipdv = new ChartNeg(this, "IP Delay Variation",Chart.IPDV, "s");
		chart_ipdv_maxmin = new ChartNegMaxMin(this, "IP Delay Variation MaxMin",Chart.IPDV_MAXMIN, "s");
		chart_ploss = new Chart (this, "Packet Loss", Chart.PLOSS, "pps");
		
		// Common flows
		common_flow_gui = new CommonFlowGUI (this);
		
		// Snapshot
		snapshot_flows = new SnapshotFlowsGUI (this);
		snapshot_common = new SnapshotCommonFlowGUI (this);
		
		// Column chooser
        	columns = new ColumnsGUI (this);
				
		// Desktop
		frame.setSize(1024, 768);
		//frame.setMaximizedBounds (new Rectangle(0,0,1024,768));
		
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane(desktop);
		frame.setVisible(true);
		frame.setExtendedState(frame.MAXIMIZED_BOTH);
		// Create a file chooser
        	fc = new JFileChooser();
		
		// Refresh desktop contents
		frame.show();
		
		// Splash screen
		welcome();
		
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void timeStep () {
		for (int i=0; i<Oreneta.NUM_METERS; i++) {
			flows[i].dataChanged();
			if (!chart_throughput.isClosed())
				chart_throughput.repaint();
			
			if(!chart_ipd.isClosed())
				chart_ipd.repaint();
				
			if(!chart_ipd_maxmin.isClosed())
				chart_ipd_maxmin.repaint();
				
			if (!chart_pps.isClosed())
				chart_pps.repaint();

			if (!chart_psize.isClosed())
				chart_psize.repaint();
				
			if (!chart_psize_maxmin.isClosed())
				chart_psize_maxmin.repaint();
				
			if (!chart_owd.isClosed())
				chart_owd.repaint();
				
			if (!chart_owd_maxmin.isClosed())
				chart_owd_maxmin.repaint();
			
			if (!chart_ipdv.isClosed())
				chart_ipdv.repaint();

			if (!chart_ipdv_maxmin.isClosed())
				chart_ipdv_maxmin.repaint();
				
			if (!chart_ploss.isClosed())
				chart_ploss.repaint();
						
			//chart[i].repaint();
			meter_gui[i].stats();
			common_flow_gui.dataChanged();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	//public void flowDataChanged(int meter_id) {
	//	flows[meter_id].dataChanged();
	//}
	
	////////////////////////////////////////////////////////////////////////////
	public void flowInserted (int meter_id, int pos) {
		flows[meter_id].flowInserted(pos);
	}

	////////////////////////////////////////////////////////////////////////////
	public final void debug (String s) {
		if (Oreneta.DEBUG) {
			console.append("DEBUG: "+s+"\n");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawFlow (Flow f) {
		chart_throughput.addDataModel (f);
		chart_pps.addDataModel (f);
		chart_ipd.addDataModel (f);
		chart_ipd_maxmin.addDataModel (f);
		chart_psize.addDataModel (f);
		chart_psize_maxmin.addDataModel (f);
		
		if (timers == 0) {
			chart_throughput.repaint();
			chart_pps.repaint();
			chart_ipd.repaint();
			chart_ipd_maxmin.repaint();
			chart_psize.repaint();
			chart_psize_maxmin.repaint();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void undrawFlow (Flow f) {
		chart_throughput.removeDataModel (f);
		chart_pps.removeDataModel (f);
		chart_ipd.removeDataModel (f);
		chart_ipd_maxmin.removeDataModel (f);
		chart_psize.removeDataModel (f);
		chart_psize_maxmin.removeDataModel (f);
		
		if (timers == 0) {
			chart_throughput.repaint();
			chart_pps.repaint();
			chart_ipd.repaint();
			chart_ipd_maxmin.repaint();
			chart_psize.repaint();
			chart_psize_maxmin.repaint();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void drawCommonFlow (CommonFlow cf) {
		chart_owd.addDataModel(cf);	
		chart_owd_maxmin.addDataModel(cf);
		chart_ploss.addDataModel(cf);
		chart_ipdv.addDataModel(cf);
		chart_ipdv_maxmin.addDataModel(cf);
		
		if (timers == 0) {
			chart_owd.repaint();
			chart_owd_maxmin.repaint();
			chart_ploss.repaint();
			chart_ipdv.repaint();
			chart_ipdv_maxmin.repaint();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void undrawCommonFlow (CommonFlow cf) {
		chart_owd.removeDataModel (cf);
		chart_owd_maxmin.removeDataModel (cf);
		chart_ploss.removeDataModel (cf);
		chart_ipdv.removeDataModel (cf);
		chart_ipdv_maxmin.removeDataModel (cf);
		
		if (timers == 0) {
			chart_owd.repaint();
			chart_owd_maxmin.repaint();
			chart_ploss.repaint();
			chart_ipdv.repaint();
			chart_ipdv_maxmin.repaint();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void updateTime () {
		analyzer.updateTime();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void startTimer () {
		if (!timer.isRunning()) {
			timer.start();
		}
		timers++;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void stopTimer () {
		if (timers == 1 && timer.isRunning()) {
			timer.stop();
		}
		timers--;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void clear () {
		analyzer.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void load () {
		int returnVal = fc.showOpenDialog(frame);

            	if (returnVal == JFileChooser.APPROVE_OPTION) {
                	File file = fc.getSelectedFile();
                	analyzer.load(file);
                	//debug("LOAD: "+file.getName());
                	
                	// Refresh snapshot windows
                	snapshot_flows.dataChanged();
			snapshot_common.dataChanged();
                }
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void save () {
		if (timers == 0) {
			int returnVal = fc.showSaveDialog(frame);
	
	            	if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	File file = fc.getSelectedFile();
	                	analyzer.save(file);
	                	//debug ("SAVE: "+file.getName());	
	                }
		} else {
			JOptionPane.showMessageDialog(frame,
			"Cannot save until all meters are stopped",
			"Error", JOptionPane.ERROR_MESSAGE);
		}       
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void exit () {
		System.exit(0);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void preferences () {
		prefs.show();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_snapshot_flows () {
		analyzer.delete_snapshot_flows();
		snapshot_flows.dataChanged();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void delete_snapshot_common () {
		analyzer.delete_snapshot_common();
		snapshot_common.dataChanged();
	}

}



