package org.flowerplatform.web.git.dto {
	import org.flowerplatform.web.git.remote.dto.CommitDto;
	
	/**
	 *	@author Cristina Constantinescu
	 */   
	[RemoteClass]
	[Bindable]
	public class ResetPageDto extends GitActionDto {
	
		public var repoName:String;
		
		public var current:GitRef;
		
		public var currentCommit:CommitDto;
		
		public var target:GitRef;
		
		public var targetCommit:CommitDto;
		
	}
}