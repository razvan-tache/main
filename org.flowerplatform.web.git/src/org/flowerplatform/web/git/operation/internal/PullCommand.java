package org.flowerplatform.web.git.operation.internal;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;

/**
 * @author Cristina Constantinescu
 */
public class PullCommand extends TransportCommand<PullCommand, PullResult> {

	private final static String DOT = "."; //$NON-NLS-1$

	private ProgressMonitor monitor = NullProgressMonitor.INSTANCE;

	private PullRebaseMode pullRebaseMode = PullRebaseMode.USE_CONFIG;

	private enum PullRebaseMode {
		USE_CONFIG,
		REBASE,
		NO_REBASE
	}

	/**
	 * @param repo
	 */
	public PullCommand(Repository repo) {
		super(repo);
	}

	/**
	 * @param monitor
	 *            a progress monitor
	 * @return this instance
	 */
	public PullCommand setProgressMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
		return this;
	}

	/**
	 * Set if rebase should be used after fetching. If set to true, rebase is
	 * used instead of merge. This is equivalent to --rebase on the command line.
	 * <p/>
	 * If set to false, merge is used after fetching, overriding the configuration
	 * file. This is equivalent to --no-rebase on the command line.
	 * <p/>
	 * This setting overrides the settings in the configuration file.
	 * By default, the setting in the repository configuration file is used.
	 * <p/>
	 * A branch can be configured to use rebase by default.
	 * See branch.[name].rebase and branch.autosetuprebase.
	 *
	 * @param useRebase
	 * @return {@code this}
	 */
	public PullCommand setRebase(boolean useRebase) {
		checkCallable();
		pullRebaseMode = useRebase ? PullRebaseMode.REBASE : PullRebaseMode.NO_REBASE;
		return this;
	}

	/**
	 * Executes the {@code Pull} command with all the options and parameters
	 * collected by the setter methods (e.g.
	 * {@link #setProgressMonitor(ProgressMonitor)}) of this class. Each
	 * instance of this class should only be used for one invocation of the
	 * command. Don't call this method twice on an instance.
	 *
	 * @return the result of the pull
	 * @throws WrongRepositoryStateException
	 * @throws InvalidConfigurationException
	 * @throws DetachedHeadException
	 * @throws InvalidRemoteException
	 * @throws CanceledException
	 * @throws RefNotFoundException
	 * @throws NoHeadException
	 * @throws org.eclipse.jgit.api.errors.TransportException
	 * @throws GitAPIException
	 */	
	public PullResult call() throws GitAPIException,
			WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException,
			RefNotFoundException, NoHeadException,
			org.eclipse.jgit.api.errors.TransportException {
		checkCallable();

		monitor.beginTask(JGitText.get().pullTaskName, 2);

		String branchName;
		String fullBranch;
		try {
			fullBranch = repo.getFullBranch();
			if (fullBranch == null)
				throw new NoHeadException(
						JGitText.get().pullOnRepoWithoutHEADCurrentlyNotSupported);
			if (!fullBranch.startsWith(Constants.R_HEADS)) {
				// we can not pull if HEAD is detached and branch is not
				// specified explicitly
				throw new DetachedHeadException();
			}
			branchName = fullBranch.substring(Constants.R_HEADS.length());
		} catch (IOException e) {
			throw new JGitInternalException(
					JGitText.get().exceptionCaughtDuringExecutionOfPullCommand,
					e);
		}

		if (!repo.getRepositoryState().equals(RepositoryState.SAFE))
			throw new WrongRepositoryStateException(MessageFormat.format(
					JGitText.get().cannotPullOnARepoWithState, repo
							.getRepositoryState().name()));

		// get the configured remote for the currently checked out branch
		// stored in configuration key branch.<branch name>.remote
		Config repoConfig = repo.getConfig();
		String remote = repoConfig.getString(
				ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
				ConfigConstants.CONFIG_KEY_REMOTE);
		if (remote == null)
			// fall back to default remote
			remote = Constants.DEFAULT_REMOTE_NAME;

		// get the name of the branch in the remote repository
		// stored in configuration key branch.<branch name>.merge
		String remoteBranchName = repoConfig.getString(
				ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
				ConfigConstants.CONFIG_KEY_MERGE);

        // determines whether rebase should be used after fetching
        boolean doRebase = false;
        switch (pullRebaseMode) {
            case REBASE:
                doRebase = true;
                break;
            case NO_REBASE:
                doRebase = false;
                break;
            case USE_CONFIG:
            default:
                // check if the branch is configured for pull-rebase
                doRebase = repoConfig.getBoolean(
                        ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                        ConfigConstants.CONFIG_KEY_REBASE, false);
                break;
        }

		if (remoteBranchName == null) {
			String missingKey = ConfigConstants.CONFIG_BRANCH_SECTION + DOT
					+ branchName + DOT + ConfigConstants.CONFIG_KEY_MERGE;
			throw new InvalidConfigurationException(MessageFormat.format(
					JGitText.get().missingConfigurationForKey, missingKey));
		}

		final boolean isRemote = !remote.equals("."); //$NON-NLS-1$
		String remoteUri;
		FetchResult fetchRes;
		if (isRemote) {
			remoteUri = repoConfig.getString(
					ConfigConstants.CONFIG_REMOTE_SECTION, remote,
					ConfigConstants.CONFIG_KEY_URL);
			if (remoteUri == null) {
				String missingKey = ConfigConstants.CONFIG_REMOTE_SECTION + DOT
						+ remote + DOT + ConfigConstants.CONFIG_KEY_URL;
				throw new InvalidConfigurationException(MessageFormat.format(
						JGitText.get().missingConfigurationForKey, missingKey));
			}

			if (monitor.isCancelled())
				throw new CanceledException(MessageFormat.format(
						JGitText.get().operationCanceled,
						JGitText.get().pullTaskName));
			
			RefSpec refSpec = new RefSpec();
			refSpec = refSpec.setForceUpdate(true);
			refSpec = refSpec.setSourceDestination(remoteBranchName, fullBranch);

			FetchCommand fetch = new Git(repo).fetch()
					.setRemote(remote)
					.setRefSpecs(refSpec);
		
			fetch.setProgressMonitor(monitor);
			configure(fetch);

			fetchRes = fetch.call();
		} else {
			// we can skip the fetch altogether
			remoteUri = "local repository";
			fetchRes = null;
		}

		monitor.update(1);

		if (monitor.isCancelled())
			throw new CanceledException(MessageFormat.format(
					JGitText.get().operationCanceled,
					JGitText.get().pullTaskName));

		// we check the updates to see which of the updated branches
		// corresponds
		// to the remote branch name
		AnyObjectId commitToMerge;
		if (isRemote) {
			Ref r = null;
			if (fetchRes != null) {
				r = fetchRes.getAdvertisedRef(remoteBranchName);
				if (r == null)
					r = fetchRes.getAdvertisedRef(Constants.R_HEADS
							+ remoteBranchName);
			}
			if (r == null)
				throw new JGitInternalException(MessageFormat.format(JGitText
						.get().couldNotGetAdvertisedRef, remoteBranchName));
			else
				commitToMerge = r.getObjectId();
		} else {
			try {
				commitToMerge = repo.resolve(remoteBranchName);
				if (commitToMerge == null)
					throw new RefNotFoundException(MessageFormat.format(
							JGitText.get().refNotResolved, remoteBranchName));
			} catch (IOException e) {
				throw new JGitInternalException(
						JGitText.get().exceptionCaughtDuringExecutionOfPullCommand,
						e);
			}
		}

		String upstreamName = "branch \'"
				+ Repository.shortenRefName(remoteBranchName) + "\' of "
				+ remoteUri;

		PullResult result;
		if (doRebase) {
			RebaseCommand rebase = new Git(repo).rebase();
			RebaseResult rebaseRes = rebase.setUpstream(commitToMerge)
					.setUpstreamName(upstreamName)
					.setProgressMonitor(monitor).setOperation(Operation.BEGIN)
					.call();
			result = new PullResult(fetchRes, remote, rebaseRes);
		} else {
			MergeCommand merge = new Git(repo).merge();
			merge.include(upstreamName, commitToMerge);
			MergeResult mergeRes = merge.call();
			monitor.update(1);
			result = new PullResult(fetchRes, remote, mergeRes);
		}
		monitor.endTask();
		return result;
	}

}
