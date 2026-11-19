package tsgp.yah_enterprise.iam.application.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.BulkRolePermissionsCommand;
import tsgp.yah_enterprise.iam.application.dto.PermissionResult;
import tsgp.yah_enterprise.iam.application.dto.ReplaceRolePermissionsCommand;
import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;
import tsgp.yah_enterprise.iam.application.port.in.BulkAddRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.BulkRemoveRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.RemoveRolePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ReplaceRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException;
import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.repository.PermissionRepository;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;

/**
 * Application service managing which permissions each role grants.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService implements
        GetRolePermissionsUseCase,
        ReplaceRolePermissionsUseCase,
        BulkAddRolePermissionsUseCase,
        BulkRemoveRolePermissionsUseCase,
        RemoveRolePermissionUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public RolePermissionsResult getRolePermissions(UUID roleId) {
        Role role = findRole(roleId);
        return toResult(role, permissionRepository.findAllByIds(role.getPermissionIds()));
    }

    @Override
    public RolePermissionsResult replaceRolePermissions(UUID roleId, ReplaceRolePermissionsCommand command) {
        Role role = findRole(roleId);
        Set<UUID> requestedIds = idsOf(command.permissionIds());

        List<Permission> permissions = loadExistingPermissions(requestedIds);

        role.replacePermissions(requestedIds);
        Role saved = roleRepository.save(role);
        return toResult(saved, permissions);
    }

    @Override
    public RolePermissionsResult addRolePermissions(UUID roleId, BulkRolePermissionsCommand command) {
        Role role = findRole(roleId);
        Set<UUID> idsToAdd = idsOf(command.permissionIds());

        loadExistingPermissions(idsToAdd); // fails with PermissionNotFoundException if any id is unknown

        role.assignPermissions(idsToAdd);
        Role saved = roleRepository.save(role);
        return toResult(saved, permissionRepository.findAllByIds(saved.getPermissionIds()));
    }

    @Override
    public RolePermissionsResult removeRolePermissions(UUID roleId, BulkRolePermissionsCommand command) {
        Role role = findRole(roleId);

        role.removePermissions(idsOf(command.permissionIds()));
        Role saved = roleRepository.save(role);
        return toResult(saved, permissionRepository.findAllByIds(saved.getPermissionIds()));
    }

    @Override
    public void removeRolePermission(UUID roleId, UUID permissionId) {
        Role role = findRole(roleId);
        role.removePermission(permissionId);
        roleRepository.save(role);
    }

    // ---- helpers ----

    private Role findRole(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    private static Set<UUID> idsOf(Set<UUID> ids) {
        return ids == null ? Set.of() : Set.copyOf(ids);
    }

    /**
     * Loads the permissions for the given ids, or throws if any of them does not exist.
     */
    private List<Permission> loadExistingPermissions(Set<UUID> ids) {
        List<Permission> permissions = permissionRepository.findAllByIds(ids);

        Set<UUID> foundIds = permissions.stream()
                .map(Permission::getPermissionId)
                .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(ids);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new PermissionNotFoundException(missingIds);
        }
        return permissions;
    }

    private RolePermissionsResult toResult(Role role, List<Permission> permissions) {
        List<PermissionResult> sorted = permissions.stream()
                .sorted(Comparator.comparing(permission -> permission.getPermissionCode().value()))
                .map(PermissionResult::from)
                .toList();
        return new RolePermissionsResult(role.getRoleId(), role.getRoleName().value(), sorted);
    }
}