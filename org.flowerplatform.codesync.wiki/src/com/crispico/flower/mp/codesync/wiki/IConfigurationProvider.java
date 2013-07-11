package com.crispico.flower.mp.codesync.wiki;

import astcache.wiki.Page;

import com.crispico.flower.mp.model.codesync.CodeSyncElement;
import com.crispico.flower.mp.model.codesync.CodeSyncRoot;

/**
 * @author Mariana
 */
public interface IConfigurationProvider {

	/**
	 * Add expressions and set parameters for <code>config</code>.
	 */
	void buildConfiguration(WikiRegexConfiguration config, CodeSyncElement cse);
	
	Class<? extends WikiTreeBuilder> getWikiTreeBuilderClass();
	
	WikiTextBuilder getWikiTextBuilder(CodeSyncElement cse);
	
	/**
	 * Generates a wiki tree from specific wiki information.
	 */
	CodeSyncRoot getWikiTree(Object wiki);
	
	/**
	 * Saves the page.
	 */
	void savePage(Page page);
}