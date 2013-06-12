package com.crispico.flower.flexdiagram.contextmenu {
	
	import flash.events.MouseEvent;
	import flash.geom.Rectangle;
	
	/**
	 * An implementation of this interface must be provided to the <code>ContextMenuManager</code> when
	 * registering an client viewer. It is a bridge between the client viewer and the CM framework.
	 * 
	 * <p/> Extends ISelectionProvider because when an action corresponding to a menu entry is run it needs
	 * to know about how to obtain it's selection. 
	 * @author Sorin 
	 * @flowerModelElementId _erw3wMSZEd-CHMGlopD3VQ
	 */
	public interface IContextMenuLogicProvider extends ISelectionProvider {
		
		/**
		 * Must be implemented to return the display area of the main selection, relative to the application's stage.
		 * If it returns null the Context Menu framework will interpret as the selection is not visible.
		 * 
		 * <p/>This method will be called by the CM framework after filling of the Context Menu has been done
		 * when it is in the positioning stage.
		 */
		 function get displayAreaOfSelection():Rectangle;
		 
		 /**
		 * Must be implemented to return if an object is under a given point or not.
		 * <p> It will be called by the CM framework to verify if the active Context Menu must be closed or not 
		 * based on the mouse position. 
		 * @author Cristina
		 */ 
		 function isOverSelection(event:MouseEvent):Boolean;
		 
		 /**
		  * Must be implemented to set focus on main selected element.
		  * <p> It will be called by the CM framework when closing the context menu
		  * after executing an action
		  *  
		  * @author Cristina
		  */
		 function setFocusOnMainSelectedObject():void;
		 
	}
}