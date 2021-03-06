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
package org.flowerplatform.editor.mindmap.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.flowerplatform.emf_model.notation.View;

import com.crispico.flower.mp.codesync.wiki.WikiPlugin;
import com.crispico.flower.mp.model.codesync.CodeSyncElement;

/**
 * @author Cristina Constantinescu
 */
public class MindMapHeadlineProcessor extends AbstractMindMapChangeProcessor {

	protected void processFeatureChange(EObject object, FeatureChange featureChange, View associatedViewOnOpenDiagram, Map<String, Object> viewDetails) {
		super.processFeatureChange(object, featureChange, associatedViewOnOpenDiagram, viewDetails);
		
//		List<String> icons = new ArrayList<String>();		
//		if (WikiPlugin.HEADING_LEVEL_1_CATEGORY.equals(((CodeSyncElement) object).getType())) {
//			icons.add("images/full-1.png");
//		} else if (WikiPlugin.HEADING_LEVEL_2_CATEGORY.equals(((CodeSyncElement) object).getType())) {
//			icons.add("images/full-2.png");
//		} else if (WikiPlugin.HEADING_LEVEL_3_CATEGORY.equals(((CodeSyncElement) object).getType())) {
//			icons.add("images/full-3.png");
//		}	
//		viewDetails.put("icons", icons);
	}
	
}