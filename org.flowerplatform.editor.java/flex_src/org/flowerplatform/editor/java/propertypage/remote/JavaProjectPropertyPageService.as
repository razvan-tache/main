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
package org.flowerplatform.editor.java.propertypage.remote {
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	
	import org.flowerplatform.communication.CommunicationPlugin;
	import org.flowerplatform.communication.service.InvokeServiceMethodServerCommand;
	import org.flowerplatform.communication.tree.remote.TreeNode;
	
	/**
	 * @author Cristina Constantinescu
	 */
	public class JavaProjectPropertyPageService {
	
		public static const SERVICE_ID:String = "javaProjectPropertyPageService";
		
		private function convertSelection(selection:ArrayCollection):ArrayCollection {
			var array:ArrayCollection = new ArrayCollection();
			for (var i:int=0; i <selection.length; i++) {
				array.addItem(TreeNode(selection.getItemAt(i)).getPathForNode(true));
			}
			return array;
		}
		
		private function invokeServiceMethod(methodName:String, parameters:Array, 
											 resultCallbackObject:Object=null, resultCallbackFunction:Function=null):void {
			CommunicationPlugin.getInstance().bridge.sendObject(
				new InvokeServiceMethodServerCommand(	SERVICE_ID, methodName, parameters, resultCallbackObject, resultCallbackFunction));
		}
		
		public function hasJavaNature(selectedNode:TreeNode, 
											  resultCallbackObject:Object=null, resultCallbackFunction:Function=null):void {			
			invokeServiceMethod(
				"hasJavaNature", 
				[selectedNode.getPathForNode(true)], 
				resultCallbackObject, resultCallbackFunction);	
		}
		
		public function setJavaNature(selectedNode:TreeNode, 
											resultCallbackObject:Object=null, resultCallbackFunction:Function=null):void {			
			invokeServiceMethod(
				"setJavaNature", 
				[selectedNode.getPathForNode(true)], 
				resultCallbackObject, resultCallbackFunction);	
		}
		
		public function getClasspathEntries(selectedNode:TreeNode, 
									  resultCallbackObject:Object=null, resultCallbackFunction:Function=null):void {			
			invokeServiceMethod(
				"getClasspathEntries", 
				[selectedNode.getPathForNode(true)], 
				resultCallbackObject, resultCallbackFunction);	
		}
		
		public function setClasspathEntries(selectedNode:TreeNode, srcFolders:ArrayCollection, projects:ArrayCollection, libraries:ArrayCollection,
											resultCallbackObject:Object=null, resultCallbackFunction:Function=null):void {			
			invokeServiceMethod(
				"setClasspathEntries", 
				[selectedNode.getPathForNode(true), srcFolders, projects, libraries], 
				resultCallbackObject, resultCallbackFunction);	
		}
	}
}