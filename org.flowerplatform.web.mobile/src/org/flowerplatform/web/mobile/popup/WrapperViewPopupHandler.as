package org.flowerplatform.web.mobile.popup {
	import mx.core.FlexGlobals;
	import mx.core.IVisualElement;
	
	import org.flowerplatform.flexutil.popup.IPopupHandler;
	
	public class WrapperViewPopupHandler implements IPopupHandler {
		
		protected var popupContentClass:Class;
		
		protected var _popupContent:IVisualElement;
		
		public function setTitle(value:String):IPopupHandler {
			return this;
		}
		
		public function setWidth(value:int):IPopupHandler {
			return this;
		}
		
		public function setHeight(value:int):IPopupHandler {
			return this;
		}
		
		public function setPopupContentClass(value:Class):IPopupHandler {
			popupContentClass = value;
			return this;
		}
		
		public function show(modal:Boolean=true):void {
			showInternal(false);
		}
		
		public function showModalOverAllApplication():void {
			showInternal(true);		
		}
		
		private function showInternal(modalOverAllApplication:Boolean):void {
			FlexGlobals.topLevelApplication.viewNavigator.pushView(WrapperView, { 
				popupContentClass: popupContentClass,
				popupHandler: this, 
				modalOverAllApplication: modalOverAllApplication
			});
		}
		
		public function getPopupContent():IVisualElement {
			return _popupContent;
		}

		public function set popupContent(value:IVisualElement):void {
			_popupContent = value;
		}
		
	}
}