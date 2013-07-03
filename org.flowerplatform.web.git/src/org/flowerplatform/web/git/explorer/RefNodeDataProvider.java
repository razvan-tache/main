package org.flowerplatform.web.git.explorer;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.flowerplatform.communication.stateful_service.StatefulServiceInvocationContext;
import org.flowerplatform.communication.tree.GenericTreeContext;
import org.flowerplatform.communication.tree.INodeDataProvider;
import org.flowerplatform.communication.tree.remote.PathFragment;
import org.flowerplatform.communication.tree.remote.TreeNode;
import org.flowerplatform.web.git.GitNodeType;
import org.flowerplatform.web.git.GitPlugin;
import org.flowerplatform.web.git.explorer.entity.RefNode;

/**
 * @author Cristina Constantienscu
 */
public class RefNodeDataProvider implements INodeDataProvider {

	@Override
	public boolean populateTreeNode(Object source, TreeNode destination,	GenericTreeContext context) {
		RefNode refNode = (RefNode) source;
		String nodeType = destination.getPathFragment().getType();
		
		destination.setLabel(Repository.shortenRefName(refNode.getRef().getName()));
		String icon;
		if (GitNodeType.NODE_TYPE_TAG.equals(nodeType)) {
			icon = "images/full/obj16/annotated-tag.gif";
		} else {
			icon = "images/full/obj16/branch_obj.gif";
		}
		destination.setIcon(GitPlugin.getInstance().getResourceUrl(icon));
		return true;
	}

	@Override
	public PathFragment getPathFragmentForNode(Object node, String nodeType, GenericTreeContext context) {
		RefNode refNode = (RefNode) node;
		return new PathFragment(refNode.getRef().getName(), nodeType);
	}
	
	@Override
	public String getLabelForLog(Object node, String nodeType) {
		RefNode refNode = (RefNode) node;
		return refNode.getRef().getName();
	}

	@Override
	public String getInplaceEditorText(StatefulServiceInvocationContext context, List<PathFragment> fullPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setInplaceEditorText(StatefulServiceInvocationContext context, List<PathFragment> path, 	String text) {
		throw new UnsupportedOperationException();
	}

}
