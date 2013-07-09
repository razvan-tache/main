package org.flowerplatform.editor {
	import flash.events.Event;
	
	import mx.containers.VBox;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	import mx.events.PropertyChangeEvent;
	
	import org.flowerplatform.editor.remote.EditableResource;
	import org.flowerplatform.editor.remote.EditorStatefulClient;
	
	import spark.components.RichEditableText;
	import spark.components.Scroller;
	import spark.components.TextArea;
	
	/**
	 * Base class for editors. Their role is to display the
	 * content of an EditableResource and to delegate to the corresponding (i.e. it's owner)
	 * <code>EditorFrontendController</code> when the user updates the
	 * editor (and updates need to be sent to server).
	 * 
	 * <p>
	 * It implements <code>ModalSpinnerSupport</code>, since it 
	 * does not have absolute layout; if it was a <code>Canvas</code>,
	 * there would be no need to implement <code>ModalSpinnerSupport</code>
	 * anymore. 
	 * 
	 * @author Cristi
	 * @flowerModelElementId _6houUIn2EeGENqKo5G_OSw
	 */
	public class EditorFrontend extends VBox/* implements IDirtyStateProvider, ModalSpinnerSupport */ {
	
		/**
		 * @flowerModelElementId _8haNsIn2EeGENqKo5G_OSw
		 */
		public var resourceStatusBar:ResourceStatusBar = new ResourceStatusBar();
		
		/**
		 * TODO CS/STFL Inutil, pentru ca avem referinta catre StatefulClient.
		 * 
		 * @flowerModelElementId _joeVQI-IEeGlZvO-ph04FQ
		 */
		public var editorInput:Object;				
		
		/**
		 * @flowerModelElementId _qnkjcKWDEeGRwJCiMM7wFA
		 */
		protected var createCollaborativeTools:Array = new Array();
		
		/**
		 * This is the underlying layer from EditorAndDiagramContainer.
		 * For example it can be a text editor.
		 * 
		 * @flowerModelElementId _CV_cYKWLEeGNp8EWVqus7Q
		 */
		private var _editor:UIComponent;
		
//		/**
//		 * @flowerModelElementId _ji2C0KWLEeGNp8EWVqus7Q
//		 */
//		protected var editorAndDiagramContainer:EditorAndDiagramContainer;
//		
//		/**
//		 * @flowerModelElementId _ntYUIKWMEeGNp8EWVqus7Q
//		 */
//		public var collaborativeDiagramViewer:DiagramViewer;
//		
//		/**
//		 * @flowerModelElementId _0JuREKiiEeGHceW7NM6-3w
//		 */
//		protected var _rootFigure:RootFigure;
//		/**
//		 * @flowerModelElementId _xSWFIKlvEeGb9qzX-Jv3_w
//		 */
//		protected var _collaborativeExtension:CollaborativeDiagramEditPartExtension;

		/**
		 * @flowerModelElementId _LuQxAKvmEeGMNMaOJaUJQA
		 */
		private var scrollCallback:Function;

		public var editorStatefulClient:EditorStatefulClient;
		
		public function getEditorStatefulClientForSelectedElement():EditorStatefulClient {
			return editorStatefulClient;
		}
		
		/**
		 * Should update the (visual) content, based on the content received
		 * from the server (which may be full or incremental).
		 * 
		 * @flowerModelElementId _nEGnoKXOEeG-cPK59Sm4Wg
		 */
		public function executeContentUpdateLogic(content:Object, isFullContent:Boolean):void {
			throw new Error("This method should be implemented");
		}
		
		public function editableResourceStatusUpdated():void {
			if (editorStatefulClient.editableResourceStatus != null) {
				// may be null on app startup with 2 editors pointing to the same ER
				resourceStatusBar.updateState(editorStatefulClient.editableResourceStatus);
			}
		}
		
		public function EditorFrontend() { 
//			_collaborativeExtension = new CollaborativeDiagramEditPartExtension(this);			
			addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
			setStyle("verticalGap", 0);
		}
		
		/**
		 * Set the initial mode: editing enabled/disabled. The notification from
		 * the server may be quicker than the actual creation of the component, so that's
		 * why we check the resource status bar, which may already be populated. 
		 * @flowerModelElementId _9LdL0L3hEeGnuMQrZe-ELA
		 */
		protected function creationCompleteHandler(event:FlexEvent):void {
			removeEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
			if (resourceStatusBar.currentLockState == ResourceStatusBar.LOCKED_BY_ME
					|| resourceStatusBar.currentLockState == ResourceStatusBar.EDITABLE) {
				enableEditing();
			} else {
				disableEditing();
			}
		}

		/**
		 * Should disable the interraction with the editor/data. Called when the component
		 * is created and when it becomes locked.
		 * 
		 * @flowerModelElementId _jouM44-IEeGlZvO-ph04FQ
		 */
		public function disableEditing():void {
			throw "This method should be implemented";
		}
		
		/**
		 * Should enable the interraction with the editor/data. Called when the
		 * resource is unlocked.
		 * 
		 * @flowerModelElementId _jo2IsY-IEeGlZvO-ph04FQ
		 */
		public function enableEditing():void {
			throw "This method should be implemented";
		}
		
		/**
		 * The editor must be a DisplayObject that implements IEditor interface.
		 * 
		 * @flowerModelElementId _P7PxIKWLEeGNp8EWVqus7Q
		 */
		public function set editor(component:UIComponent):void {
			if (!(component is UIComponent)) {
				throw new Error("the editor must be a UIComponent");
			}
			_editor = component;
//			TextArea(_editor).addEventListener(FlexEvent.CREATION_COMPLETE, setHandlersForSyncronizationWithCollaborativeDiagram);
		}
		
//		private function setHandlersForSyncronizationWithCollaborativeDiagram(event:Event):void {
//			// Dana: we listen for the UPDATE_COMPLETE because we did not find another event 
//			// to have the right updated values for editorContentHeight and editorContentWidth 
//			// at the time of the event
//			TextArea(_editor).textDisplay.addEventListener(FlexEvent.UPDATE_COMPLETE, resizeCollaborativeDiagramToEditorDimensions);
//			TextArea(_editor).removeEventListener(FlexEvent.CREATION_COMPLETE, setHandlersForSyncronizationWithCollaborativeDiagram);
//		}
//		
//		protected function resizeCollaborativeDiagramToEditorDimensions(event:Event):void {
//			var newDiagramHeight:int = editorContentHeight > editor.height?editorContentHeight:editor.height;
//			var newDiagramWidth:int = editorContentWidth > editor.width?editorContentWidth:editor.width;
//			
//			//condition used for optimization
//			if (newDiagramHeight != _rootFigure.getVisualChildrenContainer().height || newDiagramWidth != _rootFigure.getVisualChildrenContainer().width) {
//				_rootFigure.setImperativeScrollableArea(newDiagramWidth, newDiagramHeight);
//			} 
//		}
		
		/**
		 * @flowerModelElementId _o57E0KvkEeGMNMaOJaUJQA
		 */
		public function get editorContentWidth():Number {
			// TODO CS/STFL hardcodare; tr. bagat in Text* probabil; idem mai jos. Si in acest caz referinta catre editor cred ca devine inutila
			return RichEditableText(TextArea(editor).textDisplay).contentWidth;
		}
		
		/**
		 * @flowerModelElementId _q3ioEKvkEeGMNMaOJaUJQA
		 */
		public function get editorContentHeight():Number {
			return RichEditableText(TextArea(editor).textDisplay).contentHeight;
		}
		
		public function get editor():UIComponent {
			return _editor;
		}
				
//		/**
//		 * @flowerModelElementId _0JxUYqiiEeGHceW7NM6-3w
//		 */
//		public function get rootFigure():RootFigure {
//			return _rootFigure;
//		}
		
		/**
		 * @flowerModelElementId _qnmYpKWDEeGRwJCiMM7wFA
		 */
		override protected function createChildren():void {
			super.createChildren();
			resourceStatusBar.percentWidth = 100; // Streach so that buttons may appear on the right side
			addChildAt(resourceStatusBar, 0);
			
			if (editor) {
				editor.percentHeight = 100;
				editor.percentWidth = 100;
				addChild(editor);
			}
			
//			_rootFigure = createRootFigure();
//			
//			editorAndDiagramContainer = createEditorAndDiagramContainer();	
//			
//			if (editorAndDiagramContainer != null) {
//				editorAndDiagramContainer.editor = _editor;	
//			}
//			
//			collaborativeDiagramViewer = createCollaborativeDiagramViewer();
//			
//			//The resourceStatusBar needs the collaborativeDiagramViewer to activate/deactivate the
//			// create collaborative tools
//			resourceStatusBar.collaborativeDiagramViewer = collaborativeDiagramViewer;
		}
		
//		/**
//		 * @flowerModelElementId _0JxUYKiiEeGHceW7NM6-3w
//		 */
//		protected function createRootFigure():RootFigure {
//			var rootFigure:RootFigure = new RootFigure(false, false);
//			rootFigure.setStyle("backgroundColor", 0xFFFFFF);
//			rootFigure.setStyle("backgroundAlpha", 0);
//			return rootFigure;
//		}
//		
//		/**
//		 * This component will contain the editor. Above the editor will have a diagram.
//		 * Can be null if an editor is not used. In this case the diagram (diagramPane) can be added as 
//		 * a child of this component.
//		 * 
//		 * @flowerModelElementId _wb0gYKWLEeGNp8EWVqus7Q
//		 */
//		protected function createEditorAndDiagramContainer():EditorAndDiagramContainer {
//			var container:EditorAndDiagramContainer = new EditorAndDiagramContainer(this);
//			container.rootFigure = _rootFigure;
//			container.editor = _editor;	
//			addChild(container);
//			return container;
//		}
//		
//		/**
//		 * This method is called after rootFigure has been added to stage.
//		 * Must create and activate the viewer.
//		 * 
//		 * @flowerModelElementId _4c0F4KWMEeGNp8EWVqus7Q
//		 */
//		protected function createCollaborativeDiagramViewer():DiagramViewer { 			
//			var viewer:CollaborativeDiagramViewer = new CollaborativeDiagramViewer(rootFigure, this);
//			viewer.pane = editorAndDiagramContainer;
//			viewer.activate();
//			return viewer;
//		}
//
//		
//		public function get collaborativeExtension():CollaborativeDiagramEditPartExtension {
//			return _collaborativeExtension;
//		}
//		
//		/**
//		 * @flowerModelElementId _vxVBgKvkEeGMNMaOJaUJQA
//		 */
//		public function setScrollCallback(callback:Function):void {
//			this.scrollCallback = callback;
//			// TODO CS/STFL
//			TextArea(editor).scroller.viewport.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onScroll);
//		}
//		
//		/**
//		 * @flowerModelElementId _g6naAKvmEeGMNMaOJaUJQA
//		 */
//		private function onScroll(e:PropertyChangeEvent):void { 
//			if (e.source == e.target) { 
//				var scroller:Scroller;
//				if (e.property == "verticalScrollPosition") {  
//					scroller = TextArea(editor).scroller;
//					scrollCallback.call(null, "verticalScrollPosition", scroller.verticalScrollBar.value);
//				} else if (e.property == "horizontalScrollPosition") {
//					scroller = TextArea(editor).scroller; // Flex bug compiler, nu gaseste proprietatea "value"
//					scrollCallback.call(null, "horizontalScrollPosition", scroller.horizontalScrollBar.value);   
//				}        	 		    
//			}             
//		} 

		/**
		 * Returns the dirty state of the <code>EditableResource</code> corresponding
		 * with the current editorInput.
		 * 
		 * @flowerModelElementId _uBxt8LbxEeGlK6b9EKaKdw
		 */
		public function isDirty():Boolean {	
			var editableResource:EditableResource = editorStatefulClient.editableResourceStatus;
			if (editableResource != null) {
				return editableResource.dirty;
			} else {
				return false;
			}
		}
		
//		private var _modalSpinner:ModalSpinner;
//		
//		public function get modalSpinner():ModalSpinner	{
//			return _modalSpinner;
//		}
//		
//		public function set modalSpinner(value:ModalSpinner):void {
//			_modalSpinner = value;
//		}
//		
//		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
//			super.updateDisplayList(unscaledWidth, unscaledHeight);
//			if (modalSpinner != null) {
//				modalSpinner.setActualSize(unscaledWidth, unscaledHeight);
//			}
//		}
//		
//		public function deactivateDiagramIfOneExists():void {
//			if (collaborativeDiagramViewer) {
//				collaborativeDiagramViewer.deactivate();
//			}
//		}
	}
}