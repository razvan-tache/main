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

import com.crispico.flower.mp.codesync.base.IModelAdapter;
import com.crispico.flower.mp.codesync.base.Match;

public class MatchActionRemoveLeft extends MatchActionRemoveAbstract {

	@Override
	protected Object getThis(Match match) {
		return match.getLeft();
	}

	@Override
	protected Object getOpposite(Match match) {
		return match.getRight();
	}

	@Override
	protected IModelAdapter getModelAdapter(Match match) {
		return match.getEditableResource().getModelAdapterFactorySet().getLeftFactory().getModelAdapter(match.getLeft());
	}

	@Override
	protected void unsetThis(Match match) {
		match.setDiffsModifiedLeft(false);
		match.setChildrenModifiedLeft(false);
		match.setLeft(null);
	}

}