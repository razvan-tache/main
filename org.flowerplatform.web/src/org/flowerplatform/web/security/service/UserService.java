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
package org.flowerplatform.web.security.service;

import static org.flowerplatform.web.security.sandbox.SecurityEntityAdaptor.ANONYMOUS;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flowerplatform.common.CommonPlugin;
import org.flowerplatform.common.FlowerWebProperties.AddBooleanProperty;
import org.flowerplatform.common.FlowerWebProperties.AddIntegerProperty;
import org.flowerplatform.common.FlowerWebProperties.AddProperty;
import org.flowerplatform.communication.CommunicationPlugin;
import org.flowerplatform.communication.channel.CommunicationChannel;
import org.flowerplatform.communication.service.ServiceInvocationContext;
import org.flowerplatform.communication.service.ServiceRegistry;
import org.flowerplatform.web.WebPlugin;
import org.flowerplatform.web.database.DatabaseOperation;
import org.flowerplatform.web.database.DatabaseOperationWrapper;
import org.flowerplatform.web.entity.EntityFactory;
import org.flowerplatform.web.entity.Group;
import org.flowerplatform.web.entity.GroupUser;
import org.flowerplatform.web.entity.NamedEntity;
import org.flowerplatform.web.entity.Organization;
import org.flowerplatform.web.entity.OrganizationMembershipStatus;
import org.flowerplatform.web.entity.OrganizationUser;
import org.flowerplatform.web.entity.PermissionEntity;
import org.flowerplatform.web.entity.SVNCommentEntity;
import org.flowerplatform.web.entity.User;
import org.flowerplatform.web.entity.dto.NamedDto;
import org.flowerplatform.web.security.dto.GroupAdminUIDto;
import org.flowerplatform.web.security.dto.OrganizationAdminUIDto;
import org.flowerplatform.web.security.dto.OrganizationUserAdminUIDto;
import org.flowerplatform.web.security.dto.PermissionAdminUIDto;
import org.flowerplatform.web.security.dto.UserAdminUIDto;
import org.flowerplatform.web.security.dto.User_CurrentUserLoggedInDto;
import org.flowerplatform.web.security.mail.SendMailService;
import org.flowerplatform.web.security.sandbox.FlowerWebPrincipal;
import org.flowerplatform.web.security.sandbox.SecurityEntityAdaptor;
import org.flowerplatform.web.security.sandbox.SecurityUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service used to make CRUD operations on <code>User</code> entity.
 * 
 * @see BootstrapService#initialize()
 * @see ServiceRegistry
 * 
 * @author Cristi
 * @author Cristina
 * @author Mariana
 * 
 * 
 */
public class UserService extends ServiceObservable {
	
	public static final String SERVICE_ID = "userService";
	
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	public static UserService getInstance() {	
		return (UserService) CommunicationPlugin.getInstance().getServiceRegistry().getService(SERVICE_ID);
	}
	
	/**
	 * Converts a {@link User} to {@link UserAdminUIDto}. Filters the info that will be sent to the client,
	 * so any {@link Organization}s or {@link Group}s the <code>user</code> belongs to, but the client that
	 * requested the info does not have permissions over will not be sent.
	 * 
	 * @see #findAllAsAdminUIDto()
	 * @see #findByIdAsAdminUIDto()
	 * 
	 */
	private UserAdminUIDto convertUserToUserAdminUIDto(User user) {
		UserAdminUIDto dto = new UserAdminUIDto();
		
		dto.setId(user.getId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());
		dto.setIsActivated(user.isActivated());
		dto.setLogin(user.getLogin());
		
		HashSet<GroupAdminUIDto> groups = new HashSet<GroupAdminUIDto>();
		if (user.getGroupUsers() != null) {
			for (GroupUser groupUser : user.getGroupUsers()) {
				try {
					if (user.getId() != CommunicationPlugin.tlCurrentPrincipal.get().getUserId())
						SecurityUtils.checkAdminSecurityEntitiesPermission(PermissionEntity.GROUP_PREFIX + groupUser.getGroup().getName());
					Group group = groupUser.getGroup();
					NamedDto orgDto = null;
					if (group.getOrganization() != null) {
						orgDto = new NamedDto(group.getOrganization().getId(), group.getOrganization().getName());					
					}			
					groups.add(new GroupAdminUIDto(group.getId(), group.getName(), orgDto));
				} catch (SecurityException e) {
					// do nothing
				}
			}
			dto.setGroups(groups);
		}
		
		HashSet<OrganizationUserAdminUIDto> organizations = new HashSet<OrganizationUserAdminUIDto>();
		if (user.getOrganizationUsers() != null) {
			for (OrganizationUser organizationUser : user.getOrganizationUsers()) {
				try {
					if (user.getId() != CommunicationPlugin.tlCurrentPrincipal.get().getUserId()) 
						SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(organizationUser.getOrganization(), null));
					OrganizationUserAdminUIDto ouDto = new OrganizationUserAdminUIDto();
					ouDto.setId(organizationUser.getId());
					ouDto.setOrganization(OrganizationService.getInstance().convertOrganizationToOrganizationAdminUIDto(organizationUser.getOrganization(), user));
					ouDto.setStatus(organizationUser.getStatus());
					organizations.add(ouDto);
				} catch (SecurityException e) {
					// do nothing
				}
			}
			dto.setOrganizationUsers(organizations);
		}
		return dto;
	}
	
	/**
	 * @author Cristi
 	 */
	public User_CurrentUserLoggedInDto convertUserToUser_LoggedInDto(User user) {
		User_CurrentUserLoggedInDto dto = new User_CurrentUserLoggedInDto();
		
		dto.setId(user.getId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());
		dto.setLogin(user.getLogin());
		dto.setIsAdmin(user.isAdmin());
		dto.setHasAdminSecurityEntitiesPermissions(SecurityUtils.hasAdminSecurityEntitiesPermission());
		return dto;
	}
	
	/**
	 * Finds the user given by its id and returns an {@link UserAdminUIDto}. 
	 * 
	 */
	public UserAdminUIDto findByIdAsAdminUIDto(final long id) {
		logger.debug("Find user with id = {}", id);
		
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {

			@Override
			public void run() {
				User user = wrapper.find(User.class, id);
				if (user == null)
					throw new RuntimeException(String.format("User with id=%s was not found in the DB.", id));
				String groupOwners = SecurityEntityAdaptor.toCsvString(getUserGroups(user), Collections.<GroupAdminUIDto>emptyList(), PermissionEntity.GROUP_PREFIX);
				String orgOwners = SecurityEntityAdaptor.toCsvString(getOrganizationsWhereUserBelongs(user, true), null, PermissionEntity.ORGANIZATION_PREFIX);
				String owners = groupOwners + (groupOwners.length() > 0 && orgOwners.length() > 0 ? "," : "") + orgOwners;
				try {
					if (id != CommunicationPlugin.tlCurrentPrincipal.get().getUserId()) {
						SecurityUtils.checkAdminSecurityEntitiesPermission(owners);
					}
				} catch (SecurityException e) {
					throw new RuntimeException(String.format("User with id=%s is not available.", id));
				}
				wrapper.setOperationResult(convertUserToUserAdminUIDto(user));
			}
		});
		
		return (UserAdminUIDto) wrapper.getOperationResult();
	}
	
	/**
	 * Returns the {@link User}s that belong to the {@link Organization}s, {@link Group}s
	 * or whose <code>login</code>s match the filter. I.e. filter is
	 * <ul>
	 * 	<li>#org2, get all users that belong to #org2
	 * 	<li>@org3.group, get all users that belong to @org3.group
	 *  <li>#org2, @org3.group, get users that belong to #org2 or @org3.group
	 *  <li>$user1, get user with login user3
	 * </ul>
	 * 
	 * <p>
	 * Supports wildcards, i.e. #*org3*, @*org2*.*, $*user* is a valid filter.
	 * 
	 * <p>
	 * If the client that requested the info does not have rights over <b>any</b> of the {@link Organization}s
	 * or {@link Group}s a matched user belongs to, the user will not be returned.
	 * 
	 * <p>
	 * If <code>filter</code> is <code>null</code>, get all the users in the DB, and returns only those that the
	 * user has rights over.
	 */
	public List<UserAdminUIDto> findAllAsAdminUIDto(ServiceInvocationContext context, String filter) {
		logger.debug("Find users filtered by = {}", filter);
		
		if (filter == null || filter.equals("")) {
			filter = "$%";
		}
		
		filter = filter.replaceAll("\\*", "%");
		final String[] entities = filter.split("\\s+"); 
		
		final List<UserAdminUIDto> result = new ArrayList<UserAdminUIDto>();
		new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				boolean union = true; // true for OR, false for AND
				
				List<User> users = new ArrayList<User>();
				for (String entity : entities) {
					Query query = null;
					if (entity.startsWith(PermissionEntity.ORGANIZATION_PREFIX)) {
						entity = addDefaultWildcards(entity.substring(PermissionEntity.ORGANIZATION_PREFIX.length()));
						query = wrapper.createQuery("SELECT u " +
											   "FROM Organization o JOIN o.organizationUsers ou JOIN ou.user u " +
								 			   "WHERE o.name LIKE :organization_name OR o.label LIKE :organization_name");
						query.setParameter("organization_name", entity);
					} else {
						if (entity.startsWith(PermissionEntity.GROUP_PREFIX)) {
							entity = addDefaultWildcards(entity.substring(PermissionEntity.GROUP_PREFIX.length()));
							query = wrapper.createQuery("SELECT u " +
									   			   "FROM Group g JOIN g.groupUsers gu JOIN gu.user u " +
									   			   "WHERE g.name LIKE :group_name");
							query.setParameter("group_name", entity);
						} else {
							if (entity.startsWith(PermissionEntity.USER_PREFIX)) {
								entity = addDefaultWildcards(entity.substring(PermissionEntity.USER_PREFIX.length()));
								query = wrapper.createQuery("SELECT u " +
													   "FROM User u " +
													   "WHERE u.name LIKE :user_name OR u.login LIKE :user_name OR u.email LIKE :user_name");
								query.setParameter("user_name", entity);
							} else {
								// filter by status
								entity = entity.toUpperCase();
								OrganizationMembershipStatus status = null;
								for (OrganizationMembershipStatus value : OrganizationMembershipStatus.values()) {
									if (value.toString().startsWith(entity) && value.toString().contains(entity)) {
										status = value;
										break;
									}
								}
								
								if (status != null) {
									query = wrapper.createQuery("SELECT u " +
														   "FROM User u JOIN u.organizationUsers ou " +
														   "WHERE ou.status = :status");
									query.setParameter("status", status);
								} else {
									// operator, i.e. || && ,
									if (entity.equals("&&")) {
										union = false;
									}
								}
							}
						}
					}
					
					if (query != null) {
						@SuppressWarnings("unchecked")
						List<User> newList = query.list();
						if (union) {
							for (User u : newList) {
								if (!users.contains(u)) {
									users.add(u);
								}
							}
						} else {
							for (Iterator<User> it = users.iterator(); it.hasNext(); ) {
								User u = it.next();
								if (!newList.contains(u)) {
									it.remove();
								}
							}
						}
						
						// reset operator
						union = true;
					}
				}
				
				for (User user : users) {
					try {
						UserAdminUIDto dto = findByIdAsAdminUIDto(user.getId());
						result.add(dto);
					} catch (Exception e) {
						// do nothing
					}
				}
				
			}
		});
		
		return result;
	}
	
	/**
	 * Returns <code>%entity%</code>, if <code>entity</code> does not contain any wildcards already.
	 * 
	 * @author Mariana
	 */
	private String addDefaultWildcards(String entity) {
		if (!entity.contains("%"))
			return "%" + entity + "%";
		return entity;
	}
	
	/**
	 * Creates/Updates the {@link User} based on {@link UserAdminUIDto} stored information. 
	 * Returns an error message, or <code>null</code> if there were no errors.
	 * 
	 * 
	 */
	public String mergeAdminUIDto(final UserAdminUIDto dto) {
		logger.debug("Merge user = {}", dto.getLogin());
		
		User initialUser = (User) new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				wrapper.setOperationResult(wrapper.find(User.class, dto.getId()));
			}
		}).getOperationResult();
		
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				// first check if there is already a user with the same login
				List<User> usersWithSameLogin = wrapper.findByField(User.class, "login", dto.getLogin());
				if (dto.getId() == 0 && usersWithSameLogin != null && usersWithSameLogin.size() > 0) {
					wrapper.setOperationResult(WebPlugin.getInstance().getMessage("authentication.register.loginAlreadyExists"));
					return;
				}
				
				if (dto.getLogin().startsWith(ANONYMOUS)) {
					if (CommunicationPlugin.tlCurrentPrincipal.get() != null) {
						if (dto.getId() == 0 && !((FlowerWebPrincipal) CommunicationPlugin.tlCurrentPrincipal.get()).getUser().isAdmin()) {
							wrapper.setOperationResult("You cannot create an anonymous user!");
							return;
						} else {
							if (dto.getId() == CommunicationPlugin.tlCurrentPrincipal.get().getUserId()) {
								wrapper.setOperationResult("You cannot edit an anonymous user!"); // don't allow an anonymous user to edit its own details
								// note: the permissions logic will take care of the case when another user tries to edit anonymous
								// i.e. org1 admin will be allowed to edit anonymous.org1, but not anonymous.org2; FDC admin can edit any user by default
								return;
							}
						}
					}
				} 
				
				User user = wrapper.mergeDto(User.class, dto);
				user = wrapper.merge(user);
				
				// check permissions
				List<Group> originalGroups = getUserGroups(user);
				if (CommunicationPlugin.tlCurrentPrincipal.get() != null &&
						CommunicationPlugin.tlCurrentPrincipal.get().getUserId() != user.getId()) {
					String groupsCsvList = SecurityEntityAdaptor.toCsvString(originalGroups, dto.getGroups(), PermissionEntity.GROUP_PREFIX);
					String orgsCsvList = SecurityEntityAdaptor.toCsvString(getOrganizationsWhereUserBelongs(user, false), dto.getOrganizations(false), PermissionEntity.ORGANIZATION_PREFIX);
					String csvList = groupsCsvList + (groupsCsvList.length() > 0 && orgsCsvList.length() > 0 ? "," : "") + orgsCsvList;
					try {
						SecurityUtils.checkAdminSecurityEntitiesPermission(csvList);
					} catch (Exception e) {
						// if logged in user does not have permissions over user, then maybe the user has PENDING status in one of logged in user's organizations
						// in this case, return the corresponding error message
						String orgsWithPendingStatus = SecurityEntityAdaptor.toCsvString(getOrganizationsWhereUserBelongs(user, true), dto.getOrganizations(true), PermissionEntity.ORGANIZATION_PREFIX);
						SecurityUtils.checkAdminSecurityEntitiesPermission(orgsWithPendingStatus);
						wrapper.setOperationResult("The user cannot be updated while his/her membership is not approved!");
						return;
					}
				}
				user.setLogin(dto.getLogin());
				
				// validate email if changing, only if user is not anonymous
				if (!dto.getLogin().startsWith(ANONYMOUS)) {
					if (dto.getEmail() == null || !dto.getEmail().equals(user.getEmail())) {
						if (!SendMailService.getInstance().validate(dto.getEmail())) {
							wrapper.setOperationResult(WebPlugin.getInstance().getMessage("authentication.register.emailNotValid"));
							return;
						}
						
						// check for uniqueness
						if (Boolean.parseBoolean(CommonPlugin.getInstance().getFlowerWebProperties().getProperty(EMAIL_SHOULD_BE_UNIQUE))) {
							Query query = wrapper.createQuery("SELECT u " +
														"FROM User u " +
														"WHERE u.email = :email");
							query.setParameter("email", dto.getEmail());
							if (query.list().size() > 0) {
								wrapper.setOperationResult(WebPlugin.getInstance().getMessage("authentication.register.emailNotUnique"));
								return;
							}
						}
					}
				}
				user.setEmail(dto.getEmail());
				
				// allow updating the user without providing the password; if a password is provided, update it
				if (dto.getPassword() != null) {
					// check for min length
					int minLength = Integer.parseInt(CommonPlugin.getInstance().getFlowerWebProperties().getProperty(MIN_PASSWORD_LENGTH));
					if (dto.getPassword().length() < minLength) {
						wrapper.setOperationResult(WebPlugin.getInstance().getMessage("authentication.register.passwordTooShort", minLength));
					}
					user.setHashedPassword(Util.encrypt(dto.getPassword()));
				}
				
				user.setActivated(dto.getIsActivated());

//				user = wrapper.merge(user);
				
				// get original organizations
				List<Organization> originalOrganizations = getOrganizationsWhereUserBelongs(user, true);
						
				// get added and removed organizations
				List<Long>[] list = Util.getAddedRemovedElements(originalOrganizations, dto.getOrganizations(true));
				// for each removed organization, remove it from the database too
				for (Long id : list[1]) {
					for (Iterator<OrganizationUser> it = user.getOrganizationUsers().iterator(); it.hasNext();) {
						OrganizationUser organizationUser = it.next();
						try {
							// check if the current user has permissions, unless the current user is leaving the organization
							if (CommunicationPlugin.tlCurrentPrincipal.get() != null &&
									CommunicationPlugin.tlCurrentPrincipal.get().getUserId() != user.getId()) {
								SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(Collections.singletonList(organizationUser.getOrganization()), null, PermissionEntity.ORGANIZATION_PREFIX));
							}
							if (organizationUser.getOrganization().getId() == id) {
								it.remove();
//								wrapper.merge(organizationUser.getUser());
								removeOrganizationUserDependency(organizationUser);
								break;
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				}
				// for each added organization, add it in the database too
				for (Long id : list[0]) {
					Organization organization = wrapper.find(Organization.class, id);
					try {
						SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(organization, null));
						addOrganizationUserDependency(user, organization, OrganizationMembershipStatus.MEMBER, wrapper);
					} catch (Exception e) {
						// do nothing
					}
				}		
				
				// get added and removed groups
				list = Util.getAddedRemovedElements(originalGroups, dto.getGroups());
				// for each removed group, remove it from database too
				// important: only if current user has permission
				for (Long id : list[1]) {
					for (Iterator<GroupUser> it = user.getGroupUsers().iterator(); it.hasNext();) {
						GroupUser groupUser = it.next();
						try {
							SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(Collections.singletonList(groupUser.getGroup()), null, PermissionEntity.GROUP_PREFIX));
							if (groupUser.getGroup().getId() == id) {
								it.remove();
//								wrapper.merge(groupUser.getUser());
								removeGroupUserDependency(groupUser);
								break;
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				}
				// for each added group, add it in database too
				for (Long id : list[0]) {
					Group group = wrapper.find(Group.class, id);
					try {
						SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(Collections.singletonList(group), null, PermissionEntity.GROUP_PREFIX));
					
						// only if the user is a member of the organization of the group
						if (group.getOrganization() == null || OrganizationService.getInstance().getOrganizationMembershipStatus(group.getOrganization(), user) != null) {
							addGroupUserDependency(user, group, wrapper);
						} else {
							wrapper.setOperationResult("The user cannot be added to a group of an organization that he doesn't belong to!");
							return;
						}
					} catch (Exception e) {
						// do nothing
					}
				}
			
//				// save changes
//				wrapper.merge(user);
			}
		});
		
		User newUser = (User) new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				wrapper.setOperationResult(wrapper.find(User.class, dto.getId()));
			}
		}).getOperationResult();
		
		observable.notifyObservers(Arrays.asList(UPDATE, initialUser, newUser));
		return (String) wrapper.getOperationResult();
	}
	
	/**
	 * Deletes all {@link User}s based on the list of their ids. This operations is allowed iff the user has global permissions.
	 * 
	 * 
	 */
	public String delete(final ServiceInvocationContext context, final List<Integer> ids) {
		final User[] deletedUser = new User[1];
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				for (Integer id : ids) {
					if (((CommunicationChannel) context.getCommunicationChannel()).getPrincipal().getUserId() != id)
						SecurityUtils.checkAdminSecurityEntitiesPermission(PermissionEntity.ANY_ENTITY);	
					
					User user = wrapper.find(User.class, Long.valueOf(id));
					
					if (user.getLogin().startsWith(ANONYMOUS) &&
							!((User) context.getCommunicationChannel().getPrincipal().getUser()).isAdmin()) {
						wrapper.setOperationResult("You cannot delete an anonymous user!");
						return;
					}
							
					logger.debug("Delete {}", user);
					
					// user is removed so remove entities from GroupUser
//					for (Iterator<GroupUser> it = user.getGroupUsers().iterator(); it.hasNext();) {
//						GroupUser groupUser = it.next(); 
//						it.remove();
//						user = wrapper.merge(user);
//						removeGroupUserDependency(groupUser);
//					} 
					
					// user is removed so remove entities from OrganizationUser
//					for (Iterator<OrganizationUser> it = user.getOrganizationUsers().iterator(); it.hasNext();) {
//						OrganizationUser organizationUser = it.next();
//						it.remove();
//						user = wrapper.merge(user);
//						removeOrganizationUserDependency(organizationUser);
//					}
					
					// user is removed so update entities from RecentResource
					Query q = wrapper.createQuery("UPDATE RecentResource r SET lastAccessUser = NULL WHERE r.lastAccessUser = :user");
					q.setParameter("user", user);
					q.executeUpdate();
					
					q = wrapper.createQuery("UPDATE RecentResource r SET lastSaveUser = NULL WHERE r.lastSaveUser = :user");
					q.setParameter("user", user);
					q.executeUpdate();
					
					wrapper.delete(user);
					deletedUser[0] = user;
				}
			}
		});
		observable.notifyObservers(Arrays.asList(DELETE, deletedUser[0]));
		return (String) wrapper.getOperationResult();
	}
	
	/**
	 * This method does not remove the groupUser from the groupUsers 
	 * of the user (it does not have access to the iterator from which the method
	 * is called).
	 * 
	 * Removes the corresponding {@link GroupUser} from database.
	 * Removes the association between groupUser and group.
	 * 
	 * Note cache: Because of the cache mechanism, this isn't done automatically.
	 * 
	 */
	private void removeGroupUserDependency(final GroupUser groupUser) {
		logger.debug("Remove {} from {}", groupUser.getUser(), groupUser.getGroup());
		groupUser.getGroup().getGroupUsers().remove(groupUser);
	}
	
	private void removeOrganizationUserDependency(final OrganizationUser organizationUser) {
		logger.debug("Remove {} from {}", organizationUser.getUser(), organizationUser.getOrganization());
		organizationUser.getOrganization().getOrganizationUsers().remove(organizationUser);
	}
	
	/**
	 * TODO : temporary method
	 * This method makes also modifications on target entities (User and Group).
	 * 
	 * Note cache: Because of the cache mechanism, this isn't done automatically.
	 * 
	 */
	private void addGroupUserDependency(final User user, final Group group, DatabaseOperationWrapper wrapper) {	
		logger.debug("Add {} to {}", user, group);
		GroupUser gr = EntityFactory.eINSTANCE.createGroupUser();		
		gr.setGroup(group);
		gr.setUser(user);
	}
	
	private OrganizationUser addOrganizationUserDependency(final User user, final Organization organization, final OrganizationMembershipStatus status, DatabaseOperationWrapper wrapper) {
		if (logger.isDebugEnabled()) {
			String[] params = {user.toString(), organization.toString(), status.toString()};
			logger.debug("Add {} to {} with status {}", params);
		}
		OrganizationUser ou = EntityFactory.eINSTANCE.createOrganizationUser();
		ou = wrapper.merge(ou);
		ou.setOrganization(organization);
		ou.setUser(user);
		ou.setStatus(status);
		return ou;
	}

	@SuppressWarnings("unchecked")
	public List<Group> getUserGroups(final User user) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				Query query =  wrapper.createQuery("SELECT g " +
											  "FROM User u JOIN u.groupUsers gu JOIN gu.group g " +
											  "WHERE u.id = :user_id");
				query.setParameter("user_id", user.getId());
				wrapper.setOperationResult(query.list());
			}
		});
		return (List<Group>) wrapper.getOperationResult();
	}
	
	@SuppressWarnings("unchecked")
	private List<Organization> getOrganizationsWhereUserBelongs(final User user, final boolean withPendingStatus) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				Query query;
				query =  wrapper.createQuery("SELECT o " +
										"FROM User u JOIN u.organizationUsers ou JOIN ou.organization o " +
										"WHERE u.id = :user_id" + 
										(withPendingStatus ? "" : " AND ou.status != :pending_status"));
				query.setParameter("user_id", user.getId());
				if (!withPendingStatus) {
					query.setParameter("pending_status", OrganizationMembershipStatus.PENDING_MEMBERSHIP_APPROVAL);
				}
				wrapper.setOperationResult(query.list());
			}
		});
		return (List<Organization>) wrapper.getOperationResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Organization> getOrganizations(final User user) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				if (user.isAdmin()) {
					wrapper.setOperationResult(wrapper.findAll(Organization.class));
				} else {
					wrapper.setOperationResult(getOrganizationsWhereUserBelongs(user, true));
				}
			}
		});
		return (List<Organization>) wrapper.getOperationResult();
	}
	
	public Organization getOrganization(final User user, final long orgId) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				Query query =  wrapper.createQuery("SELECT o " +
						  "FROM User u JOIN u.organizationUsers ou JOIN  ou.organization o " +
						  "WHERE u.id = :user_id AND o.id = :org_id");
				query.setParameter("user_id", user.getId());
				query.setParameter("org_id", orgId);
				@SuppressWarnings("unchecked")
				List<Organization> list = query.list();
				if (!list.isEmpty()) {
					wrapper.setOperationResult(list.get(0));
				}
			}
		});
		return (Organization) wrapper.getOperationResult();
	}
	
	public List<String> getAllUserRepositories(User user) {
		List<Organization> list = getOrganizations(user);
		List<String> repositories = new ArrayList<String>();
		for (Organization org : list) {
			for (NamedEntity dto : org.getSvnRepositoryURLs()) {
				if (!repositories.contains(dto.getName())) {
					repositories.add(dto.getName());
				}
			}
		}
		return repositories;	
	}
	
	@SuppressWarnings("unchecked")
	public List<SVNCommentEntity> getSVNCommentsOrderedByTimestamp(final User user, final boolean asc) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				Query query =  wrapper.createQuery("SELECT s " +
						  "FROM SVNCommentEntity s JOIN s.user u WHERE u.id = :user_id ORDER BY s.timestamp " + (asc ? "ASC" : "DESC"));
				query.setParameter("user_id", user.getId());
				wrapper.setOperationResult(query.list());	
			}
		});
		return (List<SVNCommentEntity>) wrapper.getOperationResult();
	}
	
	public List<OrganizationAdminUIDto> getUserOrganizations(ServiceInvocationContext context, boolean addUnassignedNode) {
		CommunicationChannel channel = (CommunicationChannel) context.getCommunicationChannel();
		User user = (User) channel.getPrincipal().getUser();	
		
		List<OrganizationAdminUIDto> list = new ArrayList<OrganizationAdminUIDto>();	
		for (Organization org : UserService.getInstance().getOrganizations(user)) {
			list.add(OrganizationService.getInstance().convertOrganizationToOrganizationAdminUIDto(org, user));
		}		
		if (addUnassignedNode && user.isAdmin()) {
			OrganizationAdminUIDto unknownOrg = new OrganizationAdminUIDto();
			unknownOrg.setId(-1);
			unknownOrg.setName("Unassigned");
			list.add(unknownOrg);
		}
		return list;
	}
	
	/**
	 * Checks if the <code>activationCode</code> provided is correct and activates the user.
	 */
	public boolean activateUser(final String login, final String activationCode) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				User user = wrapper.findByField(User.class, "login", login).get(0);
				if (user.isActivated()) {
					wrapper.setOperationResult(true);
					return;
				}
				if (user.getActivationCode().equals(activationCode)) {
					// activation successful
					user.setActivated(true);
					wrapper.merge(user);
					logger.debug("Activation successful for {} with activation code = {}", user, activationCode);
					wrapper.setOperationResult(true);
					return;
				}
				logger.debug("Activation failed for {}. Reason: wrong activation code.", user);
				wrapper.setOperationResult(false);
			}
		});
		return (boolean) wrapper.getOperationResult();
	}
	
	private final String USER_ACTIVATED_SUBJECT = "mail.template.user-activated.subject";
	private final String USER_ACTIVATED_BODY = "mail.template.user-activated.body";
	private final String USER_ACTIVATED_WELCOME_MESSAGE_WITH_ORGANIZATION = "entity.organization.help.afterActivation.contentWithOrganizationFilter";
	private final String USER_ACTIVATED_WELCOME_MESSAGE_WITHOUT_ORGANIZATION = "entity.organization.help.afterActivation.contentWithoutOrganizationFilter";
	private final String ORGANIZATION_INFORMATION = "entity.organization.help.whatis.content";
	
	
	/**
	 * Called from client side after the user activates his account.
	 * 
	 * @param organizationName the organization filter; a membership request was automatically made for the user on activation
	 */
	public void sendActivationEmail(final long userId, final String organizationName) {
		new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				User user = wrapper.find(User.class, userId);
				
				String subject = WebPlugin.getInstance().getMessage(USER_ACTIVATED_SUBJECT);
				
				String url, welcomeMessage;
				if (organizationName == null) {
					url = SendMailService.getInstance().getServerUrl();
					welcomeMessage = WebPlugin.getInstance().getMessage(USER_ACTIVATED_WELCOME_MESSAGE_WITHOUT_ORGANIZATION);
				} else {
					url = SendMailService.getInstance().getServerUrlForOrganization(organizationName);
					Organization organization = wrapper.findByField(Organization.class, "name", organizationName).get(0);
					welcomeMessage = WebPlugin.getInstance().getMessage(USER_ACTIVATED_WELCOME_MESSAGE_WITH_ORGANIZATION, organization.getLabel());
				}
				
				String text = WebPlugin.getInstance().getMessage(USER_ACTIVATED_BODY,  
							new Object[] {
								user.getLogin(),
								user.getName(),
								user.getEmail(),
								url,
								welcomeMessage.replace("\n", "<br/>"),
								WebPlugin.getInstance().getMessage(ORGANIZATION_INFORMATION)
							});
				
				SendMailService.getInstance().send(user.getEmail(), subject, text);
			}
		});
		
	}
	
	public final static String ORGANIZATION_DIRECTORIES = "users.directories-for-organization";
	public final static String ORGANIZATION_DEFAULT_DIRECTORY = "users.default-directory-for-organization";
	private final static String ORGANIZATION_APPROVED_CREATE_DIRS = "users.on-approve-organization.create-dirs";
	private final static String ORGANIZATION_APPROVED_CREATE_GROUPS = "users.on-approve-organization.create-groups";
	private final static String ORGANIZATION_APPROVED_CREATE_USERS = "users.on-approve-organization.create-users";
	private final static String ORGANIZATION_APPROVED_CREATE_PERMISSIONS = "users.on-approve-organization.create-permissions";
	private final static String ON_BECOME_ADMIN_ADD_USER_TO_GROUPS = "users.on-become-org-admin.add-user-to-groups";
	private final static String ON_APPROVE_MEMBERSHIP_ADD_USER_TO_GROUPS = "users.on-approve-membership-to-organization.add-member-to-groups";
	private final static String EMAIL_SHOULD_BE_UNIQUE = "users.email-unique";
	private final static String MIN_PASSWORD_LENGTH = "users.min-password-length";
	
	private static AddProperty getDefaultAddProperty(String propertyName, String propertyDefaultValue) {
		return new AddProperty(propertyName, propertyDefaultValue) {
			
			@Override
			protected String validateProperty(String input) {
				// do nothing
				return null;
			}
		};
	}
	
	private static AddProperty getPropertyWithGroupValidation(String propertyName, String propertyDefaultValue) {
		return new AddProperty(propertyName, propertyDefaultValue) {
			
			@Override
			protected String validateProperty(String input) {
				String createdGroups = CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_GROUPS);
				for (String group : input.split(",")) {
					if (!createdGroups.contains(group)) {
						return "Group " + group + " is not created on organization approve: " + createdGroups;
					}
				}
				
				return null;
			}
		};
	}
	
	static {
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getDefaultAddProperty(ORGANIZATION_DIRECTORIES, "{0}"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getDefaultAddProperty(ORGANIZATION_DEFAULT_DIRECTORY, "ws_trunk"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getDefaultAddProperty(ORGANIZATION_APPROVED_CREATE_DIRS, "{0}, {0}/ws_trunk"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getDefaultAddProperty(ORGANIZATION_APPROVED_CREATE_GROUPS, "{0}.admin, {0}.user"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getDefaultAddProperty(ORGANIZATION_APPROVED_CREATE_USERS, "anonymous.{0}"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(new AddProperty(ORGANIZATION_APPROVED_CREATE_PERMISSIONS, 
				"@{0}.admin: AdminSecurityEntitiesPermission(null, #{0})," +
				"@{0}.admin: ModifyTreePermissionsPermission({0}, #{0})," +
				"@{0}.admin: ModifyTreePermissionsPermission({0}/*, #{0})," +
				"@{0}.admin: FlowerWebFilePermission({0}, read-write-delete)," +
				"@{0}.admin: FlowerWebFilePermission({0}/*, read-write-delete)," +
				"#{0}: FlowerWebFilePermission({0}, read)," +
				"#{0}: FlowerWebFilePermission({0}/*, read)") {

					@Override
					protected String validateProperty(String input) {
						String createdGroups = CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_GROUPS);
						Pattern pattern = Pattern.compile("s*?@(.*?):s*?");
						Matcher matcher = pattern.matcher(input);
						while (matcher.find()) {
							String group = matcher.group(1);
							if (!createdGroups.contains(group)) {
								return "Group " + group + " is not created on organization approve: " + createdGroups;
							}
						}
						return null;
					}
			
		}) ;
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getPropertyWithGroupValidation(ON_BECOME_ADMIN_ADD_USER_TO_GROUPS, "{0}.admin")); 
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(getPropertyWithGroupValidation(ON_APPROVE_MEMBERSHIP_ADD_USER_TO_GROUPS, "{0}.user"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(new AddBooleanProperty(EMAIL_SHOULD_BE_UNIQUE, "true"));
		CommonPlugin.getInstance().getFlowerWebProperties().addProperty(new AddIntegerProperty(MIN_PASSWORD_LENGTH, "4") {

			@Override
			protected String validateProperty(String input) {
				String result = super.validateProperty(input);
				if (result != null) {
					return result;
				}
				if (Integer.valueOf(input) == 0) {
					return "Value cannot be 0";
				}
				return null;
			}
			
		});
	}
	
	private final String COMMENT_TEMPLATE_WITH_ADMIN_COMMENT = "mail.template.withAdminComment";
	private final String COMMENT_TEMPLATE_WITH_USER_COMMENT = "mail.template.withUserComment";
	private final String COMMENT_TEMPLATE_WITH_YOUR_COMMENT = "mail.template.withYourComment";
	
	private String getCommentForTemplate(String id, String comment) {
		if (comment == null)
			return "";
		return WebPlugin.getInstance().getMessage(id, comment.replace("\r", "<br/>"));
	}
	
	private final String ORGANIZATION_CREATED_TO_USER_SUBJECT = "mail.template.organization-created.to-user.subject";
	private final String ORGANIZATION_CREATED_TO_USER_BODY = "mail.template.organization-created.to-user.body";
	private final String ORGANIZATION_CREATED_TO_FDC_ADMIN_SUBJECT = "mail.template.organization-created.to-fdc-admin.subject";
	private final String ORGANIZATION_CREATED_TO_FDC_ADMIN_BODY = "mail.template.organization-created.to-fdc-admin.body";
	
	/**
	 * Creates the {@link Organization}. The {@link User} who requested this organization will be added to
	 * the organization with status <code>PENDING_NEW_ORGANIZATION_APPROVAL</code>. Mails are sent to the organization
	 * creator and FDC admins to notify about the creation of a new organization.
	 * 
	 * @param dto new organization
	 * @param commentForAdmin will be sent to the FDC administrators
	 * @return error message if the organization could not be created
	 */
	public String requestNewOrganization(final ServiceInvocationContext context, final OrganizationAdminUIDto dto, final String commentForAdmin) {
		logger.debug("New organization request from {} with comment = {}",
				((User) context.getCommunicationChannel().getPrincipal().getUser()), commentForAdmin);
		
		if (((User) context.getCommunicationChannel().getPrincipal().getUser()).getLogin().startsWith(ANONYMOUS)) {
			return "Anonymous user cannot request a new organization!";
		}
		
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				String errorMessage = OrganizationService.getInstance().mergeAdminUIDto(context, dto, true);
				if (errorMessage != null) {
					wrapper.setOperationResult(errorMessage);
					return;
				}
				
				long userId = CommunicationPlugin.tlCurrentPrincipal.get().getUserId();
				User currentUser = wrapper.find(User.class, userId);
				if (currentUser.isAdmin()) {
					// activate org automatically if this user is FDC admin
					wrapper.setOperationResult(approveDenyNewOrganization(context, dto, true, null));
					return;
				} else {
					// add the user to the organization
					Organization organization = wrapper.findByField(Organization.class, "name", dto.getName()).get(0);
					User organizationOwner = currentUser;
					UserService.getInstance().addOrganizationUserDependency(organizationOwner, organization, OrganizationMembershipStatus.ADMIN, wrapper);
					
					// send email to user
					String subject = WebPlugin.getInstance().getMessage(ORGANIZATION_CREATED_TO_USER_SUBJECT, dto.getLabel());
					String content = WebPlugin.getInstance().getMessage(ORGANIZATION_CREATED_TO_USER_BODY,
							new Object[] {
								organizationOwner.getLogin(),
								organizationOwner.getName(),
								organizationOwner.getEmail(),
								SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
								dto.getLabel(),
								getCommentForTemplate(COMMENT_TEMPLATE_WITH_YOUR_COMMENT, commentForAdmin)
							});
					
					SendMailService.getInstance().send(organizationOwner.getEmail(), subject, content);
					
					// send email to FDC admins
					subject = WebPlugin.getInstance().getMessage(ORGANIZATION_CREATED_TO_FDC_ADMIN_SUBJECT, dto.getLabel());
					for (User user : wrapper.findAll(User.class)) {
						if (user.isAdmin()) {
							content = WebPlugin.getInstance().getMessage(ORGANIZATION_CREATED_TO_FDC_ADMIN_BODY,
									new Object[] {
										user.getLogin(),
										user.getName(),
										user.getEmail(),
										SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
										organizationOwner.getName(), 
										dto.getLabel(), 
										getCommentForTemplate(COMMENT_TEMPLATE_WITH_USER_COMMENT, commentForAdmin)
									});
							
							SendMailService.getInstance().send(user.getEmail(), subject, content);
						}
					}
				}
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	private final String ORGANIZATION_APPROVED_SUBJECT = "mail.template.organization-approved.subject";
	private final String ORGANIZATION_APPROVED_BODY = "mail.template.organization-approved.body";
	private final String ORGANIZATION_DENIED_SUBJECT = "mail.template.organization-denied.subject";
	private final String ORGANIZATION_DENIED_BODY = "mail.template.organization-denied.body";
	private final String ORGANIZATION_APPROVED_OPERATIONS_EXECUTED = "mail.template.organization-approved.operations-executed";
	
	/**
	 * Only available to FDC admin.
	 */
	public String approveDenyNewOrganization(final ServiceInvocationContext context, final OrganizationAdminUIDto dto, final boolean approve, final String commentFromAdmin) {
		SecurityUtils.checkAdminSecurityEntitiesPermission(PermissionEntity.ANY_ENTITY);
		
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				Organization organization = wrapper.findByField(Organization.class, "name", dto.getName()).get(0);
				
				List<OrganizationUser> list = organization.getOrganizationUsers();
				User organizationOwner =  null;
				if (list.size() > 0) {
					OrganizationUser organizationUser = (OrganizationUser) list.toArray()[0];
					organizationOwner = organizationUser.getUser();
				}
				
				if (approve) {
					// approve request 
					logger.debug("Approve {}", organization);
					
					organization.setActivated(true);
					organization = wrapper.merge(organization);
					
					// create directories
					String createDirsProp = MessageFormat.format(
							CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_DIRS), 
							organization.getName());
					logger.debug("Create directories = {}", createDirsProp);
					String[] dirsList = createDirsProp.split(",\\s*");
					for (String dirName : dirsList) {
						File dir = new File(CommonPlugin.getInstance().getWorkspaceRoot(), dirName);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					}
					
					// create groups
					String createGroupsProp = MessageFormat.format(
							CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_GROUPS), 
							organization.getName());
					logger.debug("Create new groups = {}", createGroupsProp);
					String[] groupNames = createGroupsProp.split(",\\s*");
					for (String groupName : groupNames) {
						GroupAdminUIDto groupDto = new GroupAdminUIDto();
						groupDto.setName(groupName);
						groupDto.setOrganization(new NamedDto(dto.getId(), dto.getName()));
						groupDto.setOrganizationLabel(dto.getLabel());
						GroupService.getInstance().mergeAdminUIDto(context, groupDto);
					}
					
					// create users
					String createUsersProp = MessageFormat.format(CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_USERS), organization.getName());
					logger.debug("Create new users = {}", createUsersProp);
					String[] userNames = createUsersProp.split(",\\s*");
					for (String username : userNames) {
						UserAdminUIDto userDto = new UserAdminUIDto();
						userDto.setName(username);
						userDto.setLogin(username);
						userDto.setPassword(username);
						userDto.setIsActivated(true);
						try {
							mergeAdminUIDto(userDto);
							User newUser = wrapper.findByField(User.class, "login", username).get(0);
							addOrganizationUserDependency(newUser, organization, OrganizationMembershipStatus.MEMBER, wrapper);
						} catch (Exception e) {
							// do nothing
						}
					}
					
					// add admin to admin groups
					String adminGroups = MessageFormat.format(
							CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ON_BECOME_ADMIN_ADD_USER_TO_GROUPS), 
							organization.getName());
					if (organizationOwner != null) {
						logger.debug("Add {} to admin groups = {}", organizationOwner, adminGroups);
						addRemoveUserFromGroups(organizationOwner, adminGroups, true, wrapper);
					}
					
					// create permissions
					String createPermissionsProp = MessageFormat.format(
							CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ORGANIZATION_APPROVED_CREATE_PERMISSIONS), 
							organization.getName());
					logger.debug("Create permissions = {}", createPermissionsProp);
					Pattern pattern = Pattern.compile("\\s*?(.*?)\\s*:\\s*(\\w*)\\s*\\((.*?)\\),?\\s*?");
					Matcher matcher = pattern.matcher(createPermissionsProp);
					String permissionsCreated = "";
					while (matcher.find()) {
						String assignedTo = matcher.group(1);
						String permissionType = "org.flowerplatform.web.security.permission." + matcher.group(2);
						String paramList = matcher.group(3);
						String[] params = paramList.split("\\s*,\\s*");
						
						PermissionAdminUIDto permissionDto = new PermissionAdminUIDto();
						permissionDto.setAssignedTo(assignedTo);
						permissionDto.setType(permissionType);
						permissionDto.setName(params[0].equals("null") ? "" : params[0]);
						permissionDto.setActions(params[1]);
						
						try {
							PermissionService.getInstance().mergeAdminUIDto(context, permissionDto);
							permissionsCreated += "<br/>	" + permissionDto; // HTML format, because this info will be sent via mail
						} catch (Exception e) {
							logger.error(e.getMessage());
							// do nothing
						}
					}
					
					// compose message to send to client
					String message = WebPlugin.getInstance().getMessage(ORGANIZATION_APPROVED_OPERATIONS_EXECUTED, 
							new Object[] { 
								createDirsProp,
								createGroupsProp,
								createUsersProp,
								organizationOwner != null ? organizationOwner.getName() : "null",
								adminGroups,
								permissionsCreated
							});
							
					if (organizationOwner != null) {
						// send mail
						String subject = WebPlugin.getInstance().getMessage(ORGANIZATION_APPROVED_SUBJECT, organization.getLabel());
						String content = WebPlugin.getInstance().getMessage(ORGANIZATION_APPROVED_BODY, 
								new Object[] { 
									organizationOwner.getLogin(),
									organizationOwner.getName(),
									organizationOwner.getEmail(),
									SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
									organization.getLabel(), 
									message,
									getCommentForTemplate(COMMENT_TEMPLATE_WITH_ADMIN_COMMENT, commentFromAdmin)
								});
						
						SendMailService.getInstance().send(organizationOwner.getEmail(), subject, content);
					}
					
					wrapper.setOperationResult(message);
					return;
				} else {
					// deny request => delete organization
					logger.debug("Deny organization = {}", dto.getName());
					
					OrganizationService.getInstance().delete(Arrays.asList((int) dto.getId()));
					
					// send mail to user
					if (organizationOwner != null) {
						String subject = WebPlugin.getInstance().getMessage(ORGANIZATION_DENIED_SUBJECT, organization.getLabel());
						String content = WebPlugin.getInstance().getMessage(ORGANIZATION_DENIED_BODY, 
								new Object[] { 
									organizationOwner.getLogin(),
									organizationOwner.getName(),
									organizationOwner.getEmail(),
									SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
									organization.getLabel(),
									getCommentForTemplate(COMMENT_TEMPLATE_WITH_ADMIN_COMMENT, commentFromAdmin)
								});
						SendMailService.getInstance().send(organizationOwner.getEmail(), subject, content);
					}
				}
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	private final String MEMBERSHIP_REQUESTED_TO_USER_SUBJECT = "mail.template.membership-requested.to-user.subject";
	private final String MEMBERSHIP_REQUESTED_TO_USER_BODY = "mail.template.membership-requested.to-user.body";
	private final String MEMBERSHIP_REQUESTED_TO_ORG_ADMIN_SUBJECT = "mail.template.membership-requested.to-org-admin.subject";
	private final String MEMBERSHIP_REQUESTED_TO_ORG_ADMIN_BODY = "mail.template.membership-requested.to-org-admin.body";
	
	/**
	 * Called from client side. Adds the user to the organization's members list, with
	 * the {@link OrganizationMembershipStatus#PENDING_MEMBERSHIP_APPROVAL} status.
	 */
	public String requestMembership(final ServiceInvocationContext context, final long userId, final String organizationName, final String commmentForAdmin) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				User user = wrapper.find(User.class, userId);
				
				if (user.getLogin().startsWith(ANONYMOUS) && !((FlowerWebPrincipal) CommunicationPlugin.tlCurrentPrincipal.get()).getUser().isAdmin()) {
					wrapper.setOperationResult("Anonymous user cannot join organizations!");
					return;
				}
				
				List<Organization> list = wrapper.findByField(Organization.class, "name", organizationName);
				if (list.size() != 1) {
					wrapper.setOperationResult("Organization does not exist");
					return;
				}
				Organization organization = list.get(0);
				
				logger.debug("{} requesting membership to {}", user, organization);
					
				// cannot request membership for an organization that was not activated yet
				if (organization.isActivated()) {
					// first check if the user is already a member/has requested to be a member before
					if (OrganizationService.getInstance().getOrganizationMembershipStatus(organization, user) == null) {
						OrganizationUser ou = addOrganizationUserDependency(user, organization, OrganizationMembershipStatus.PENDING_MEMBERSHIP_APPROVAL, wrapper);
						
						// NOTE: if this is in fact the organization owner adding a new user to his organization, the following mails won't be sent at all
						if (((CommunicationChannel) context.getCommunicationChannel()).getPrincipal().getUserId() == user.getId()) {
							// send mail to user
							String subject = WebPlugin.getInstance().getMessage(MEMBERSHIP_REQUESTED_TO_USER_SUBJECT, organization.getLabel());
							String content = WebPlugin.getInstance().getMessage(MEMBERSHIP_REQUESTED_TO_USER_BODY,
									new Object[] { 
										user.getLogin(),
										user.getName(),
										user.getEmail(),
										SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
										organization.getLabel(),
										getCommentForTemplate(COMMENT_TEMPLATE_WITH_YOUR_COMMENT, commmentForAdmin)
									});
							SendMailService.getInstance().send(user.getEmail(), subject, content);
						
							// send mail to organization admins
							subject = WebPlugin.getInstance().getMessage(MEMBERSHIP_REQUESTED_TO_ORG_ADMIN_SUBJECT, organization.getLabel());
							for (OrganizationUser organizationUser : organization.getOrganizationUsers()) {
								if (organizationUser.getStatus().equals(OrganizationMembershipStatus.ADMIN)) {
									User orgOwner = organizationUser.getUser();
									content = WebPlugin.getInstance().getMessage(MEMBERSHIP_REQUESTED_TO_ORG_ADMIN_BODY,
											new Object[] { 
												orgOwner.getLogin(),
												orgOwner.getName(),
												orgOwner.getEmail(),
												SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
												organization.getLabel(), 
												getCommentForTemplate(COMMENT_TEMPLATE_WITH_USER_COMMENT , commmentForAdmin),
												user.getLogin(),
												user.getName(), 
												user.getEmail()
											});
									
									SendMailService.getInstance().send(orgOwner.getEmail(), subject, content);
								}
							}
						}
						
						// approve membership if current user is FDC admin or org admin
						String message = null;
						try {
							SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(organization, null));
							message = approveDenyMembership(context, ou.getId(), true, commmentForAdmin);
						} catch (Exception e) {
							// do nothing
						}
						
						wrapper.setOperationResult(message);
						return;
					} else {
						logger.debug("{} is already a member of {}", user, organization);
						wrapper.setOperationResult("User is already a member of " + organization.getName() + ".");
						return;
					}
				} else {
					logger.debug("Cannot request membership to an organization that is not yet activated!");
					wrapper.setOperationResult("Cannot request membership to an organization that is not yet activated!");
				}
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	private final String MEMBERSHIP_ACCEPTED_SUBJECT = "mail.template.membership.accepted.subject";
	private final String MEMBERSHIP_ACCEPTED_BODY = "mail.template.membership.accepted.body";
	private final String MEMBERSHIP_DENIED_SUBJECT = "mail.template.membership.denied.subject";
	private final String MEMBERSHIP_DENIED_BODY = "mail.template.membership.denied.body";
	
	/**
	 * Called from client side. The user's pending membership request is approved or denied. Returns a message to inform
	 * the client about the groups that the user was added to upon approving his membership request, or <code>null</code>
	 * if the user's request was denied.
	 */
	public String approveDenyMembership(ServiceInvocationContext context, final long organizationUserId, final boolean approve, final String commentForUser) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				OrganizationUser organizationUser = wrapper.find(OrganizationUser.class, organizationUserId);
				
				SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(organizationUser.getOrganization(), null));
				
				if (approve) {
					// accept membership request, add user to organization
					logger.debug("Approve membership request from {} to {}", organizationUser.getUser(), organizationUser.getOrganization());
					
					organizationUser.setStatus(OrganizationMembershipStatus.MEMBER);
					organizationUser = wrapper.merge(organizationUser);
					User user = organizationUser.getUser();
					for (OrganizationUser ou : user.getOrganizationUsers()) {
						if (ou.getId() == organizationUser.getId()) {
							ou.setStatus(OrganizationMembershipStatus.MEMBER);
							user = wrapper.merge(user);
							wrapper.merge(organizationUser.getOrganization());
							break;
						}
					}
					
					// send mail to user if comment was added
					if (commentForUser != null) {
						String subject = WebPlugin.getInstance().getMessage(MEMBERSHIP_ACCEPTED_SUBJECT, organizationUser.getOrganization().getLabel());
						String content = WebPlugin.getInstance().getMessage(MEMBERSHIP_ACCEPTED_BODY,
								new Object[] { 
									user.getLogin(),
									user.getName(),
									user.getEmail(),
									SendMailService.getInstance().getServerUrlForOrganization(organizationUser.getOrganization().getName()),
									organizationUser.getOrganization().getLabel(),
									commentForUser == null ? "" : getCommentForTemplate(COMMENT_TEMPLATE_WITH_ADMIN_COMMENT, commentForUser)
									// don't add the comment if no comment was sent
								});
						
						SendMailService.getInstance().send(user.getEmail(), subject, content);	
					}
					
					// add user to groups
					String prop = CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ON_APPROVE_MEMBERSHIP_ADD_USER_TO_GROUPS);
					
					String groupsString = MessageFormat.format(prop, organizationUser.getOrganization().getName());
					String result = addRemoveUserFromGroups(user, groupsString, true, wrapper);
					result += WebPlugin.getInstance().getMessage("entity.group.manuallyAddUserToGroups");
					wrapper.setOperationResult(result);
					return;
				} else {
					// deny membership request, unassociate user from organization
					logger.debug("Deny membership request from {} to {}", organizationUser.getUser(), organizationUser.getOrganization());
					
					Organization organization = organizationUser.getOrganization();
					
					User user = organizationUser.getUser();
					for (Iterator<OrganizationUser> it = user.getOrganizationUsers().iterator(); it.hasNext();) {
						OrganizationUser ou = it.next();
						if (ou.getId() == organizationUser.getId()) {
							it.remove();
							organizationUser.getOrganization().getOrganizationUsers().remove(ou);
							break;
						}
					}
					wrapper.delete(organizationUser);
					
					// send mail to user
					String subject = WebPlugin.getInstance().getMessage(MEMBERSHIP_DENIED_SUBJECT, organization.getLabel());
					String content = WebPlugin.getInstance().getMessage(MEMBERSHIP_DENIED_BODY,
							new Object[] { 
								user.getLogin(),
								user.getName(),
								user.getEmail(),
								SendMailService.getInstance().getServerUrlForOrganization(organization.getName()),
								organization.getLabel(),
								getCommentForTemplate(COMMENT_TEMPLATE_WITH_ADMIN_COMMENT, commentForUser)
							});
					SendMailService.getInstance().send(user.getEmail(), subject, content);	
				}
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	public String createAndApproveAsOrganizationMember(final ServiceInvocationContext context, final UserAdminUIDto userDto) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				String errorMessage = mergeAdminUIDto(userDto);
				if (errorMessage != null) {
					wrapper.setOperationResult(errorMessage);
					return;
				}
				
				User user = wrapper.findByField(User.class, "login", userDto.getLogin()).get(0);
				OrganizationUser ou = null;
				if (user.getOrganizationUsers().size() > 0) {
					ou = (OrganizationUser) user.getOrganizationUsers().toArray()[0];
				}
				if (!user.isActivated()) {
					sendActivationCodeForUser(user, ou == null ? null : ou.getOrganization().getName());
				}
				if (ou != null) {
					approveDenyMembership(context, ou.getId(), true, null);
				}
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	private final String LEAVE_ORGANIZATION_TO_USER_SUBJECT = "mail.template.leave-organization.to-user.subject";
	private final String LEAVE_ORGANIZATION_TO_USER_BODY = "mail.template.leave-organization.to-user.body";
	private final String LEAVE_ORGANIZATION_TO_ADMIN_SUBJECT = "mail.template.leave-organization.to-admin.subject";
	private final String LEAVE_ORGANIZATION_TO_ADMIN_BODY = "mail.template.leave-organization.to-admin.body";
	
	/**
	 * Removes the user from the given organizations and from the organization's groups. Sends mail to
	 * user and organization's admins to notify that the user left the organization.
	 * 
	 * @param userDto user that requested to leave
	 * @param organizationDtos the organizations that the user is leaving
	 */
	public String leaveOrganizations(final ServiceInvocationContext context, final UserAdminUIDto userDto, final List<OrganizationAdminUIDto> organizationDtos, final String commentForAdmin) {
		if (userDto.getLogin().startsWith(ANONYMOUS) && !((FlowerWebPrincipal) CommunicationPlugin.tlCurrentPrincipal.get()).getUser().isAdmin()) {
			return "Anonymous user cannot be removed from organizations!";
		}
		
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				String removedFromGroupsMessage = new String();
				for (Iterator<OrganizationUserAdminUIDto> organizationIt = userDto.getOrganizationUsers().iterator(); organizationIt.hasNext();) {
					OrganizationUserAdminUIDto ou = organizationIt.next();
					for (OrganizationAdminUIDto removedOrg : organizationDtos) {
						if (ou.getOrganization().getId() == removedOrg.getId()) {
							// remove from organization
							logger.debug("Remove user = {} from organization = {}", userDto.getLogin(), removedOrg.getName());
							organizationIt.remove();
							
							// remove from groups belonging to organization
							for (@SuppressWarnings("unchecked")
							Iterator<GroupAdminUIDto> groupIt = (Iterator<GroupAdminUIDto>) userDto.getGroups().iterator(); groupIt.hasNext();) {
								GroupAdminUIDto group = groupIt.next();
								if (group.getOrganization() != null && group.getOrganization().getId() == ou.getOrganization().getId()) {
									groupIt.remove();
									removedFromGroupsMessage += "<li>" + group.getName() + "</li>";
								}
							}
							boolean wasRemovedByAdmin = ((CommunicationChannel) context.getCommunicationChannel()).getPrincipal().getUserId() != userDto.getId();
							if (!wasRemovedByAdmin) {
								removedFromGroupsMessage = WebPlugin.getInstance().getMessage("entity.group.youWereRemovedFromGroups", removedFromGroupsMessage);
							} else {
								removedFromGroupsMessage = WebPlugin.getInstance().getMessage("entity.group.userWasRemovedFromGroups", removedFromGroupsMessage); 
							}
							
							// send mail to user if a comment was added
							if (commentForAdmin != null) {
								String subject = WebPlugin.getInstance().getMessage(LEAVE_ORGANIZATION_TO_USER_SUBJECT, ou.getOrganization().getLabel());
								String text = WebPlugin.getInstance().getMessage(LEAVE_ORGANIZATION_TO_USER_BODY,
										new Object[] { 
											userDto.getLogin(),
											userDto.getName(),
											userDto.getEmail(),
											SendMailService.getInstance().getServerUrlForOrganization(ou.getOrganization().getURL()),
											ou.getOrganization().getLabel(),
											wasRemovedByAdmin && (commentForAdmin != null) ? getCommentForTemplate(COMMENT_TEMPLATE_WITH_ADMIN_COMMENT, commentForAdmin) : ""
										});
								
								SendMailService.getInstance().send(userDto.getEmail(), subject, text);
							}
							
							// send mail to organization admins
							Organization organization = wrapper.findByField(Organization.class, "name", ou.getOrganization().getName()).get(0);
							for (OrganizationUser orgAdmin : organization.getOrganizationUsers()) {
								if (orgAdmin.getStatus().equals(OrganizationMembershipStatus.ADMIN)) {
									String subject = WebPlugin.getInstance().getMessage(LEAVE_ORGANIZATION_TO_ADMIN_SUBJECT, ou.getOrganization().getLabel());
									String text = WebPlugin.getInstance().getMessage(LEAVE_ORGANIZATION_TO_ADMIN_BODY,
											new Object[] { 
												orgAdmin.getUser().getLogin(),
												orgAdmin.getUser().getName(),
												orgAdmin.getUser().getEmail(),
												SendMailService.getInstance().getServerUrlForOrganization(ou.getOrganization().getURL()),
												ou.getOrganization().getName(),
												userDto.getLogin(), 
												userDto.getName(),
												userDto.getEmail()
											});
									
									SendMailService.getInstance().send(orgAdmin.getUser().getEmail(), subject, text);
								}
							}
							break;
						}
					}
				}
				
				// add changes to DB
				String message = mergeAdminUIDto(userDto);
				if (message != null) {
					wrapper.setOperationResult(message);
					return;
				}
				wrapper.setOperationResult(removedFromGroupsMessage);
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	public String upgradeDowngradeUser(final List<Integer> organizationUserIds, final boolean upgrade) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				OrganizationMembershipStatus newStatus = upgrade ? OrganizationMembershipStatus.ADMIN : OrganizationMembershipStatus.MEMBER;
				String message = "";
				for (int ouId : organizationUserIds) {
					OrganizationUser ou = wrapper.find(OrganizationUser.class, (long) ouId);
					
					if (!ou.getStatus().equals(newStatus)) {
						try {
							SecurityUtils.checkAdminSecurityEntitiesPermission(SecurityEntityAdaptor.toCsvString(ou.getOrganization(), null));
							
							ou.setStatus(newStatus);
							ou = wrapper.merge(ou);
							
							logger.debug("{} is now " + newStatus + " in {}", ou.getUser(), ou.getOrganization());
						
							// in case of upgrade, add to admin groups and remove from member groups
							// in case of downgrade, add to member groups and remove from admin groups
							String adminGroups = MessageFormat.format(CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ON_BECOME_ADMIN_ADD_USER_TO_GROUPS), 
									ou.getOrganization().getName());
							message += addRemoveUserFromGroups(ou.getUser(), adminGroups, upgrade, wrapper);
							
							String memberGroups = MessageFormat.format(CommonPlugin.getInstance().getFlowerWebProperties().getProperty(ON_APPROVE_MEMBERSHIP_ADD_USER_TO_GROUPS), 
									ou.getOrganization().getName());
							message += addRemoveUserFromGroups(ou.getUser(), memberGroups, !upgrade, wrapper);
							
						} catch (Exception e) {
							logger.debug("{} is not allowed to upgrade/downgrade members of {}",
									CommunicationPlugin.tlCurrentPrincipal.get().getUser(),
									ou.getOrganization());
						}
					}
				}
				wrapper.setOperationResult(message);
			}
		});
		return (String) wrapper.getOperationResult();
	}
	
	private String addRemoveUserFromGroups(final User user, final String groupsNames, final boolean add, DatabaseOperationWrapper wrapper) {
		String[] adminGroupNames = groupsNames.split(",\\s*");
		String namesList = "";
		String doNotExist = "";
		for (String groupName : adminGroupNames) {
			List<Group> list = wrapper.findByField(Group.class, "name", groupName);
			if (list.size() == 0) {
				doNotExist += "<li>" + groupName + "</li>";
			} else {
				Group group = list.get(0);
				namesList += "<li>" + group.getName() + "</li>";
				if (add) {
					// check if user is already part of the group
					boolean alreadyInGroup = false;
					for (GroupUser groupUser : user.getGroupUsers()) {
						if (groupUser.getGroup().getId() == group.getId()) {
							alreadyInGroup = true;
						}
					}
					if (!alreadyInGroup) {
						addGroupUserDependency(user, group, wrapper);
					}
				} else {
					for (GroupUser gu : group.getGroupUsers()) {
						if (gu.getUser().getId() == user.getId()) {
							removeGroupUserDependency(gu);
							user.getGroupUsers().remove(gu);
							wrapper.merge(user);
							break;
						}
					}
				}
			}
		}
		String message = add ? WebPlugin.getInstance().getMessage("entity.group.userWasAddedToGroups", namesList) :
			WebPlugin.getInstance().getMessage("entity.group.userWasRemovedFromGroups", namesList);
		if (doNotExist.length() > 0) {
			message += WebPlugin.getInstance().getMessage("entity.group.userWasNotAddedToGroups", doNotExist);
		}
		return message;
	}

	private final String SEND_ACTIVATION_CODE_SUBJECT = "mail.template.send-activation-code.subject";
	private final String SEND_ACTIVATION_CODE_BODY = "mail.template.send-activation-code.body";
	
	/**
	 * Sends an email containing the user's activation link and activation code. Returns
	 * the activation code.
	 */
	protected String sendActivationCodeForUser(final User user, String organizationFilter) {
		DatabaseOperationWrapper wrapper = new DatabaseOperationWrapper(new DatabaseOperation() {
			
			@Override
			public void run() {
				String activationCode = user.getActivationCode();
				if (activationCode == null) {
					activationCode = generateRandomString();
					user.setActivationCode(activationCode);
					wrapper.merge(user);
					wrapper.setOperationResult(activationCode);
				}
			}
		});
		String activationCode = (String) wrapper.getOperationResult();
		String subject = WebPlugin.getInstance().getMessage(SEND_ACTIVATION_CODE_SUBJECT);
		String url = organizationFilter == null ? SendMailService.getInstance().getServerUrl() : 
												  SendMailService.getInstance().getServerUrlForOrganization(organizationFilter);
		String text = WebPlugin.getInstance().getMessage(SEND_ACTIVATION_CODE_BODY, 
				new Object[] { 
						user.getLogin(),
						user.getName(),
						user.getEmail(),
						url,
						url + (url.indexOf("?") < 0 ? "?" : "&") + "login=" + user.getLogin() + "&activationCode=" + activationCode, 
						activationCode
				});
		
		SendMailService.getInstance().send(user.getEmail(), subject, text);
		return activationCode;
	}
	
	private static Random random = new Random();
	
	protected String generateRandomString() {
		byte[] bytes = new byte[20];
		for (int i =0; i < bytes.length; i++) {
			bytes[i] = (byte) (97 + random.nextInt(25));
		}
		return new String(bytes);
	}
	
}