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
package  org.flowerplatform.web.git.layout {
	import com.crispico.flower.util.layout.Workbench;
	import com.crispico.flower.util.layout.persistence.SashLayoutData;
	import com.crispico.flower.util.layout.persistence.StackLayoutData;
	import com.crispico.flower.util.layout.persistence.WorkbenchLayoutData;
	
	import mx.collections.ArrayCollection;
	
	import org.flowerplatform.flexutil.layout.ViewLayoutData;
	import org.flowerplatform.web.common.explorer.ExplorerViewProvider;
	import org.flowerplatform.web.git.GitPlugin;
	import org.flowerplatform.web.git.history.GitHistoryViewProvider;
	import org.flowerplatform.web.layout.Perspective;
	
	/**
	 * @author Cristina Constantinescu
	 */
	public class GitPerspective extends Perspective {
		
		public static const ID:String = "GitPerspective";
		
		public override function get id():String {
			return ID;
		}
		
		public override function get name():String {	
			return GitPlugin.getInstance().getMessage("git.perspective.name");
		}
		
		public override function get iconUrl():String {			
			return GitPlugin.getInstance().getResourceUrl("images/full/obj16/gitrepository.gif");
		}
		
		public override function resetPerspective(workbench:Workbench):void {	
			var wld:WorkbenchLayoutData = new WorkbenchLayoutData();
			wld.direction = SashLayoutData.HORIZONTAL;
			wld.ratios = new ArrayCollection([25, 75]);
			wld.mrmRatios = new ArrayCollection([0, 0]);
			
			var nav:StackLayoutData = new StackLayoutData();
			nav.parent = wld;
			wld.children.addItem(nav);
			
			// repositories view
			var view:ViewLayoutData = new ViewLayoutData();
			view.viewId = ExplorerViewProvider.ID;
			nav.children.addItem(view);
			view.parent = nav;
			
			var sash:SashLayoutData = new SashLayoutData();
			sash.direction = SashLayoutData.VERTICAL;
			sash.ratios = new ArrayCollection([70, 30]);
			sash.mrmRatios = new ArrayCollection([0, 0]);
			sash.parent = wld;
			wld.children.addItem(sash);
			
			var sashEditor:SashLayoutData = new SashLayoutData();
			sashEditor.isEditor = true;
			sashEditor.direction = SashLayoutData.HORIZONTAL;
			sashEditor.ratios = new ArrayCollection([100]);
			sashEditor.mrmRatios = new ArrayCollection([0]);
			sashEditor.parent = sash;
			sash.children.addItem(sashEditor);
			
			var stackEditor:StackLayoutData = new StackLayoutData();
			stackEditor.parent = sashEditor;
			sashEditor.children.addItem(stackEditor);
						
			var stack:StackLayoutData = new StackLayoutData();
			stack.parent = sash;
			sash.children.addItem(stack);
			
			// history view
			view = new ViewLayoutData();
			view.viewId = GitHistoryViewProvider.ID;
			stack.children.addItem(view);
			view.parent = stack;
			
			load(workbench, wld, sashEditor);
		}
	}
	
}