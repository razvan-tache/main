package org.flowerplatform.editor.model {
	
	import org.flowerplatform.editor.EditorDescriptor;
	import org.flowerplatform.editor.EditorFrontend;
	import org.flowerplatform.editor.model.remote.DiagramEditorStatefulClient;
	import org.flowerplatform.editor.model.remote.NotationDiagramEditorStatefulClient;
	import org.flowerplatform.editor.remote.EditorStatefulClient;
	import org.flowerplatform.flexutil.layout.ViewLayoutData;

	/**
	 * @author Cristian Spiescu
	 * @author Cristina Constantinescu
	 */
	public class NotationDiagramEditorDescriptor extends EditorDescriptor {
		
		override public function getEditorName():String {
			return "diagram";
		}
		
		override protected function createViewInstance():EditorFrontend	{
			return new NotationDiagramEditorFrontend();
		}
		
		override protected function createEditorStatefulClient():EditorStatefulClient {
			return new NotationDiagramEditorStatefulClient();
		}
		
		public override function getId():String {	
			return "org.flowerplatform.editor.diagram";
		}
		
		public override function getIcon(viewLayoutData:ViewLayoutData=null):Object {	
			//			var result:Object = super.getIcon(viewLayoutData);
			//			if (result != null) {
			//				return result;
			//			} else {
			//				return EditorTextPlugin.getInstance().getResourceUrl("images/file.gif");
			//			}
			return null;
		}
		
		public override function getTitle(viewLayoutData:ViewLayoutData=null):String {	
			//			if (viewLayoutData == null) {
			//				return EditorTextPlugin.getInstance().getMessage("editor.text.name");
			//			} else {
			//				return viewLayoutData.customData.slice(viewLayoutData.customData.lastIndexOf("/") + 1);
			//			}
			//			return null;
			return "diagram ...";
		}
	}
}