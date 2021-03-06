/* license-start
 * 
 * Copyright (C) 2008 - 2013 Crispico, <http://www.crispico.com/>.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details, at <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *   Crispico - Initial API and implementation
 *
 * license-end
 */
package org.flowerplatform.web.layout {
	import com.crispico.flower.util.layout.Workbench;
	import com.crispico.flower.util.layout.persistence.SashLayoutData;
	import com.crispico.flower.util.layout.persistence.WorkbenchLayoutData;
	
	/**
	 * An instance for this class should exist for each perspective.
	 * 
	 * Holds specific information for the perspective 
	 * (e.g. name and icon url that will appear in the menu).
	 * 
	 * <p>
	 * It should also know how to reset the perspective by setting programatically 
	 * the initial layout data on workbench.
	 * 
	 * @author Cristi
	 * @author Cristina
	 * 
	 */
	public class Perspective {
		
		public function get id():String {
			throw new Error("This method needs to be implemented.");
		}
		
		/**
		 * Default name.
		 * 
		 * 
		 */
		public function get name():String {
			throw new Error("This method needs to be implemented.");
		}
		
		/**
		 * Default icon.
		 * 
		 * 
		 */
		public function get iconUrl():String {			
			throw new Error("This method needs to be implemented.");
		}
		
		/**
		 * Abstract method.
		 * 
		 * <p>
		 * Should clear the workbench and build the layout for this perspective.
		 * 
		 * 
		 */
		public function resetPerspective(workbench:Workbench):void {
			throw new Error("This method needs to be implemented.");
		}
		
		protected function load(workbench:Workbench, workbenchLayoutData:WorkbenchLayoutData, editorSashLayoutData:SashLayoutData):void {
			if (editorSashLayoutData == null) {
				throw new Error("A sash editor must exist on a perspective!");
			}
			workbench.load(workbenchLayoutData, true, true);
		}
	}
}