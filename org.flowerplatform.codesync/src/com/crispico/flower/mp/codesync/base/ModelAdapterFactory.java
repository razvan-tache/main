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
package com.crispico.flower.mp.codesync.base;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class ModelAdapterFactory {
	
	protected static class ModelAdapterEntry {
		public Class<?> clazz;
		public IModelAdapter modelAdapter;
		public String extension;
		
		public ModelAdapterEntry() {
		}

		@Override
		public String toString() {
			return clazz.getSimpleName() + " " + modelAdapter.getClass().getSimpleName(); 
		}
	}
	
	/**
	 * O vom optimiza probabil cu un hashmap. Asta este modelul pentru care nu
	 * am pus functia de genul "IModelAdapter.isForType()", care ar fi implicat mereu
	 * o iteratie.
	 */
	protected Map<Object, ModelAdapterEntry> modelAdapters = new HashMap<Object, ModelAdapterEntry>();
	
	/**
	 * @author Cristi
	 * @author Mariana
	 */
	public IModelAdapter getModelAdapter(Object modelElement) {
		for (ModelAdapterEntry e : modelAdapters.values())
			if (e.clazz != null && e.clazz.isAssignableFrom(modelElement.getClass())) {
				if (e.extension == null || e.extension.equals(((IFile) modelElement).getFileExtension())) {
					return e.modelAdapter;
				}
			}
		return null;
	}

	public ModelAdapterEntry addModelAdapter(Class<?> clazz, IModelAdapter modelAdapter) {
		ModelAdapterEntry e = new ModelAdapterEntry();
		e.clazz = clazz;
		e.modelAdapter = modelAdapter;
		modelAdapters.put(clazz, e);
		return e;
	}
	
	/**
	 * @author Mariana
	 */
	public ModelAdapterEntry addModelAdapter(Class<?> clazz, IModelAdapter modelAdapter, String extension) {
		ModelAdapterEntry e = addModelAdapter(clazz, modelAdapter);
		e.extension = extension;
		return e;
	}
}