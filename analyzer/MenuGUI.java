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

import java.awt.event.*;
import javax.swing.*;


public class MenuGUI implements ActionListener {

	GUI gui;
	
	////////////////////////////////////////////////////////////////////////////
	MenuGUI (GUI gui) {
		JMenuItem menuItem;
		
		JMenu mFile = new JMenu("File");
		addMenuItem (mFile, "Load", "icon/Open16.gif");
		addMenuItem (mFile, "Save", "icon/Save16.gif");
		mFile.addSeparator();
		addMenuItem (mFile, "Preferences", "icon/Preferences16.gif");
		mFile.addSeparator();
		addMenuItem (mFile, "Exit");
        
        JMenu mMeter = new JMenu ("Meter");
        for (int i=0; i<Oreneta.NUM_METERS;i++)
        	addMenuItem (mMeter, "Meter "+i, "icon/Host16.gif");
        	
        JMenu mFlows = new JMenu ("Flows");
        for (int i=0; i<Oreneta.NUM_METERS;i++)
        	addMenuItem (mFlows, "Flows "+i, "icon/Flows16.gif");
        	
        JMenu mSnapshot = new JMenu ("Snapshot");
        addMenuItem(mSnapshot, "Snapshot Flows", "icon/SnapshotFlows16.gif");
        addMenuItem(mSnapshot, "Snapshot Common Flows", "icon/SnapshotCommonFlows16.gif");
        
        JMenu mCharts = new JMenu("Charts");
        addMenuItem(mCharts, "Throughput");
        addMenuItem(mCharts, "PPS");
        addMenuItem(mCharts, "IPD");
        addMenuItem(mCharts, "IPD MaxMin");
        addMenuItem(mCharts, "Packet Size");
        addMenuItem(mCharts, "Packet Size MaxMin");
        mCharts.addSeparator();
        addMenuItem(mCharts, "One Way Delay");
        addMenuItem(mCharts, "One Way Delay MaxMin");
        addMenuItem(mCharts, "IP Delay Variation");
        addMenuItem(mCharts, "IP Delay Variation MaxMin");
        addMenuItem(mCharts, "Packet Loss");
        
        JMenu mWindow = new JMenu("Window");
        mWindow.add(mMeter);
        mWindow.add(mFlows);
        mWindow.add(mSnapshot);
        addMenuItem(mWindow, "Common Flows", "icon/CommonFlows16.gif");
        addMenuItem(mWindow, "Console", "icon/Console16.gif");
        mWindow.addSeparator();
        addMenuItem(mWindow, "Select columns", "icon/SelectColumns16.gif");
        
        JMenu mCommands = new JMenu("Commands");
        addMenuItem(mCommands, "Clear", "icon/Delete16.gif");
        addMenuItem(mCommands, "Delete Snapshot Flows", "icon/Delete16.gif");
        addMenuItem(mCommands, "Delete Snapshot Common Flows", "icon/Delete16.gif");
        
        JMenu mAbout = new JMenu("About");
        addMenuItem(mAbout, "About Oreneta", "icon/About16.gif");
        
    	// Menu Bar
    	JMenuBar menuBar = new JMenuBar();
        menuBar.add(mFile);
        menuBar.add(mWindow);
        menuBar.add(mCharts);
        menuBar.add(mCommands);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(mAbout);
    
        gui.frame.setJMenuBar(menuBar);
        this.gui = gui;
	}
	
	////////////////////////////////////////////////////////////////////////////
	void addMenuItem(JMenu menu, String item) {
		JMenuItem menuItem = new JMenuItem(item);
		menuItem.addActionListener(this);
		menu.add(menuItem);
	}
	
	////////////////////////////////////////////////////////////////////////////
	void addMenuItem(JMenu menu, String item, String icon) {
		JMenuItem menuItem = new JMenuItem(item, new ImageIcon(icon));
		menuItem.addActionListener(this);
		menu.add(menuItem);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void actionPerformed (ActionEvent e) {
		String event = e.getActionCommand();
        //gui.debug ("Menu action: " + e);
        if (event == "About Oreneta") {
        	gui.welcome();
        	gui.debug("About Oreneta");
        	
        } else if (event.startsWith("Meter")) {
        	try {
        		gui.meter_gui[Integer.parseInt(event.substring(6,event.length()))].show();
        	} catch (IndexOutOfBoundsException exception) {}
        
        } else if (event.startsWith("Flows")) {
        	try {
        		gui.flows[Integer.parseInt(event.substring(6,event.length()))].show();
        	} catch (IndexOutOfBoundsException exception) {}
        	
        } else if (event == "Snapshot Flows") {
        	gui.snapshot_flows.show();
        
        } else if (event == "Snapshot Common Flows") {
        	gui.snapshot_common.show();
        	
        } else if (event == "Delete Snapshot Flows") {
        	gui.delete_snapshot_flows();
        
        } else if (event == "Delete Snapshot Common Flows") {
        	gui.delete_snapshot_common();
        
        } else if (event == "Throughput") {
        	gui.chart_throughput.Show();
        	
        } else if (event == "PPS") {
        	gui.chart_pps.Show();
        	
        } else if (event == "IPD") {
        	gui.chart_ipd.Show();
        	
        } else if (event == "IPD MaxMin") {
        	gui.chart_ipd_maxmin.Show();

		} else if (event == "Packet Size") {
        	gui.chart_psize.Show();
        	
        } else if (event == "Packet Size MaxMin") {
        	gui.chart_psize_maxmin.Show();
        	
        } else if (event == "One Way Delay") {
        	gui.chart_owd.Show();
        	
        } else if (event == "One Way Delay MaxMin") {
        	gui.chart_owd_maxmin.Show();
        	
        } else if (event == "IP Delay Variation") {
        	gui.chart_ipdv.Show();
        	
        } else if (event == "IP Delay Variation MaxMin") {
        	gui.chart_ipdv_maxmin.Show();
        	
    	} else if (event == "Packet Loss") {
    		gui.chart_ploss.Show();
    	
    	} else if (event == "Common Flows") {
    		gui.common_flow_gui.show();	
        
        } else if (event == "Console") {
        	gui.console.show();
        	
        } else if (event == "Select columns") {
        	gui.columns.show();
        	
        } else if (event == "Clear") {
        	gui.clear();
        
        } else if (event == "Load") {
        	gui.load();
        
        } else if (event == "Save") {
        	gui.save();
        	
        } else if (event == "Exit") {
        	gui.exit();
        	
        } else if (event == "Preferences") {
        	gui.preferences();
        
        } else {
        	gui.debug ("What's happening? "+e);
        }
    }


}