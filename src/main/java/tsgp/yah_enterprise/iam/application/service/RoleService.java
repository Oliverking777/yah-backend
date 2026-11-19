package tsgp.yah_enterprise.iam.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.CreateRoleCommand;
import tsgp.yah_enterprise.iam.application.dto.RolePageRequest;
import tsgp.yah_enterprise.iam.application.dto.RolePageResult;
import tsgp.yah_enterprise.iam.application.dto.RoleResult;
import tsgp.yah_enterprise.iam.application.dto.UpdateRoleCommand;
import tsgp.yah_enterprise.iam.application.port.in.CreateRoleUseCase;
import tsgp.yah_enterprise.iam.application.port.in.DeleteRoleUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetRoleUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ListRolesUseCase;
import tsgp.yah_enterprise.iam.application.port.in.UpdateRoleUseCase;
import tsgp.yah_enterprise.iam.domain.exception.RoleAlreadyExistsException;
import tsgp.yah_enterprise.iam.domain.exception.RoleInUseException;
import tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException;
import tsgp.yah_enterprise.iam.domain.model.LocationScope;
import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

/**
 * Application service implementing all role use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService implements
        CreateRoleUseCase,
        UpdateRoleUseCase,
        GetRoleUseCase,
        ListRolesUseCase,
        DeleteRoleUseCase {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public RoleResult createRole(CreateRoleCommand request) {
        RoleName roleName = RoleName.of(request.roleName());

        if (roleRepository.existsByName(roleName)) {
            throw new RoleAlreadyExistsException(roleName);
        }

        LocationScope locationScope = request.locationScope() == null
                ? LocationScope.ASSIGNED
                : LocationScope.from(request.locationScope());

        Role role = Role.createRole(request.roleName(), request.description(), locationScope);
        return RoleResult.from(roleRepository.save(role));
    }

    @Override
    public RoleResult updateRole(UUID roleId, UpdateRoleCommand request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        if (request.roleName() != null) {
            RoleName newName = RoleName.of(request.roleName());
            boolean nameChanged = !newName.equals(role.getRoleName());

            if (nameChanged && roleRepository.existsByName(newName)) {
                throw new RoleAlreadyExistsException(newName);
            }
            if (nameChanged) {
                role.changeRoleName(request.roleName());
            }
        }

        if (request.description() != null) {
            role.changeDescription(request.description());
        }

        if (request.locationScope() != null) {
            LocationScope newScope = LocationScope.from(request.locationScope());
            if (newScope != role.getLocationScope()) {
                role.changeLocationScope(newScope);
            }
        }

        return RoleResult.from(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResult getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .map(RoleResult::from)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResult getRoleByName(String roleName) {
        RoleName name = RoleName.of(roleName);
        return roleRepository.findByName(name)
                .map(RoleResult::from)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }

    @Override
    @Transactional(readOnly = true)
    public RolePageResult listRoles(RolePageRequest request) {
        List<RoleResult> content = roleRepository.findAll(request.page(), request.size())
                .stream()
                .map(RoleResult::from)
                .toList();

        long total = roleRepository.count();
        return RolePageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        role.ensureDeletable(); // built-in roles cannot be deleted
        if (userRepository.existsByRoleId(roleId)) {
            throw new RoleInUseException(roleId);
        }
        roleRepository.deleteById(roleId);
    }
}