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
package com.crispico.flower.mp.codesync.code.java.adapter;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;

import com.crispico.flower.mp.model.astcache.code.AstCacheCodePackage;
import com.crispico.flower.mp.model.codesync.CodeSyncPackage;

/**
 * Mapped by {@link AnnotationTypeMemberDeclaration}.
 * 
 * @author Mariana
 */
public class JavaAnnotationTypeMemberDeclarationModelAdapter extends JavaAbstractAstNodeModelAdapter {

	public static final String ANNOTATION_MEMBER = "javaAnnotationMember";
	
	@Override
	public Object getMatchKey(Object element) {
		return getAnnotationMember(element).getName().getIdentifier();
	}

	@Override
	public Object getValueFeatureValue(Object element, Object feature, Object correspondingValue) {
		if (CodeSyncPackage.eINSTANCE.getCodeSyncElement_Name().equals(feature)) {
			return getMatchKey(element);
		}
		if (CodeSyncPackage.eINSTANCE.getCodeSyncElement_Type().equals(feature)) {
			return ANNOTATION_MEMBER;
		}
		if (AstCacheCodePackage.eINSTANCE.getAnnotationMember_DefaultValue().equals(feature)) {
			return getStringFromExpression(getAnnotationMember(element).getDefault());
		}
		if (AstCacheCodePackage.eINSTANCE.getTypedElement_Type().equals(feature)) {
			return getStringFromType(getAnnotationMember(element).getType());
		}
		return super.getValueFeatureValue(element, feature, correspondingValue);
	}

	@Override
	public void setValueFeatureValue(Object element, Object feature, Object value) {
		if (CodeSyncPackage.eINSTANCE.getCodeSyncElement_Name().equals(feature)) {
			AnnotationTypeMemberDeclaration member = getAnnotationMember(element);
			member.setName(member.getAST().newSimpleName((String) value));
		}
		if (AstCacheCodePackage.eINSTANCE.getAnnotationMember_DefaultValue().equals(feature)) {
			AnnotationTypeMemberDeclaration member = getAnnotationMember(element);
			member.setDefault(getExpressionFromString(member.getAST(), (String) value));
		}
		if (AstCacheCodePackage.eINSTANCE.getTypedElement_Type().equals(feature)) {
			AnnotationTypeMemberDeclaration member = getAnnotationMember(element);
			member.setType(getTypeFromString(member.getAST(), (String) value));
		}
		super.setValueFeatureValue(element, feature, value);
	}

	@Override
	public Object createCorrespondingModelElement(Object element) {
		return AstCacheCodePackage.eINSTANCE.getAstCacheCodeFactory().createAnnotationMember();
	}

	@Override
	public List<?> getChildren(Object modelElement) {
		return Collections.emptyList();
	}
	
	private AnnotationTypeMemberDeclaration getAnnotationMember(Object element) {
		return (AnnotationTypeMemberDeclaration) element;
	}

}