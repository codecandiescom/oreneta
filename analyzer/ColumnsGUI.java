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
import java.awt.event.*;

public class ColumnsGUI implements ActionListener {
	
	GUI gui;
	
	JInternalFrame frame;
	
	JButton btn_ok;
	JButton btn_apply;
	JButton btn_cancel;
	
	JCheckBox [][] checkBoxes;
	final static int NUM_PANELS = 5;
	
	////////////////////////////////////////////////////////////////////////////
	ColumnsGUI (GUI gui) {
		this.gui = gui;
	
		create();	
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void create() {
		frame = new JInternalFrame ("Select Columns", false, true, false, false);
		frame.setSize (600, 300);
		frame.setLocation (5, 135);
		frame.setVisible(false);
		frame.setFrameIcon (new ImageIcon("icon/SelectColumns16.gif"));
		gui.desktop.add(frame);
		
		/*
		checkBoxes = new JCheckBox [0][gui.flows[0].tableModel.getColumnCount()];
		checkBoxes = new JCheckBox [1][gui.flows[1].tableModel.getColumnCount()];
		checkBoxes = new JCheckBox [2][gui.common_flow_gui.tableModel.getColumnCount()];
		checkBoxes = new JCheckBox [3][gui.snapshot_flows.tableModel.getColumnCount()];
		checkBoxes = new JCheckBox [4][gui.snapshot_common.tableModel.getColumnCount()];
		*/
		
		// Tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );

		JPanel panel [] = new JPanel[NUM_PANELS];
		OrenetaTableModel models [] = new OrenetaTableModel[NUM_PANELS];
		
		models[0] = gui.flows[0].tableModel;
		models[1] = gui.flows[1].tableModel;
		models[2] = gui.common_flow_gui.tableModel;
		models[3] = gui.snapshot_flows.tableModel;
		models[4] = gui.snapshot_common.tableModel;
		
		checkBoxes = new JCheckBox[NUM_PANELS][];
		for (int i=0; i<NUM_PANELS; i++ ) {
			panel[i] = new JPanel(true);
			checkBoxes[i] = new JCheckBox [models[i].getColumnCount()];
			for (int j=0; j<models[i].getColumnCount(); j++) {
				checkBoxes[i][j] = new JCheckBox(
				models[i].getColumnName(j),
				models[i].selectedColumns[j]);
				
				panel[i].add(checkBoxes[i][j]);
			}
			
			panel[i].setLayout(new GridLayout(5, 2));
		}
		
		tabbedPane.addTab("Flows 0", new ImageIcon("icon/Flows16.gif"), panel[0]);
		tabbedPane.addTab("Flows 1", new ImageIcon("icon/Flows16.gif"), panel[1]);
		tabbedPane.addTab("Common Flows", new ImageIcon("icon/CommonFlows16.gif"), panel[2]);
		tabbedPane.addTab("Snapshot Flows", new ImageIcon("icon/SnapshotFlows16.gif"), panel[3]);
		tabbedPane.addTab("Snapshot Common Flows", new ImageIcon("icon/SnapshotCommonFlows16.gif"), panel[4]);
		
		// Buttons
		btn_ok = new JButton("OK");
		btn_apply = new JButton("Apply");
		btn_cancel = new JButton("Cancel");
		
		JPanel panelButtons = new JPanel(true);
		panelButtons.add(btn_ok);
		panelButtons.add(btn_apply);
		panelButtons.add(btn_cancel);
		
		btn_ok.addActionListener(this);
		btn_apply.addActionListener(this);
		btn_cancel.addActionListener(this);
		
		// Container
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		content.add (tabbedPane, BorderLayout.CENTER);
		content.add (panelButtons, BorderLayout.SOUTH);
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
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand() == "OK") {
			applyChanges();
			frame.dispose();
			
		} else if (e.getActionCommand() == "Apply") {
			applyChanges();
				
		} else if (e.getActionCommand() == "Cancel") {
			frame.dispose();
			
		} else {
			gui.debug ("Extrange things are happening");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	void applyChanges () {
		for(int i=0; i<gui.flows[0].tableModel.getColumnCount(); i++)
			gui.flows[0].tableModel.selectedColumns[i] =
				checkBoxes[0][i].isSelected();
		gui.flows[0].columnsChanged();
		
		for(int i=0; i<gui.flows[1].tableModel.getColumnCount(); i++)
			gui.flows[1].tableModel.selectedColumns[i] =
				checkBoxes[1][i].isSelected();
		gui.flows[1].columnsChanged();
		
		for(int i=0; i<gui.common_flow_gui.tableModel.getColumnCount(); i++)
			gui.common_flow_gui.tableModel.selectedColumns[i] =
				checkBoxes[2][i].isSelected();
		gui.common_flow_gui.columnsChanged();
		
		for(int i=0; i<gui.snapshot_flows.tableModel.getColumnCount(); i++)
			gui.snapshot_flows.tableModel.selectedColumns[i] =
				checkBoxes[3][i].isSelected();
		gui.snapshot_flows.columnsChanged();
		
		for(int i=0; i<gui.snapshot_common.tableModel.getColumnCount(); i++)
			gui.snapshot_common.tableModel.selectedColumns[i] =
				checkBoxes[4][i].isSelected();
		gui.snapshot_common.columnsChanged();
	}
		
		
	
}