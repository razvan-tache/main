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
package org.flowerplatform.web.tests.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.flowerplatform.common.regex.RegexConfiguration;
import org.flowerplatform.common.regex.RegexProcessingSession;
import org.flowerplatform.common.regex.RegexWithAction;
import org.junit.Test;

/**
 * @author Cristi
 */
public class RegexEngineTest extends RegexTestBase {

	@Test
	public void testDispatchingFindResultToRegexWithAction() {
		final List<String> results = new ArrayList<String>();
		
		RegexConfiguration re = new RegexConfiguration();
		
		final String rule1Descr = "cuvant cu sufix cifre";
		final String rule2Descr = "string";
		final String rule3Descr = "cuvant:cuvant";
		
		re
			.add(new RegexWithAction.IfFindThisAnnounceMatchCandidate(rule1Descr, "suf(\\d*)", "categ: " + rule1Descr) {
				
				@Override
				public void executeAction(RegexProcessingSession session) {
					super.executeAction(session);
					results.add(rule1Descr);
					Assert.assertEquals(1, session.getCurrentSubMatchesForCurrentRegex().length);
					Assert.assertEquals("23", session.getCurrentSubMatchesForCurrentRegex()[0]);
				}
			})
			.add(new RegexWithAction.IfFindThisAnnounceMatchCandidate(rule2Descr, "string", "categ: " + rule2Descr) {
				
				@Override
				public void executeAction(RegexProcessingSession session) {
					super.executeAction(session);
					results.add(rule2Descr);					
					Assert.assertNull(session.getCurrentSubMatchesForCurrentRegex());
				}
			})
			.add(new RegexWithAction.IfFindThisAnnounceMatchCandidate(rule3Descr, "(\\w*):(\\w*)", "categ: " + rule3Descr) {
				
				@Override
				public void executeAction(RegexProcessingSession session) {
					super.executeAction(session);
					results.add(rule3Descr);					
					Assert.assertEquals(2, session.getCurrentSubMatchesForCurrentRegex().length);
					Assert.assertEquals("atr", session.getCurrentSubMatchesForCurrentRegex()[0]);
					Assert.assertEquals("tip", session.getCurrentSubMatchesForCurrentRegex()[1]);
				}
			})
			.compile(Pattern.DOTALL);	
		
		RegexProcessingSession session = re.startSession("Aceste este un string dar si un suf23. Am mai adaugat si atr:tip.");
		while (session.find()) {
		}
		
		Assert.assertEquals("We have exactly 3 matches", 3, results.size());
		Assert.assertEquals("2nd match = 1st rule", rule1Descr, results.get(1));
		Assert.assertEquals("1st match = 2nd rule", rule2Descr, results.get(0));
		Assert.assertEquals("3rd match = 3rd rule", rule3Descr, results.get(2));
	}
}