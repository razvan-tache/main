package org.flowerplatform.web.svn.explorer;

import java.util.Collections;

import org.flowerplatform.communication.tree.GenericTreeContext;
import org.flowerplatform.communication.tree.IGenericTreeStatefulServiceAware;
import org.flowerplatform.communication.tree.remote.GenericTreeStatefulService;
import org.flowerplatform.communication.tree.remote.TreeNode;
import org.flowerplatform.web.explorer.AbstractVirtualItemChildrenProvider;
import org.flowerplatform.web.svn.SvnNodeType;
import org.flowerplatform.web.svn.SvnPlugin;
import org.tigris.subversion.subclipse.core.SVNException;

/**
 * Parent node = Organization (i.e. File).<br/>
 * Child node is a virtual item, i.e. Pair<Org File, nodeType>.
 * 
 * @author Victor Badila
 * 
 * @flowerModelElementId _UEfo0P2kEeKrJqcAep-lCg
 */

public class SvnRepositories_OrganizationChildrenProvider extends
		AbstractVirtualItemChildrenProvider implements
		IGenericTreeStatefulServiceAware {
	/**
	 * @throws SVNException
	 * @flowerModelElementId _DjxSYP2tEeKrJqcAep-lCg
	 */
	
	public SvnRepositories_OrganizationChildrenProvider() throws SVNException {
		super();
		childNodeTypes = Collections
				.singletonList(SvnNodeType.NODE_TYPE_SVN_REPOSITORIES);

	}

	/**
	 * @flowerModelElementId _Upao4P2mEeKrJqcAep-lCg
	 */

	@Override
	public void setGenericTreeStatefulService(
			GenericTreeStatefulService genericTreeStatefulService) {
		SvnPlugin.getInstance().getTreeStatefulServicesDisplayingSvnContent()
				.add(genericTreeStatefulService);
	}

	/**
	 * In faza 2, cand folosim BD, tr sa ne uitam acolo sa vedem daca avem date.
	 * 
	 * @flowerModelElementId _RAmGAP6AEeKrJqcAep-lCg
	 */
	
	// TODO following code was commented because for the moment a 'virtual' SVNRepositories node is created
	
	// public Collection<Pair<Object, String>> getChildrenForNode(Object node,
	// TreeNode treeNode,
	// GenericTreeContext context) {
	// // TODO Auto-generated by Flower Dev Center
	//
	// if(!nodeHasChildren(node, treeNode, context))
	// return null;
	//
	//
	// Pair<Object, String> realChild;
	// Pair<Object, String> child;
	//
	// Collection<Pair<Object, String>> result = new ArrayList<Pair<Object,
	// String>>();
	//
	// for (String childNodeType : childNodeTypes) {
	// realChild = new Pair<Object, String>(node, childNodeType);
	// child = new Pair<Object, String>(realChild, childNodeType);
	//
	// result.add(child);
	// }
	// return result;
	// Collection<Pair<Object, String>> myCollection = new
	// ArrayList<Pair<Object, String>>();
	//
	//
	// myCollection.add(new Pair("SvnRepositories",childNodeTypes.get(0)));
	// return myCollection;
	// return null;
	// }

	/**
	 * @flowerModelElementId _RA3y0P6AEeKrJqcAep-lCg
	 */
	public Boolean nodeHasChildren(Object node, TreeNode treeNode,
			GenericTreeContext context) {
		// TODO Auto-generated by Flower Dev Center
		return true;
	}
}