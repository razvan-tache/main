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
package com.crispico.flower.mp.codesync.code;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.flowerplatform.common.plugin.AbstractFlowerJavaPlugin;
import org.flowerplatform.communication.CommunicationPlugin;
import org.flowerplatform.communication.channel.CommunicationChannel;
import org.flowerplatform.communication.stateful_service.StatefulServiceInvocationContext;
import org.flowerplatform.editor.remote.EditorStatefulClientLocalState;
import org.flowerplatform.emf_model.notation.util.NotationAdapterFactory;
import org.flowerplatform.web.projects.remote.ProjectsService;
import org.osgi.framework.BundleContext;

import com.crispico.flower.mp.codesync.base.CodeSyncAlgorithm;
import com.crispico.flower.mp.codesync.base.CodeSyncEditableResource;
import com.crispico.flower.mp.codesync.base.CodeSyncPlugin;
import com.crispico.flower.mp.codesync.base.Match;
import com.crispico.flower.mp.codesync.base.ModelAdapterFactorySet;
import com.crispico.flower.mp.codesync.base.communication.CodeSyncEditorStatefulService;
import com.crispico.flower.mp.codesync.code.adapter.AstModelElementAdapter;
import com.crispico.flower.mp.codesync.code.adapter.FolderModelAdapter;
import com.crispico.flower.mp.codesync.merge.CodeSyncMergePlugin;
import com.crispico.flower.mp.model.astcache.code.Annotation;
import com.crispico.flower.mp.model.astcache.code.AnnotationValue;
import com.crispico.flower.mp.model.astcache.code.AstCacheCodeFactory;
import com.crispico.flower.mp.model.astcache.code.AstCacheCodePackage;
import com.crispico.flower.mp.model.astcache.code.Class;
import com.crispico.flower.mp.model.astcache.code.ExtendedModifier;
import com.crispico.flower.mp.model.astcache.code.ModifiableElement;
import com.crispico.flower.mp.model.astcache.code.Modifier;
import com.crispico.flower.mp.model.astcache.code.Operation;
import com.crispico.flower.mp.model.astcache.code.Parameter;
import com.crispico.flower.mp.model.astcache.code.util.AstCacheCodeAdapterFactory;
import com.crispico.flower.mp.model.codesync.AstCacheElement;
import com.crispico.flower.mp.model.codesync.CodeSyncElement;
import com.crispico.flower.mp.model.codesync.CodeSyncPackage;
import com.crispico.flower.mp.model.codesync.CodeSyncRoot;
import com.crispico.flower.mp.model.codesync.FeatureChange;
import com.crispico.flower.mp.model.codesync.util.CodeSyncAdapterFactory;

/**
 * @author Cristi
 */
public class CodeSyncCodePlugin extends AbstractFlowerJavaPlugin {
	
	public static String MARKER_ANNOTATION = "_MARKER";
	
	public static String SINGLE_MEMBER_ANNOTATION = "_SINGLE_MEMBER";
	 
	public static String NORMAL_ANNOTATION = "_NORMAL";
	
	public final static String SINGLE_MEMBER_ANNOTATION_VALUE_NAME = "_";
	
	protected static CodeSyncCodePlugin INSTANCE;
	
	public static CodeSyncCodePlugin getInstance() {
		return INSTANCE;
	}
	
	protected ModelAdapterFactorySetProvider modelAdapterFactorySetProvider;
	
	protected List<String> srcDirs = null;
	
	/**
	 * @see #getOrCreateEditingDomain()
	 * 
	 * @author Mariana
	 */
	protected Map<IProject, AdapterFactoryEditingDomain> editingDomains = new HashMap<IProject, AdapterFactoryEditingDomain>();
	
	private Utils utils = new Utils();
	
	/**
	 * The location of the CSE mapping file, relative to the project. May be
	 * configurable in the future.
	 * 
	 * @author Mariana
	 */
	public String CSE_MAPPING_FILE_LOCATION = "/CSE.notation";
	
	/**
	 * The location of the ACE file, relative to the project. May be configurable
	 * in the future.
	 * 
	 * @author Mariana
	 */
	public String ACE_FILE_LOCATION = "/ACE.notation";
	
	/**
	 * @author Mariana
	 */
	public ModelAdapterFactorySetProvider getModelAdapterFactorySetProvider() {
		return modelAdapterFactorySetProvider;
	}

	/**
	 * @author Mariana
	 */
	public Utils getUtils() {
		return utils;
	}
	
	/**
	 * @author Cristi
	 * @author Mariana
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		INSTANCE = this;
		
		initializeExtensionPoint_modelAdapterFactorySet();
	}
	
	private void initializeExtensionPoint_modelAdapterFactorySet() throws CoreException {
		modelAdapterFactorySetProvider = new ModelAdapterFactorySetProvider();
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.flowerplatform.codesync.code.modelAdapterFactorySet");
		for (IConfigurationElement configurationElement : configurationElements) {
			String id = configurationElement.getAttribute("id");
			String technology = configurationElement.getAttribute("technology");
			Object instance = configurationElement.createExecutableExtension("modelAdapterFactorySetClass");
			modelAdapterFactorySetProvider.getFactorieSets().put(technology, (ModelAdapterFactorySet) instance);
			logger.debug("Added CodeSync ModelAdapterFactorySet with id = {} with class = {}", id, instance.getClass());
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		INSTANCE = null;
	}

	@Override
	public void registerMessageBundle() throws Exception {
		// nothing to do yet
	}
	
	/**
	 * @author Cristi
	 * @author Mariana
	 */
//	public CodeSyncElement getCodeSyncElement(String fullyQualifiedName, String technology, CommunicationChannel communicationChannel) {
	public CodeSyncElement getCodeSyncElement(IProject project, IResource file, String technology, CommunicationChannel communicationChannel, boolean showDialog) {
//		if (fullyQualifiedName.startsWith("/")) {
//			fullyQualifiedName = fullyQualifiedName.substring(1);
//		}
//		String[] pathFragments = fullyQualifiedName.split("/");
//		
//		// STEP 1 : identify the project corresponding to the 1st path fragment and find the CSE mapping file
//		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pathFragments[0]);
//		if (!project.exists()) {
//			throw new RuntimeException("Project " + pathFragments[0] + " does not exist!");
//		}
//		
//		// TEST : if there is already an open ER for this project, don't perform sync
//		CodeSyncEditorStatefulService service = (CodeSyncEditorStatefulService) ServiceRegistry.INSTANCE.getService(CodeSyncEditorStatefulService.SERVICE_ID);
//		CodeSyncEditableResource editableResource = (CodeSyncEditableResource) service.getEditableResource(project.getFullPath().toString());
//		if (editableResource != null) {
//			service.subscribeClientForcefully(communicationChannel, project.getFullPath().toString());
//			return null;
//		}
		
		ResourceSet resourceSet = CodeSyncPlugin.getInstance().getOrCreateResourceSet(project, "diagramEditorStatefulService");
		
		Resource cseResource = getCodeSyncMapping(project, resourceSet);
		
		// STEP 2 : find the SrcDir corresponding to the 2nd path fragment
//		CodeSyncElement srcDir = getSrcDir(cseResource, pathFragments[1]);
		CodeSyncElement srcDir = getSrcDir(cseResource, file.getFullPath().segment(2));
		
//		// STEP 3 : find the CSE corresponding to the name
		String[] path = Arrays.copyOfRange(file.getFullPath().segments(), 3, file.getFullPath().segments().length);
		CodeSyncElement codeSyncElement = getCodeSyncElement(srcDir, path);
		if (codeSyncElement == null) {
			// SUBSTEP : run the CodeSync algorithm if the CSE does not exist
//			runCodeSyncAlgorithm(srcDir, project, pathFragments[1], technology, communicationChannel);
			runCodeSyncAlgorithm(srcDir, project, resourceSet, file.getFullPath().segment(1).concat("/" + file.getFullPath().segment(2)), technology, communicationChannel, showDialog);
		} else {
			if (showDialog) {
				runCodeSyncAlgorithm(srcDir, project, resourceSet, file.getFullPath().segment(1).concat("/" + file.getFullPath().segment(2)), technology, communicationChannel, showDialog);
			}
			return codeSyncElement;
		}
		return getCodeSyncElement(srcDir, path);
	}
	
	/**
	 * Finds the {@link CodeSyncElement} corresponding to the <code>path</code> by traversing the CSE tree
	 * rooted at the <code>srcDir</code>.
	 * 
	 * @author Mariana
	 */
	public CodeSyncElement getCodeSyncElement(CodeSyncElement srcDir, String[] path) {
		CodeSyncElement codeSyncElement = srcDir;
		boolean foundForPath = true;
		for (int i = 0; i < path.length; i++) {
			boolean foundChild = false;
			for (CodeSyncElement cse : codeSyncElement.getChildren()) {
				if (cse.getName().equals(path[i])) {
					codeSyncElement = cse;
					foundChild = true;
					break;
				}
			}
			if (!foundChild) {
				foundForPath = false;
				break;
			}
		}
		if (foundForPath) {
			return codeSyncElement;
		}
		return null;
	}
	
	/**
	 * @author Mariana
	 */
	public CodeSyncEditableResource runCodeSyncAlgorithm(CodeSyncElement model, IProject project, ResourceSet resourceSet, String path, String technology, CommunicationChannel communicationChannel, boolean showDialog) {
//	public CodeSyncEditableResource runCodeSyncAlgorithm(CodeSyncElement model, IProject project, IResource file, String technology, CommunicationChannel communicationChannel) {
		CodeSyncEditorStatefulService service = (CodeSyncEditorStatefulService) CommunicationPlugin.getInstance().getServiceRegistry().getService(CodeSyncEditorStatefulService.SERVICE_ID);
		String editableResourcePath = project.getFullPath().toString();
		CodeSyncEditableResource editableResource; // = (CodeSyncEditableResource) service.getEditableResource(project.getFullPath().toString());
		if (showDialog) {
			editableResource = (CodeSyncEditableResource) service.subscribeClientForcefully(communicationChannel, editableResourcePath);
		} else {
			service.subscribe(new StatefulServiceInvocationContext(communicationChannel), new EditorStatefulClientLocalState(editableResourcePath));
			editableResource = (CodeSyncEditableResource) service.getEditableResource(editableResourcePath);
		}
//		CodeSyncEditableResource editableResource = new CodeSyncEditableResource();
	
		Match match = new Match();
		match.setAncestor(model);
		match.setLeft(model);
		IResource ast = null;
		if (model.getType().equals(AstModelElementAdapter.FOLDER)) {
			ast = project.getFolder(path);
		} else {
			if (model.getType().equals(AstModelElementAdapter.FILE)) {
				ast = project.getFile(path);
			}
		}
//		ast = file.getParent();
		match.setRight(ast);
		
		match.setEditableResource(editableResource);
		editableResource.setMatch(match);
		ModelAdapterFactorySet modelAdapterFactorySet = getModelAdapterFactorySetProvider().getFactorieSets().get(technology);
		modelAdapterFactorySet.initialize(getAstCache(project, resourceSet));
		editableResource.setModelAdapterFactorySet(modelAdapterFactorySet);
		
		new CodeSyncAlgorithm(editableResource.getModelAdapterFactorySet()).generateDiff(match);
		
//		StatefulServiceInvocationContext context = new StatefulServiceInvocationContext(communicationChannel);
//		service.attemptUpdateEditableResourceContent(context, project.getFullPath().toString(), null);
		
		if (!showDialog) {
			// we're not showing the dialog => perform sync
			StatefulServiceInvocationContext context = new StatefulServiceInvocationContext(communicationChannel);
			service.synchronize(context, editableResourcePath);
			// and unsubscribe
			service.unsubscribeAllClientsForcefully(editableResourcePath, false);
		}
		
		return editableResource;
//		return null;
	}
	
	/**
	 * @author Mariana
	 */
	public Resource getCodeSyncMapping(IProject project, ResourceSet resourceSet) {
		File codeSyncElementMappingFile = ProjectsService.getInstance().getFileFromProjectWrapperResource(project.getFile(new Path(ProjectsService.LINK_TO_PROJECT + CSE_MAPPING_FILE_LOCATION)));
		Resource cseResource = CodeSyncPlugin.getInstance().getResource(resourceSet, codeSyncElementMappingFile);
		if (!codeSyncElementMappingFile.exists()) {
			// first clear the resource in case the mapping file was deleted 
			// after it has been loaded at a previous moment
			cseResource.getContents().clear();
			
			for (String srcDir : getSrcDirs()) {
				CodeSyncRoot cseRoot = (CodeSyncRoot) getRoot(cseResource, srcDir);
				if (cseRoot == null) {
					// create the CSE for the SrcDir
					cseRoot = CodeSyncPackage.eINSTANCE.getCodeSyncFactory().createCodeSyncRoot();
					cseRoot.setName(srcDir);
					cseRoot.setType(FolderModelAdapter.FOLDER);
				}
				cseResource.getContents().add(cseRoot);
			}
			
			CodeSyncPlugin.getInstance().saveResource(cseResource);
		}
		return cseResource;
	}
	
	/**
	 * @author Mariana
	 */
	public Resource getAstCache(IProject project, ResourceSet resourceSet) {
		File astCacheElementFile = ProjectsService.getInstance().getFileFromProjectWrapperResource(project.getFile(new Path(ProjectsService.LINK_TO_PROJECT + ACE_FILE_LOCATION)));
		return CodeSyncPlugin.getInstance().getResource(resourceSet, astCacheElementFile);
	}
	
	/**
	 * @author Mariana
	 */
	protected CodeSyncRoot getRoot(Resource resource, String srcDir) {
		for (EObject eObj : resource.getContents()) {
			if (eObj instanceof CodeSyncRoot) {
				CodeSyncRoot root = (CodeSyncRoot) eObj;
				if (root.getName().equals(srcDir))
					return root;
			}
		}
		return null;
	}
	
	/**
	 * @author Mariana
	 */
	public CodeSyncElement getSrcDir(Resource resource, String name) {
		CodeSyncElement srcDir = null;
		for (EObject member : resource.getContents()) {
			if (((CodeSyncElement) member).getName().equals(name)) {
				srcDir = (CodeSyncElement) member;
				break;
			}
		}
		if (srcDir == null) {
			throw new RuntimeException("SrcDir " + name + " is not mapped to a CSE!");
		}
		return srcDir;
	}
	
	/**
	 * @author Mariana
	 */
	public List<String> getSrcDirs() {
		if (srcDirs == null) {
			// TODO Mariana : get user input
			return Collections.singletonList("src");
		} 
		return srcDirs;
	}
	
	public void addSrcDir(String srcDir) {
		if (srcDirs == null) {
			srcDirs = new ArrayList<String>();
		}
		if (!srcDirs.contains(srcDir)) {
			srcDirs.add(srcDir);
		}
	}

	/**
	 * @author Mariana
	 */
	public class Utils {

		public void addFeatureChange(CodeSyncElement cse, EStructuralFeature feature, FeatureChange featureChange) {
			cse.getFeatureChanges().put(feature, featureChange);
		}
		
		public void removeFeatureChange(CodeSyncElement cse, Object feature) {
			cse.getFeatureChanges().removeKey(feature);
		}
		
		public void addChild(CodeSyncElement parent, CodeSyncElement child) {
			parent.getChildren().add(child);
		}
		
		public void addAnnotationValue(Annotation annotation, AnnotationValue value) {
			annotation.getValues().add(value);
		}
		
		public void setSuperClasses(Class cls, List<String> superClasses) {
			cls.getSuperClasses().clear();
			cls.getSuperClasses().addAll(superClasses);
		}
		
		public void setSuperInterfaces(Class cls, List<String> superInterfaces) {
			cls.getSuperInterfaces().clear();
			cls.getSuperInterfaces().addAll(superInterfaces);
		}
		
		public void setModifiers(ModifiableElement op, List<ExtendedModifier> modifiers) {
			op.getModifiers().clear();
			op.getModifiers().addAll(modifiers);
		}
		
		public void setParams(Operation op, List<Parameter> params) {
			op.getParameters().clear();
			op.getParameters().addAll(params);
		}
		
		public void addToResource(IProject project, ResourceSet resourceSet, AstCacheElement ace) {
			getAstCache(project, resourceSet).getContents().add(ace);
		}
		
		public boolean testEquality(Resource expected, Resource actual, String name) {
			return EcoreUtil.equals(getSrcDir(expected, name), getSrcDir(actual, name));
		}
	}
	
	/**
	 * TODO Mariana : move to test
	 */
	public boolean testValueAsString() {
		// STEP 1 : create FeatureChange
		CodeSyncElement expectedCSE = CodeSyncPackage.eINSTANCE.getCodeSyncFactory().createCodeSyncElement();
		FeatureChange expectedChange = CodeSyncPackage.eINSTANCE.getCodeSyncFactory().createFeatureChange();
		expectedCSE.getFeatureChanges().put(AstCacheCodePackage.eINSTANCE.getModifiableElement_Modifiers(), expectedChange); 
		EList<ExtendedModifier> modifiers = new BasicEList<ExtendedModifier>();
		Modifier modifier = AstCacheCodeFactory.eINSTANCE.createModifier();
		modifier.setType(3);
		modifiers.add(modifier);
		expectedChange.setOldValue(modifiers);
		
		// STEP 2 : put FC in resource and save
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		try {
			if (!project.exists()) {
				project.create(null);
			}
			project.open(null);
			ResourceSet resourceSet = CodeSyncPlugin.getInstance().getOrCreateResourceSet(project, "diagramEditorStatefulService");
			IFile codeSyncElementsFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("test/CSE.notation"));
			Resource resource = null;
			if (!codeSyncElementsFile.exists()) {
				resource = CodeSyncPlugin.getInstance().getResource(resourceSet, codeSyncElementsFile);
				resource.getContents().add(expectedCSE);
				CodeSyncPlugin.getInstance().saveResource(resource);
			}
			
			CodeSyncPlugin.getInstance().discardResource(resource);
			
			// STEP 3 : get value from resource
			resource = CodeSyncPlugin.getInstance().getResource(resourceSet, codeSyncElementsFile);
//					assertEquals(1, resource.getContents().size());
			CodeSyncElement actualCSE = (CodeSyncElement) resource.getContents().get(0);
			FeatureChange actualChange = actualCSE.getFeatureChanges().get(AstCacheCodePackage.eINSTANCE.getModifiableElement_Modifiers());
//					assertNotNull(actualChange);
			EList<ExtendedModifier> actual = (EList<ExtendedModifier>) actualChange.getOldValue();
//					assertEquals(1, actual.size());
//					assertEquals(3, ((Modifier) actual.get(0)).getType());
			return ((Modifier) actual.get(0)).getType() == 3;
		} catch (CoreException e) {
			return false;
		}
	}
	
}