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
package  org.flowerplatform.communication.progress_monitor.remote {
	import org.flowerplatform.communication.CommunicationPlugin;
	import org.flowerplatform.communication.stateful_service.IStatefulClientLocalState;
	import org.flowerplatform.communication.stateful_service.StatefulClient;
	import org.flowerplatform.flexutil.FlexUtilGlobals;
	import org.flowerplatform.flexutil.popup.IProgressMonitor;
	import org.flowerplatform.flexutil.popup.IProgressMonitorHandler;
		
	/**
	 * @author Sorin
	 * @author Cristina Constantinescu
	 * 
	 */
	public class ProgressMonitorStatefulClient extends StatefulClient implements IProgressMonitorHandler {
		
		/**
		 * 
		 */
		private static const SERVICE_ID:String = "ProgressMonitorStatefulService";
	
		private static var lastUsedId:Number = 0;
		
		private var statefulClientId:String;
		
		private var title:String;
		
		private var createdByServer:Boolean;
		
		/**
		 * If <code>true</code>, a cancel button will be displayed on client side 
		 * to allow cancellation.
		 * 
		 * @author Cristina
		 */
		private var allowCancellation:Boolean;
		
		private var progressMonitor:IProgressMonitor;
	
		/**
		 * 
		 */
		public function ProgressMonitorStatefulClient(title:String, statefulClientId:String, createdByServer:Boolean, allowCancellation:Boolean=true) {
			this.title = title;
			this.statefulClientId = createdByServer ? statefulClientId : "ClientProgresMonitor" + (++ lastUsedId); // Use or generate new.
			this.createdByServer = createdByServer;
			this.allowCancellation = allowCancellation;
		}
		
		/**
		 * 
		 */
		public override function getStatefulServiceId():String {
			return SERVICE_ID;
		}
		
		/**
		 * 
		 */
		public override function getStatefulClientId():String {
			return statefulClientId;
		}
		
		/**
		 * 
		 * TODO Sorin : de vorbit cu cristi sa spuna ca subscribe in in urma unui reconnect? 
		 * 
		 * See CreateProgressMonitorStatefulClient#execute() for register dataRegistrator != null.
		 * See #createAndShowProgressMonitor() for register dataRegistrator != null. 
		 * 
		 * 
		 */
		public override function getCurrentStatefulClientLocalState(dataFromRegistrator:Object = null):IStatefulClientLocalState {
			return new ProgressMonitorStatefulLocalClient(title, /* after reconnect */ dataFromRegistrator == null) ;
		}

		/**
		 * Package visibility because only the monitor dialog can call it.
		 * 
		 */
		public function attemptCancelProgressMonitor():void {
			invokeServiceMethod("attemptCancelProgressMonitor", null);
		}
		
		override public function afterAddInStatefulClientRegistry():void {
			// When added to the registy also show popup
			progressMonitor = FlexUtilGlobals.getInstance().progressMonitorHandlerFactory.createProgressMonitor()
				.setAllowCancellation(allowCancellation)
				.setTitle(title)
				.setHandler(this);
			
			progressMonitor.show();			
		}
		 
		override public function subscribeToStatefulService(dataFromRegistrator:Object):void {
			if (!createdByServer || dataFromRegistrator == null ) // Server already knows it, no need to subscribe, or reconnect
				super.subscribeToStatefulService(dataFromRegistrator);
		}
		
		override public function unsubscribeFromStatefulService(dataFromUnregistrator:Object):Boolean {
			return true; // Just unregister, the unsubscription was done already on the server.
		}
		
		override protected function removeUIAndRelatedElementsAndStatefulClientBecauseUnsubscribedForcefully():void {
			closeProgressMonitor();
		}
		
		/**
		 * Returns Progress monitor Stateful Client Id to be passed to the server.
		 * @see ProgressMonitorStatefulService#getProgressMonitor(progressMonitorStatefulClientId)
		 */ 
		public static function createAndShowProgressMonitor(title:String):ProgressMonitorStatefulClient {
			var progressMonitorStatefulClient:ProgressMonitorStatefulClient = new ProgressMonitorStatefulClient(title, null /* generate statefulClientId */, false /* created by client */);
			CommunicationPlugin.getInstance().statefulClientRegistry.register(progressMonitorStatefulClient, new Object() /* != null so not as reconnect */); 
			return progressMonitorStatefulClient;
		}
		
		override public function toString():String {
			return "ProgressMonitorStatefulClient statefulClientId = " + statefulClientId + " title = " + title;  
		}
				
		///////////////////////////////////////////////////////////////
		//@RemoteInvocation methods
		///////////////////////////////////////////////////////////////
		
		/**
		 * 
		 */
		[RemoteInvocation]
		public function beginProgressMonitor(name:String, totalWork:int):void {
			progressMonitor.setTotalWork(totalWork).setLabel(name);
		}
		
		/**
		 * 
		 */
		[RemoteInvocation]
		public function updateProgressMonitor(name:String, workUntilNow:int):void {
			progressMonitor.setWorked(workUntilNow).setLabel(name);			
		}
		
		/**
		 * Hides the monitor and unregisters it.
		 * 
		 */
		[RemoteInvocation]
		public function closeProgressMonitor():void {
			progressMonitor.close();
			CommunicationPlugin.getInstance().statefulClientRegistry.unregister(this, null);
		}
		
	}
}