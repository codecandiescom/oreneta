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

public class TimeStep implements ActionListener {

	GUI gui;

	////////////////////////////////////////////////////////////////////////////
	TimeStep (GUI gui) {
		this.gui = gui;
	}
	
	////////////////////////////////////////////////////////////////////////////
	public void actionPerformed(ActionEvent e) {
		
		
		final SwingWorker worker = new SwingWorker() {
	        public Object construct() {
	            //...code that might take a while to execute is here...
	            
	            //System.out.println ("actionPerformed");
		
				// First of all, update time
				gui.updateTime();
				// lastTime++;
				
				// Update flow data (Data Model)
				gui.analyzer.timeStep();
				
				// Update flow tables (GUI)
				gui.timeStep();
				
				// Update charts (GUI)
	            return this;
	        }
	    };
	    worker.start();  //required for SwingWorker 3
	}

}