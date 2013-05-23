package  com.crispico.flower.flexdiagram.event {
	import com.crispico.flower.flexdiagram.EditPart;
	
	import flash.events.Event;

	/**
	 * Dispatched by EditParts when the associated connections
	 * should update. If the EditPart has a figure, UI events (of
	 * the figure and/or parent figure(s)) should
	 * trigger this event (e.g. move, resize). If not, this event
	 * should be dispatched as an effect of some modifications of
	 * the model (and/or parent model). Received by ConnectionEditParts.
	 * 
	 * @see EditPart.dispatchUpdateConnectionEndsEvent()
  	 * @see ConnectionEditPart.updateConnectionsHandler()
	 * @author Cristi
	 */
	public class UpdateConnectionEndsEvent extends Event {
	
		public static const UPDATE_CONNECTION_ENDS:String = "UpdateConnectionEndsEvent";
		
		public var editPart:EditPart;
		
		public function UpdateConnectionEndsEvent(editPart:EditPart):void {
			super(UPDATE_CONNECTION_ENDS);
			this.editPart = editPart;
		}

	}
}