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
import java.awt.*;

public class ConsoleGUI {

	GUI gui;
	JInternalFrame frame;
	JTextArea area;
	
	////////////////////////////////////////////////////////////////////////////
	ConsoleGUI (GUI gui) {
		this.gui = gui;
		
		create();
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void create() {
		frame = new JInternalFrame ("Console", true, true, true, true);
	    frame.setSize (600, 250);
	    frame.setLocation (5, 135);
	    frame.setVisible(false);
	    frame.setFrameIcon (new ImageIcon("icon/Console16.gif"));
	    gui.desktop.add(frame);

		area = new JTextArea();
		area.setText ("ORENETA - Oneway delay REaltime NETwork Analizer\n");
		area.setEditable(false);
		
		frame.getContentPane().add (new JScrollPane(area), BorderLayout.CENTER);
		
		// iconify console
		// try {frame.setIcon(true);} catch (java.beans.PropertyVetoException e){}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void append (String s) {
		area.append(s);
			
		if (area.getLineCount() > 250) {
			try{
				area.replaceRange ("", 0, area.getLineEndOffset(100));
			} catch (javax.swing.text.BadLocationException e) {}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void show () {
		if(frame == null || frame.isClosed()) {
			create();
			frame.setVisible(true);
		} else {
			frame.setVisible(true);
			try { frame.setIcon(false); } catch (java.beans.PropertyVetoException e) {}
			frame.toFront();
		}
	}

}
