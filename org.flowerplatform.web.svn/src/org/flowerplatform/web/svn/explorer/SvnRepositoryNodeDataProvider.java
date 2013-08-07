package org.flowerplatform.web.svn.explorer;


import java.util.List;

import org.flowerplatform.communication.stateful_service.StatefulServiceInvocationContext;
import org.flowerplatform.communication.tree.GenericTreeContext;
import org.flowerplatform.communication.tree.INodeDataProvider;
import org.flowerplatform.communication.tree.remote.PathFragment;
import org.flowerplatform.communication.tree.remote.TreeNode;

/**
 * @flowerModelElementId _aF3AMP5mEeKrJqcAep-lCg
 */
public class SvnRepositoryNodeDataProvider implements INodeDataProvider {
	/**
	 * @flowerModelElementId _iQKIIP5mEeKrJqcAep-lCg
	 */
	public PathFragment getPathFragmentForNode(Object node, String nodeType,
			GenericTreeContext context) {
		// TODO implement
		return null;
	}

	/**
	 * @flowerModelElementId _-KimAP5_EeKrJqcAep-lCg
	 */
	public String getInplaceEditorText(
			StatefulServiceInvocationContext context,
			List<PathFragment> fullPath) {
		// TODO Auto-generated by Flower Dev Center
		//return super.getInplaceEditorText(context, fullPath);
		return null;
	}

	/**
	 * @flowerModelElementId _-K4kQP5_EeKrJqcAep-lCg
	 */
	public String getLabelForLog(Object node, String nodeType) {
		// TODO Auto-generated by Flower Dev Center
		//return super.getLabelForLog(node, nodeType);
		return null;
	}

	/**
	 * @flowerModelElementId _-LKREP5_EeKrJqcAep-lCg
	 */
	public boolean populateTreeNode(Object source, TreeNode destination,
			GenericTreeContext context) {
		// TODO Auto-generated by Flower Dev Center
		//return super.populateTreeNode(source, destination, context);
		return true;
	}

	/**
	 * @flowerModelElementId _-Lb94P5_EeKrJqcAep-lCg
	 */
	public boolean setInplaceEditorText(
			StatefulServiceInvocationContext context, List<PathFragment> path,
			String text) {
		// TODO Auto-generated by Flower Dev Center
		//return super.setInplaceEditorText(context, path, text);
		return false;
	}
}