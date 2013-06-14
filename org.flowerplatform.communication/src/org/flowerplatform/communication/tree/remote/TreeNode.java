package org.flowerplatform.communication.tree.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cristi
 * @author Cristina
 * @author Sorin
 * 
 * @see Corresponding AS class.
 * 
 * @flowerModelElementId _YcuIwDR9EeCGErbqxW555A
 */
public class TreeNode {
		
	/**
	 * @flowerModelElementId _fUIsgDR9EeCGErbqxW555A
	 */
	private String label;
	
	/**
	 * @flowerModelElementId _Ck1J0EiREeCqsckFqpKKFw
	 */
	private String icon;
	
	/**
	 * @flowerModelElementId _YM-VMDSAEeCGErbqxW555A
	 */
	private boolean hasChildren;
	
	/**
	 * @flowerModelElementId _tXtU8DR9EeCGErbqxW555A
	 */
	private List<TreeNode> children;
	
	/**
	 * @flowerModelElementId _WHXtsKAQEeGLneHqP7FuFA
	 */
	private TreeNode parent;
	
	/**
	 * @flowerModelElementId _0zGxgKKdEeGYz6sIcvSzpg
	 */
	private PathFragment pathFragment;
	
	private Map<String, Object> customData;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String iconUrl) {
		this.icon = iconUrl;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public PathFragment getPathFragment() {
		return pathFragment;
	}

	public void setPathFragment(PathFragment pathFragment) {
		this.pathFragment = pathFragment;
	}

	/**
	 * This getter is intended only for serialization.
	 * For working with the map see #getOrCreateCustomData(). 
	 */
	public Map<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}
	
	/**
	 * Method should be used only for behaviors that wish to populate this field.
	 * This field may be useful for transporting specific information to the client side
	 * about the corresponding object on the server side.
	 *  
	 * Note: the logic that computes information must be fast.
	 */
	public Map<String, Object> getOrCreateCustomData() {
		if (customData == null)
			customData = new HashMap<String, Object>(); 
		return customData;
	}
	
	@Override
	public String toString() {
		return "TreeNode [label=" + label + ", iconUrl=" + icon + ", pathFragment=" + pathFragment + "]";
	}
}