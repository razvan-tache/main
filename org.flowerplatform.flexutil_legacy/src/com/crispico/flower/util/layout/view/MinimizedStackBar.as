package  com.crispico.flower.util.layout.view {
	import com.crispico.flower.flexdiagram.util.common.FlowerLinkButton;
	import com.crispico.flower.util.UtilAssets;
	import com.crispico.flower.util.layout.Workbench;
	import com.crispico.flower.util.layout.persistence.StackLayoutData;
	import org.flowerplatform.flexutil.layout.ViewLayoutData;
	
	import flash.events.MouseEvent;
	
	import mx.containers.Box;
	import mx.containers.BoxDirection;
	import mx.controls.Button;
	import mx.controls.LinkButton;
	import mx.core.UIComponent;
	
	/**
	 * Represents a graphical minimized stack.
	 * 
	 * @flowerModelElementId _dU_ScCvGEeG6vrEjfFek0Q
	 */
	public class MinimizedStackBar extends Box {
				
		/**
		 * @flowerModelElementId _xzRWcCx0EeG30ZBOJxPP8Q
		 */
		private var workbench:Workbench;
		
		/**
		 * @flowerModelElementId _yciV4Cx0EeG30ZBOJxPP8Q
		 */
		public var stackLayoutData:StackLayoutData;
		
		/**
		 * @flowerModelElementId _KfW88EZkEeGLHf9i2RlZCg
		 */
		private var restoreButton:Button;
		
		/**		
		 * @flowerModelElementId _ou0TYCx0EeG30ZBOJxPP8Q
		 */
		public function MinimizedStackBar(orientation:Number, stackLayoutData:StackLayoutData, workbench:Workbench) {
			this.stackLayoutData = stackLayoutData;
			this.workbench = workbench;
			this.setStyle("horizontalGap", 0);
			this.setStyle("verticalGap", 0);
									
			if (orientation == StackLayoutData.LEFT || orientation == StackLayoutData.RIGHT) {
				this.direction = BoxDirection.VERTICAL;
			} else {
				this.direction = BoxDirection.HORIZONTAL;
			}
		}
		
		/**
		 * For each stack layout data child, adds a button with corresponding icon.
		 * @flowerModelElementId _xnoNYEZuEeGLHf9i2RlZCg
		 */
		protected override function createChildren():void {			
			super.createChildren();
						
			restoreButton = new LinkButton();
		 	
		 	restoreButton.setStyle("icon", UtilAssets.INSTANCE.tabRes);
		 	restoreButton.setStyle("paddingLeft", 2);
		 	restoreButton.setStyle("paddingRight", 2);
			restoreButton.tabEnabled = false;
			
		 	restoreButton.addEventListener(MouseEvent.CLICK, restoreButtonHandler);		 	
		 	addChild(restoreButton);
		 	
		 	for each (var child:ViewLayoutData in stackLayoutData.children) {
		 		var linkBtn:FlowerLinkButton = new FlowerLinkButton();
				linkBtn.addEventListener(MouseEvent.CLICK, restoreButtonHandler);
		 		linkBtn.setStyle("iconURL", workbench.viewProvider.getIcon(child));
		 		linkBtn.toolTip = workbench.viewProvider.getTitle(child);
		 		linkBtn.setStyle("paddingLeft", 2);
		 		linkBtn.setStyle("paddingRight", 2);
				linkBtn.tabEnabled = false;
				
		 		addChild(linkBtn);
		 	}		 	
		}
		
		/**
		 * Restores the stack graphical component.
		 * <p>
		 * Sets the active view to be the restored component.
		 * 
		 * @flowerModelElementId _M_A2QEZkEeGLHf9i2RlZCg
		 */
		private function restoreButtonHandler(event:MouseEvent):void {
			workbench.restore(stackLayoutData);
			
			var tabNavigator:LayoutTabNavigator = LayoutTabNavigator(workbench.layoutDataToComponent[stackLayoutData]);
			if (tabNavigator.selectedIndex != -1) {			
				workbench.activeViewList.setActiveView(UIComponent(tabNavigator.getChildAt(tabNavigator.selectedIndex)));
			}
		}		
	}
	
}