package org.flowerplatform.flexutil.plugin {
	import flash.display.Loader;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.net.URLRequest;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	
	import mx.collections.ArrayCollection;

	/**
	 * @author Cristi
	 */
	public class FlexPluginManager {
		
		protected static var INSTANCE:FlexPluginManager;
		
		public static function getInstance():FlexPluginManager {
			if (INSTANCE == null) {
				INSTANCE = new FlexPluginManager();
			}
			return INSTANCE;
		}
		
		public var flexPluginEntries:ArrayCollection = new ArrayCollection();
		
		public var currentLoadingSession:FlexPluginLoadingSession;
		
		protected function createLoadingSession():FlexPluginLoadingSession {
			return new FlexPluginLoadingSession(this);
		}
		
		public function loadPlugins(pluginSwcUrls:ArrayCollection, callbackFunction:Function = null, callbackObject:Object = null):void {
			if (currentLoadingSession != null) {
				throw new Error("A loading session is already in progress. Need to wait until it's finished");
			}
			
			if (pluginSwcUrls == null || pluginSwcUrls.length == 0) {
				throw new Error("A null or empty list of plugin urls was passed as param");
			}
			
			currentLoadingSession = createLoadingSession();
			currentLoadingSession.loadPlugins(pluginSwcUrls, callbackFunction, callbackObject);
		}
			
	}
}