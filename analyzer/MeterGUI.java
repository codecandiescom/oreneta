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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MeterGUI implements ActionListener {

	Meter meter;
	GUI gui;
	JInternalFrame frame;
	
	// Buttons
	JButton btn_connect;
	JButton btn_start;
	JTextField fld_hostname;
	JTextField fld_port;
	JTextField fld_device;
	JTextField fld_filter;
	JLabel lbl_pktrecv;
	JLabel lbl_pktdrop;
	
	////////////////////////////////////////////////////////////////////////////
	MeterGUI (GUI gui, Meter meter) {
		this.meter = meter;
		this.gui = gui;
		
		frame = new JInternalFrame ("Meter " + meter.meter_id, false, false, false, true);
		frame.setSize (500, 120);
		frame.setLocation (5+(meter.meter_id*505), 10);
		frame.setVisible(true);
		frame.setFrameIcon (new ImageIcon("icon/Host16.gif"));
		gui.desktop.add(frame);
		
		fld_hostname = new JTextField(15);
		fld_port = new JTextField("7777");
		fld_device = new JTextField("eth0");
		fld_filter = new JTextField("not port 22 and not port 7777");
		
		JLabel lbl_hostname = new JLabel("Hostname");
		JLabel lbl_port = new JLabel("Port");
		JLabel lbl_device = new JLabel("Device");
		JLabel lbl_filter = new JLabel("Filter");
		lbl_pktrecv = new JLabel("Packets received");
		lbl_pktdrop = new JLabel("Packets dropped");
		
		btn_connect = new JButton("Connect");
		btn_start = new JButton("Start");
		
		fld_device.setEnabled(false);
		fld_filter.setEnabled(false);
		btn_start.setEnabled(false);
		
		Container content = frame.getContentPane();
		//content.setLayout(new FlowLayout());
		content.setLayout (null);
		content.add(lbl_hostname);
		content.add(fld_hostname);
		content.add(lbl_port);
		content.add(fld_port);
		content.add(btn_connect);
		content.add(lbl_device);
		content.add(fld_device);
		content.add(lbl_filter);
		content.add(fld_filter);
		content.add(btn_start);
		content.add(lbl_pktrecv);
		content.add(lbl_pktdrop);
		
		Insets insets = content.getInsets();
        lbl_hostname.setBounds (10,5,75,20);
        fld_hostname.setBounds (80,5,150,20);
        lbl_port.setBounds (240,5,60,20);
        fld_port.setBounds (270,5,50,20);
        btn_connect.setBounds (330,5,120,20);
        lbl_device.setBounds (10,30,50,20);
        fld_device.setBounds (55,30,40,20);
        lbl_filter.setBounds (105,30,50,20);
        fld_filter.setBounds (145,30,250,20);
        btn_start.setBounds (400,30,80,20);
        lbl_pktrecv.setBounds (10,55,250,20);
        lbl_pktdrop.setBounds (250,55,250,20);
        
        //b1.setBounds(25 + insets.left, 5 + insets.top, 75, 20);
        //b2.setBounds(55 + insets.left, 35 + insets.top, 75, 20);
        //b3.setBounds(150 + insets.left, 15 + insets.top, 75, 30);
		
		//frame.pack();
		
		btn_connect.addActionListener(this);
		btn_start.addActionListener(this);
		fld_hostname.addActionListener(this);
		fld_port.addActionListener(this);
		
		StartEvent se = new StartEvent();
		fld_device.addActionListener(se);
		fld_filter.addActionListener(se);
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand() == "Connect") {
			try {
				meter.connect (fld_hostname.getText(),
			  	Integer.parseInt(fld_port.getText()));
			  	btn_connect.setText ("Disconnect");
			  	
			  	fld_device.setEnabled(true);
				fld_filter.setEnabled(true);
			  	btn_start.setEnabled(true);
				//frame.pack();
			} catch (IOException ioe) {
				gui.debug("Error connecting: " + ioe);
			}
			
		} else if (e.getActionCommand() == "Disconnect") {
			meter.disconnect ();
			btn_connect.setText ("Connect");
	
			fld_device.setEnabled(false);
			fld_filter.setEnabled(false);
			btn_start.setEnabled(false);
		
		} else if (e.getActionCommand() == "Start") {
			//gui.debug ("MeterGUI.commandStart");
			byte result = meter.start(fld_device.getText()+" "+fld_filter.getText());
			if (result == 0) {
				btn_start.setText ("Stop");
				btn_connect.setEnabled(false);
				gui.startTimer();
			} else if (result == 1 || result == 2) {
				gui.debug ("Start error - network error");
				fld_device.selectAll();
			} else if (result == 3 || result == 4) {
				gui.debug ("Start error - filter error");
				//fld_filter.setCaretColor(Color.red);
			}
		
		} else if (e.getActionCommand() == "Stop") {
			meter.stop();
			btn_start.setText ("Start");
			btn_connect.setEnabled(true);
			gui.stopTimer();
		
		} else {
			// Pressed ENTER in textfield
			if (!btn_start.isEnabled()) {
				try {
					meter.connect (fld_hostname.getText(),
				  	Integer.parseInt(fld_port.getText()));
				  	btn_connect.setText ("Disconnect");
				  	
				  	fld_device.setEnabled(true);
					fld_filter.setEnabled(true);
				  	btn_start.setEnabled(true);
				  	//gui.debug("connecting through keyboard ENTER");
					//frame.pack();
				} catch (IOException ioe) {
					gui.debug("Error connecting: " + ioe);
				}
			} else {			
				//gui.debug("Unknown event: "+e);
			}
		}
					
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void stats () {
		MeterStats ms = meter.stats();
		if (ms != null) {
			lbl_pktrecv.setText("Packets received: "+ms.packets_received);
			lbl_pktdrop.setText("Packets dropped: "+ms.packets_dropped);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void show() {
		try { frame.setIcon(false); } catch (java.beans.PropertyVetoException e) {}
		frame.toFront();
	}
	
	////////////////////////////////////////////////////////////////////////////
	class StartEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (btn_start.getText() == "Start") {
				byte result = meter.start(fld_device.getText()+" "+fld_filter.getText());
				if (result == 0) {
					btn_start.setText ("Stop");
					btn_connect.setEnabled(false);
					gui.startTimer();
				} else if (result == 1 || result == 2) {
					gui.debug ("Start error - network error");
					fld_device.selectAll();
				} else if (result == 3 || result == 4) {
					gui.debug ("Start error - filter error");
					//fld_filter.setCaretColor(Color.red);
				}
			}
		}		
	}
		
}
