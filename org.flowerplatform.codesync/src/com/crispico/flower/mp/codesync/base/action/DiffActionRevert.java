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
package com.crispico.flower.mp.codesync.base.action;

import com.crispico.flower.mp.codesync.base.Diff;
import com.crispico.flower.mp.codesync.base.IModelAdapter;
import com.crispico.flower.mp.codesync.base.Match;


/**
 * 
 */
public class DiffActionRevert extends DiffAction {

	@Override
	public ActionResult execute(Match match, int diffIndex) {
		Diff diff = match.getDiffs().get(diffIndex);
		IModelAdapter ancestorModelAdapter = match.getEditableResource().getModelAdapterFactorySet().getAncestorFactory().getModelAdapter(match.getAncestor());
		IModelAdapter leftModelAdapter = null;
		if (diff.isLeftModified())
			leftModelAdapter = match.getEditableResource().getModelAdapterFactorySet().getLeftFactory().getModelAdapter(match.getLeft());
		IModelAdapter rightModelAdapter = null;
		if (diff.isRightModified())
			rightModelAdapter = match.getEditableResource().getModelAdapterFactorySet().getRightFactory().getModelAdapter(match.getRight());
		
		Object value = ancestorModelAdapter.getValueFeatureValue(match.getAncestor(), diff.getFeature(), null);
		if (diff.isLeftModified()) {
			leftModelAdapter.setValueFeatureValue(match.getLeft(), diff.getFeature(), value);
		}
		if (diff.isRightModified()) {
			rightModelAdapter.setValueFeatureValue(match.getRight(), diff.getFeature(), value);
		}
		match.getDiffs().remove(diffIndex);
		
		return new ActionResult(false, false, false);
	}
}