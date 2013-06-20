package org.flowerplatform.flexdiagram.tool {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.ArrayList;
	import mx.core.mx_internal;
	import mx.utils.ArrayUtil;
	
	import org.flowerplatform.flexdiagram.DiagramShell;
	
	/**
	 * @author Cristina Constantinescu
	 */ 
	public class WakeUpTool extends Tool {
				
		public static const ID:String = "WakeUpTool";
		
		public static const NO_EVENT:String = "none";		
		public static const MOUSE_DRAG:String = "mouseDrag";
		public static const MOUSE_DOWN:String = "mouseDown";
		public static const MOUSE_UP:String = "mouseUp";
		
		private static var listeners:ArrayList = new ArrayList();
			
		public var myEventType:String;
		
		public function WakeUpTool(diagramShell:DiagramShell) {
			super(diagramShell);
		}
		
		public static function wakeMeUpIfEventOccurs(tool:Tool, event:String, priority:int = 0):void {
			var listener:Object = new Object();
			listener.tool = tool;
			listener.event = event;
			listener.priority = priority;
			
			listeners.addItem(listener);
		}
		
		override public function activateAsMainTool():void {			
			diagramRenderer.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
			diagramRenderer.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);			
			diagramRenderer.addEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);			
		}
		
		override public function deactivateAsMainTool():void {			
			diagramRenderer.removeEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
			diagramRenderer.removeEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);			
			diagramRenderer.removeEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);
		}
				
		override public function activateDozingMode():void {
			diagramRenderer.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
		override public function deactivateDozingMode():void { 	
			diagramRenderer.removeEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
		// added here because we only need one event for all tools
		// this seamed to be the right place
		protected function keyDownHandler(event:KeyboardEvent):void {
			if (event.keyCode == Keyboard.ESCAPE || event.keyCode == Keyboard.BACK) {
				// if ESC or back button pressed, deactivates current tool
				diagramShell.mainToolFinishedItsJob();
			}
		}
		
		private function mouseDownHandler(event:MouseEvent):void {
			myEventType = MOUSE_DOWN;			
			dispatchMyEvent(myEventType, event);			
		}
		
		private function mouseMoveHandler(event:MouseEvent):void {
			if (event.buttonDown) {			
				myEventType = MOUSE_DRAG;
				dispatchMyEvent(myEventType, event);
			}			
		}
				
		private function mouseUpHandler(event:MouseEvent):void {
			if (myEventType != MOUSE_DRAG) {
				myEventType = MOUSE_UP;
				dispatchMyEvent(myEventType, event);
			}	
		}
			
		private function dispatchMyEvent(eventType:String, initialEvent:MouseEvent):void {
			var array:Array = filterAndSortListeners(eventType);
			
			while (array.length != 0) {				
				var tool:IWakeUpableTool = array.pop().tool;
				if (tool.wakeUp(eventType, initialEvent)) {
					diagramShell.mainTool = Tool(tool);
					break;
				}
			}
		}
		
		private function filterAndSortListeners(eventType:String):Array {
			// filter by eventType
			var array:Array = listeners.source.filter(
				function callback(item:*, index:int, array:Array):Boolean {
					return (item.event == eventType);
				}			
			);			
			// sort descending by priority
			return array.sortOn("priority", Array.DESCENDING & Array.NUMERIC);
		}
	}
}