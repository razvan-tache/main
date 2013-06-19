package org.flowerplatform.web.security.sandbox;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.security.Policy;
import java.util.List;

import org.flowerplatform.communication.CommunicationPlugin;
import org.flowerplatform.web.WebPlugin;
import org.flowerplatform.web.entity.ISecurityEntity;
import org.flowerplatform.web.entity.Organization;
import org.flowerplatform.web.entity.PermissionEntity;
import org.flowerplatform.web.entity.User;
import org.flowerplatform.web.entity.dto.NamedDto;
import org.flowerplatform.web.security.permission.AdminSecurityEntitiesPermission;
import org.flowerplatform.web.security.permission.ModifyTreePermissionsPermission;
import org.flowerplatform.web.security.permission.PermissionDescriptor;
import org.flowerplatform.web.security.service.OrganizationService;

import sun.security.util.SecurityConstants;

/**
 * 
 * @author Florin
 * 
 * @flowerModelElementId _a8iYsHTVEeGlbboTCbsTMQ
 */
public class SecurityUtils {

	/**
	 * @flowerModelElementId _a8i_wXTVEeGlbboTCbsTMQ
	 */
	public static boolean hasReadPermission(File file) {
		return hasPermission(new FilePermission(file.getAbsolutePath(), SecurityConstants.FILE_READ_ACTION));
	}

	public boolean hasWritePermission(File file) {
		if (file == null) {
			// for the moment this might happen during testing, but might happen in production in the future as well
			return false;
		}
		return hasPermission(new FilePermission(file.getAbsolutePath(), SecurityConstants.FILE_WRITE_ACTION));	
	}
	
	/**
	 * @flowerModelElementId _a8i_xHTVEeGlbboTCbsTMQ
	 */
	public static boolean hasPermission(Permission perm) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			try {
				sm.checkPermission(perm);
			} catch (SecurityException e) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Throws SecurityException if the current principal does not have permission to create the permissionEntity
	 * (for a tree permission).
	 * 
	 * @param permissionEntity
	 * @return
	 * @flowerModelElementId _Z_AWMHgGEeGtTo1wOb4S9A
	 */
	public static void checkModifyTreePermission(PermissionEntity permissionEntity) {
		FlowerWebPolicy policy = (FlowerWebPolicy) Policy.getPolicy();
		PermissionDescriptor descriptor = policy.getPermissionDescriptor(permissionEntity.getType());
		if (descriptor.isTreePermission()) {
			Class<? extends Permission> treePermissionClass = descriptor.getImplementedPermissionType();
			ModifyTreePermissionsPermission modifyPermission;
			if (treePermissionClass.equals(ModifyTreePermissionsPermission.class)) {
				StringBuilder actions = new StringBuilder();
				actions.append(permissionEntity.getAssignedTo());
				actions.append(",");
				actions.append(permissionEntity.getActions());
				String path = policy.getRuntimeWorkspace() + "/" + permissionEntity.getName();
				modifyPermission = new ModifyTreePermissionsPermission(path, actions.toString());
			} else {
				String path = policy.getRuntimeWorkspace() + "/" + permissionEntity.getName();
				modifyPermission = new ModifyTreePermissionsPermission(path, permissionEntity.getAssignedTo());
			}
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				sm.checkPermission(modifyPermission);
			}
		} 
	}
	
	/**
	 * @param groupCsvList - list of groups (as a comma separated value string) to which the user is assigned or will be assigned. 
 	 * 
 	 * @author Florin
	 * @author Mariana
	 * 
	 * @flowerModelElementId _Z_BkUngGEeGtTo1wOb4S9A
	 */
	public static void checkAdminSecurityEntitiesPermission(String groupCsvList) {	
		AdminSecurityEntitiesPermission permission = new AdminSecurityEntitiesPermission("", groupCsvList);
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(permission);
		}
	}
	
	/**
	 * Checks if the user has {@link AdminSecurityEntitiesPermission} for any of the existing {@link Organization}s in the DB by
	 * using {@link OrganizationService} to iterate through all the organizations.
	 * 
	 * @return true if the user has permissions for at least one organization, false otherwise
	 * @author Mariana
	 */
	public static boolean hasAdminSecurityEntitiesPermission() {
		try { 
			SecurityUtils.checkAdminSecurityEntitiesPermission(PermissionEntity.ANY_ENTITY);
			return true;
		} catch (SecurityException e) {
		}
		
		List<NamedDto> list = ((OrganizationService) OrganizationService.getInstance()).findAllAsNamedDto();
		if (list.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param groupCsvList
	 */
	public static void checkCurrentUserIsAdmin(String errorMessage) {
		FlowerWebPrincipal principal = (FlowerWebPrincipal) CommunicationPlugin.tlCurrentPrincipal.get();
		if (principal != null) {
			User user = principal.getUser();
			if (!user.isAdmin()) {
				throw new SecurityException(errorMessage);
			}
		}
	}
	
	/**
	 * @flowerModelElementId _Z_DZgXgGEeGtTo1wOb4S9A
	 */
	public static boolean securityEntityIsAssignable(List<ISecurityEntity> assignableSecurityEntities, ISecurityEntity securityEntityToCheck) {
		boolean is = false;
		for (ISecurityEntity assignableSecurityEntity: assignableSecurityEntities) {
			if (assignableSecurityEntity.contains(securityEntityToCheck)) {
				is = true;
				break;
			}
		}
		return is;
	}
	
	/**
	 * @author Mariana
	 */
	public static String validateSecurityEntity(String assignedTo) {
		ISecurityEntity entity = SecurityEntityAdaptor.toSecurityEntity(assignedTo);
		if (entity == null) {
			String message;
			if (assignedTo.startsWith(PermissionEntity.ORGANIZATION_PREFIX)) {
				message = "entity.permission.validation.organizationsNotFound";
			} else {
				if (assignedTo.startsWith(PermissionEntity.GROUP_PREFIX)) {
					message = "entity.permission.validation.groupsNotFound";
				} else {
					if (assignedTo.startsWith(PermissionEntity.USER_PREFIX)) {
						message = "entity.permission.validation.usersNotFound";
					} else {
						// no prefix
						message = "entity.permission.validation.invalidPrefix";
					}
				}
			}
			return WebPlugin.getInstance().getMessage(message, assignedTo);
		}
		return null;
	}
}