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
import java.awt.*;


public class PreferencesGUI implements ActionListener {

	GUI gui;
	
	JInternalFrame frame;
	
	JSlider sld_max_ipd;
	JSlider sld_flows_holdtime;
	JSlider sld_packet_holdtime;
	
	JButton btn_ok;
	JButton btn_cancel;
	
	////////////////////////////////////////////////////////////////////////////
	PreferencesGUI (GUI gui) {
		this.gui = gui;
		
		create();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void create() {
		
		// Frame
		frame = new JInternalFrame ("Preferences", false, false, false, false);
		frame.setSize (220, 280);
		frame.setLocation (5, 135);
		frame.setVisible(false);
		frame.setFrameIcon (new ImageIcon("icon/Preferences16.gif"));
		gui.desktop.add(frame);
		
		// Sliders
		sld_max_ipd = new JSlider(JSlider.HORIZONTAL, 2, 12,
			(int)gui.analyzer.prefs.max_ipd);
		sld_max_ipd.setMajorTickSpacing(1);
		sld_max_ipd.setMinorTickSpacing(1);
		sld_max_ipd.setPaintTicks(true);
		sld_max_ipd.setPaintLabels(true);
			
		sld_flows_holdtime = new JSlider(JSlider.HORIZONTAL, 10, 310,
			gui.analyzer.prefs.flows_holdtime);
		sld_flows_holdtime.setMajorTickSpacing(50);
		sld_flows_holdtime.setMinorTickSpacing(10);
		sld_flows_holdtime.setPaintTicks(true);
		sld_flows_holdtime.setPaintLabels(true);
			
		sld_packet_holdtime = new JSlider(JSlider.HORIZONTAL, 2, 12,
			gui.analyzer.prefs.packet_holdtime);
		sld_packet_holdtime.setMajorTickSpacing(1);
		sld_packet_holdtime.setMinorTickSpacing(1);
		sld_packet_holdtime.setPaintTicks(true);
		sld_packet_holdtime.setPaintLabels(true);
		
		// Buttons
		btn_ok = new JButton("OK");
		btn_cancel = new JButton("Cancel");
		
		btn_ok.addActionListener(this);
		btn_cancel.addActionListener(this);
		
		// Container
		Container content = frame.getContentPane();
		content.setLayout(new FlowLayout());
		content.add (new JLabel("Max IPD", JLabel.CENTER));
		content.add (sld_max_ipd);
		content.add (new JLabel("Flows Hold Time", JLabel.CENTER));
		content.add (sld_flows_holdtime);
		content.add (new JLabel("Packet Hold Time", JLabel.CENTER));
		content.add (sld_packet_holdtime);
		content.add (btn_ok);
		content.add (btn_cancel);
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
			if (gui.timers == 0) {
				gui.analyzer.prefs.max_ipd = (double) sld_max_ipd.getValue();
				gui.analyzer.prefs.flows_holdtime = sld_flows_holdtime.getValue();
				gui.analyzer.prefs.packet_holdtime = sld_packet_holdtime.getValue();
				frame.dispose();
			} else {
				JOptionPane.showMessageDialog(frame,
				"Cannot set preferences until all meters are stopped",
				"Error", JOptionPane.ERROR_MESSAGE);
			}
				
		} else if (e.getActionCommand() == "Cancel") {
			frame.dispose();
			
		} else {
			gui.debug ("Extrange things are happening");
		}
	}
        
}
