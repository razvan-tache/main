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
package org.flowerplatform.editor.mindmap.controller {
	import flash.display.DisplayObject;
	import flash.display.Stage;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.core.IDataRenderer;
	import mx.core.IVisualElement;
	import mx.core.UIComponent;
	
	import org.flowerplatform.editor.mindmap.NotationMindMapDiagramShell;
	import org.flowerplatform.emf_model.notation.Diagram;
	import org.flowerplatform.emf_model.notation.MindMapNode;
	import org.flowerplatform.flexdiagram.DiagramShell;
	import org.flowerplatform.flexdiagram.controller.ControllerBase;
	import org.flowerplatform.flexdiagram.controller.model_extra_info.DynamicModelExtraInfoController;
	import org.flowerplatform.flexdiagram.mindmap.MindMapDiagramShell;
	import org.flowerplatform.flexdiagram.mindmap.controller.IMindMapControllerProvider;
	import org.flowerplatform.flexdiagram.mindmap.controller.IMindMapModelController;
	import org.flowerplatform.flexdiagram.renderer.DiagramRenderer;
	import org.flowerplatform.flexdiagram.tool.controller.drag.IDragController;
	import org.flowerplatform.flexdiagram.ui.MoveResizePlaceHolder;
	
	/**
	 * @author Cristina Constantinescu
	 */
	public class MindMapNodeDragController extends ControllerBase implements IDragController {
		
		public function MindMapNodeDragController(diagramShell:DiagramShell) {
			super(diagramShell);
		}
		
		public function activate(model:Object, initialX:Number, initialY:Number):void {		
			if (diagramShell.getControllerProvider(model).getModelChildrenController(model).getParent(model) is Diagram) { // don't drag the root node
				return;
			}
			getDynamicObject(model).initialX = initialX;
			getDynamicObject(model).initialY = initialY;
		}
		
		public function drag(model:Object, deltaX:Number, deltaY:Number):void {
			getDynamicObject(model).finalX = getDynamicObject(model).initialX + deltaX;
			getDynamicObject(model).finalY = getDynamicObject(model).initialY + deltaY;
			
			var point:Point = new Point(getDynamicObject(model).finalX, getDynamicObject(model).finalY);
			var renderer:IVisualElement = getRendererFromCoordinates(point);			
			var dropModel:Object = IDataRenderer(renderer).data;
						
			if (renderer is DiagramRenderer // don't drop over diagram 
				|| !dragModelIsParentForDropModel(model, dropModel) // or children structure
				|| (diagramShell.getControllerProvider(model).getModelChildrenController(model).getParent(dropModel) is MindMapNode && diagramShell.getControllerProvider(model).getModelChildrenController(model).getParent(model) == dropModel) // or in same parent (except root)
				|| ArrayList(NotationMindMapDiagramShell(diagramShell).editorStatefulClient.viewTypeToAcceptedViewTypeChildren[MindMapNode(dropModel).viewType]).getItemIndex(MindMapNode(model).viewType) == -1) { 
				// or not accepted edit part
				deletePlaceHolder(model);
				return;
			}
			
			var placeHolder:MoveResizePlaceHolder = getPlaceHolder(model);
			// set default values
			var rect:Rectangle = diagramShell.getControllerProvider(dropModel).getAbsoluteLayoutRectangleController(dropModel).getBounds(dropModel);
			placeHolder.x = rect.x;
			placeHolder.y = rect.y;
			placeHolder.width = rect.width;
			placeHolder.height = rect.height;		
			placeHolder.colors = [0x000000, 0xFFFFFF];
			
			var side:int = MindMapDiagramShell.NONE;
			if (dropModel.side == MindMapDiagramShell.LEFT) { 
				if (new Rectangle(rect.x, rect.y, rect.width / 2, rect.height).containsPoint(point)) {	// set styles for left rectangle			
					placeHolder.width = rect.width / 2;
					placeHolder.gradientBoxRotation = 0;
					side =  MindMapDiagramShell.LEFT;
				} else { // set styles for top rectangle
					placeHolder.gradientBoxRotation = Math.PI / 2;
				}
			} else if (dropModel.side == MindMapDiagramShell.RIGHT) { 
				if (new Rectangle(rect.x + rect.width / 2, rect.y, rect.width, rect.height).containsPoint(point)) {	// set styles for right rectangle
					placeHolder.x = rect.x + rect.width / 2;
					placeHolder.width = rect.width / 2;
					placeHolder.colors = [0xFFFFFF, 0x000000];
					placeHolder.gradientBoxRotation = 0;
					side =  MindMapDiagramShell.RIGHT;
				} else {	// set styles for top rectangle			
					placeHolder.gradientBoxRotation = Math.PI / 2;
				}
			} else { // root model
				if (new Rectangle(rect.x, rect.y, rect.width / 2, rect.height).containsPoint(point)) {	 // set styles for left rectangle
					placeHolder.x = rect.x;
					placeHolder.width = rect.width / 2;					
					placeHolder.gradientBoxRotation = 0;
					side =  MindMapDiagramShell.LEFT;
				} else { // set styles for right rectangle					
					placeHolder.x = rect.x + rect.width / 2;
					placeHolder.width = rect.width / 2;
					placeHolder.colors = [0xFFFFFF, 0x000000];
					side =  MindMapDiagramShell.RIGHT;
				}				
			}
			getDynamicObject(model).side = side;
		}
		
		public function drop(model:Object):void {
			var dropPoint:Point = new Point(getDynamicObject(model).finalX, getDynamicObject(model).finalY);
			var renderer:IVisualElement = getRendererFromCoordinates(dropPoint);
			
			var dropModel:Object = IDataRenderer(renderer).data;
			if (renderer is DiagramRenderer // don't drop over diagram 
				|| !dragModelIsParentForDropModel(model, dropModel) // or children structure
				|| (diagramShell.getControllerProvider(dropModel).getModelChildrenController(dropModel).getParent(dropModel) is MindMapNode && diagramShell.getControllerProvider(model).getModelChildrenController(model).getParent(model) == dropModel) // or in same parent (except root)
				|| ArrayList(NotationMindMapDiagramShell(diagramShell).editorStatefulClient.viewTypeToAcceptedViewTypeChildren[MindMapNode(dropModel).viewType]).getItemIndex(MindMapNode(model).viewType) == -1) { 
				// or not accepted edit part
				return;
			}
			
			deletePlaceHolder(model);
						
			var side:int = getDynamicObject(model).side;
			
			// remove model from current parent
			var dragParentModel:Object = diagramShell.getControllerProvider(model).getModelChildrenController(model).getParent(model);		

			// calculate new parent and position based on side
			var dropParentModel:Object = (side != MindMapDiagramShell.NONE) ? dropModel : diagramShell.getControllerProvider(dropModel).getModelChildrenController(dropModel).getParent(dropModel);	
			var children:IList = diagramShell.getControllerProvider(dropParentModel).getModelChildrenController(dropParentModel).getChildren(dropParentModel);	
		
			var index:int = 0;
			if (side != MindMapDiagramShell.NONE) {
				index = children.length;
			} else {
				for (var i:int = 0; i < children.length; i++) {
					if (children.getItemAt(i) == dropModel) {
						index = i;
						break;
					}
				}
			}
			NotationMindMapDiagramShell(diagramShell).editorStatefulClient.service_changeParent(
				model.id, 
				dropParentModel.id,
				index, 
				(side != MindMapDiagramShell.NONE) ? side : getModelController(dropModel).getSide(dropModel), 
				this, selectModelAfterDroppingHandler);
			
			// add model in new parent
//			getModelController(dropParentModel).getChildren(dropParentModel).addItemAt(model, index);		
//			getModelController(model).setSide(model, (side != MindMapDiagramShell.NONE) ? side : getModelController(dropModel).getSide(dropModel));
//			getModelController(model).setParent(model, dropParentModel);	
			
			// select model or parent 
			diagramShell.selectedItems.removeAll();
//			if (getModelController(dropParentModel).getExpanded(dropParentModel)) {
//				diagramShell.selectedItems.addItem(model);
//			} else {
//				diagramShell.selectedItems.addItem(dropParentModel);
//			}
		}
		
		private function selectModelAfterDroppingHandler(viewId:Object):void {
			trace(viewId);
		}
		
		public function deactivate(model:Object):void {
			delete getDynamicObject(model).initialX;
			delete getDynamicObject(model).initialY;
			delete getDynamicObject(model).finalX;
			delete getDynamicObject(model).finalY;
			delete getDynamicObject(model).side;
		}
		
		private function getDynamicObject(model:Object):Object {
			return DynamicModelExtraInfoController(diagramShell.getControllerProvider(model).getModelExtraInfoController(model)).getDynamicObject(model);
		}
		
		private function getModelController(model:Object):IMindMapModelController {
			return MindMapDiagramShell(diagramShell).getModelController(model);
		}
		
		private function deletePlaceHolder(model:Object):void {
			var placeHolder:MoveResizePlaceHolder = getDynamicObject(model).placeHolder;
			if (placeHolder != null) {
				diagramShell.diagramRenderer.removeElement(placeHolder);
				delete getDynamicObject(model).placeHolder;		
			}
		}
		
		private function getPlaceHolder(model:Object):MoveResizePlaceHolder {
			var placeHolder:MoveResizePlaceHolder = getDynamicObject(model).placeHolder;
			if (placeHolder == null) {
				placeHolder = new MoveResizePlaceHolder();				
				placeHolder.alphas = [0.4, 0.4];
				placeHolder.ratios = [0, 255];				
				getDynamicObject(model).placeHolder = placeHolder;
				diagramShell.diagramRenderer.addElement(placeHolder);
			}
			return placeHolder;
		}
		
		protected function getRendererFromCoordinates(point:Point):IVisualElement {
			var stage:Stage = DisplayObject(diagramShell.diagramRenderer).stage;
			var arr:Array = stage.getObjectsUnderPoint(new Point(stage.mouseX, stage.mouseY));
			
			var renderer:IVisualElement;
			var i:int;
			for (i = arr.length - 1; i >= 0;  i--) {
				renderer = getRendererFromDisplay(arr[i]);
				if (renderer != null) {					
					return renderer;
				}
			}
			return null;
		}
		
		protected function getRendererFromDisplay(obj:Object):IVisualElement {			
			// in order for us to traverse its hierrarchy
			// it has to be a DisplayObject
			if (!(obj is DisplayObject) || obj is MoveResizePlaceHolder) {
				return null;
			}
			
			// traverse all the obj's hierarchy	
			while (obj != null) {
				if (obj is DiagramRenderer) {
					return IVisualElement(obj);
				}
				if (obj is IDataRenderer && diagramShell.modelToExtraInfoMap[IDataRenderer(obj).data] != null) {
					// found it
					return IVisualElement(obj);					
				}
				obj = DisplayObject(obj).parent;
			}
			
			// no found on the obj's hierarchy
			return null;
		}
		
		private function dragModelIsParentForDropModel(dragModel:Object, dropModel:Object):Boolean {
			if (dragModel == dropModel) {
				return false;
			}
			if (diagramShell.getControllerProvider(dropModel).getModelChildrenController(dropModel).getParent(dropModel) == null) {
				return true;
			}
			return dragModelIsParentForDropModel(dragModel, diagramShell.getControllerProvider(dropModel).getModelChildrenController(dropModel).getParent(dropModel));
		}
	}
}