package com.crispico.flower.flexdiagram.action
{
	import com.crispico.flower.flexdiagram.IFigure;

	/**
	 * Basic data structure available for actions (in <code>IAction.context</code>).
	 * Actions can use it their methods; e.g. <code>isVisible()</code>; <code>run()</code>.
	 * 
	 * @see com.crispico.flower.flexdiagram.action.IAction#context
	 * @see com.crispico.flower.flexdiagram.action.CreateActionContext
	 * @see com.crispico.flower.flexdiagram.gantt.contextmenu.GanttCreateActionContext
	 * @author Sorin
	 * @flowerModelElementId _eui40IhTEeC3D4t2GHNvxQ
	 */ 	
	public dynamic class ActionContext {

		/**
		 * The main diagram figure (of type <code>IFigure</code>).
		 */ 
		public var diagramFigure:IFigure;
	}
}