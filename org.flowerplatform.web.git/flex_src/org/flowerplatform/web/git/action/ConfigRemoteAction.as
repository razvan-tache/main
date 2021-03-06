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
package org.flowerplatform.web.git.action {
	
	import org.flowerplatform.communication.tree.remote.TreeNode;
	import org.flowerplatform.flexutil.popup.ActionBase;
	import org.flowerplatform.web.git.GitNodeType;
	import org.flowerplatform.web.git.GitPlugin;
	import org.flowerplatform.web.git.dto.RemoteConfig;
	import org.flowerplatform.web.git.ui.ConfigRemoteWindow;
	
	/**
	 * @author Cristina Constantinescu
	 */
	public class ConfigRemoteAction extends ActionBase {
		
		private var node:TreeNode;
		
		public function ConfigRemoteAction() {
			label = GitPlugin.getInstance().getMessage("git.action.configRemote.label");
			icon = GitPlugin.getInstance().getResourceUrl('images/full/obj16/remote_entry_tbl.gif');
			orderIndex = int(GitPlugin.getInstance().getMessage("git.action.configRemote.sortIndex"));
		}
		
		override public function get visible():Boolean {
			if (selection.length == 1 && selection.getItemAt(0) is TreeNode) {			
				return TreeNode(selection.getItemAt(0)).pathFragment.type == GitNodeType.NODE_TYPE_REMOTE;
			}
			return false;
		}
		
		private function getRemoteConfigDataCallbackHandler(result:RemoteConfig):void {
			if (result != null) {
				var configWindow:ConfigRemoteWindow = new ConfigRemoteWindow();
				configWindow.node = node.parent;
				configWindow.remoteConfig = result;
				configWindow.showPopup();
			}
		}
		
		override public function run():void {
			node = TreeNode(selection.getItemAt(0));
			GitPlugin.getInstance().service.getRemoteConfigData(node, this, getRemoteConfigDataCallbackHandler);			
		}	
	}
}