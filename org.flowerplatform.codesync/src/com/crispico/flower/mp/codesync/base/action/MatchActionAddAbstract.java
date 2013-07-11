package com.crispico.flower.mp.codesync.base.action;

import com.crispico.flower.mp.codesync.base.CodeSyncAlgorithm;
import com.crispico.flower.mp.codesync.base.IModelAdapter;
import com.crispico.flower.mp.codesync.base.Match;


/**
 * @flowerModelElementId _Sa1kULRtEeCZUu0W6cecrg
 */
public abstract class MatchActionAddAbstract extends DiffAction {

	protected boolean processDiffs;
	
	protected abstract Object getThis(Match match); 
	protected abstract Object getOpposite(Match match);
	protected abstract IModelAdapter getThisModelAdapter(Match match);
	protected abstract IModelAdapter getOppositeModelAdapter(Match match);
	protected abstract void setOpposite(Match match, Object elment);
	protected abstract void processDiffs(Match match);
	protected abstract void setChildrenModified(Match match);

	public MatchActionAddAbstract(boolean processDiffs) {
		super();
		this.processDiffs = processDiffs;
	}

	@Override
	public ActionResult execute(Match match, int diffIndex) {
		processMatch(match.getParentMatch(), match, true);
		Object child = getThis(match);
		return new ActionResult(false, true, true, getThisModelAdapter(match).getMatchKey(child), true);
	}
	
	protected void processMatch(Match parentMatch, Match match, boolean isFirst) {
		Object this_ = getThis(match);
		if (this_ == null) // this happens when parentMatch was a 2-match-ancestor-left/right and match is 1-match-ancestor (i.e. del left & right)
			return;
		IModelAdapter oppositeParentMa = getOppositeModelAdapter(parentMatch);
		Object opposite = oppositeParentMa.createChildOnContainmentFeature(getOpposite(parentMatch), match.getFeature(), this_);
		setOpposite(match, opposite);
		// from 1-match-left or 1-match-right, the match became 2-match-left-right

		// process value features 
		IModelAdapter thisMa = getThisModelAdapter(match);
		IModelAdapter oppositeMa = getOppositeModelAdapter(match);
		for (Object childFeature : match.getEditableResource().getModelAdapterFactorySet().getFeatureProvider(this_).getFeatures(this_)) {
			switch (match.getEditableResource().getModelAdapterFactorySet().getFeatureProvider(this_).getFeatureType(childFeature)) {
			case IModelAdapter.FEATURE_TYPE_VALUE:
				Object value = thisMa.getValueFeatureValue(this_, childFeature, null);
				Object valueOpposite = oppositeMa.getValueFeatureValue(opposite, childFeature, null);
				if (!CodeSyncAlgorithm.safeEquals(value, valueOpposite))
					oppositeMa.setValueFeatureValue(opposite, childFeature, value);
				break;
			}
		}
		
		if (processDiffs) 
			processDiffs(match);
		
		match.setChildrenConflict(false);
				
		if (!match.getSubMatches().isEmpty()) {
			setChildrenModified(match);
			// process child match
			for (Match childMatch : match.getSubMatches())
				processMatch(match, childMatch, false);
		}
	}
}