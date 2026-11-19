package tsgp.yah_enterprise.iam.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.CreatePermissionCommand;
import tsgp.yah_enterprise.iam.application.dto.PermissionPageRequest;
import tsgp.yah_enterprise.iam.application.dto.PermissionPageResult;
import tsgp.yah_enterprise.iam.application.dto.PermissionResult;
import tsgp.yah_enterprise.iam.application.dto.UpdatePermissionCommand;
import tsgp.yah_enterprise.iam.application.port.in.CreatePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.DeletePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetPermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ListPermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.UpdatePermissionUseCase;
import tsgp.yah_enterprise.iam.domain.exception.PermissionAlreadyExistsException;
import tsgp.yah_enterprise.iam.domain.exception.PermissionInUseException;
import tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException;
import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.domain.repository.PermissionRepository;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;

/**
 * Application service implementing all permission use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService implements
        CreatePermissionUseCase,
        UpdatePermissionUseCase,
        GetPermissionUseCase,
        ListPermissionsUseCase,
        DeletePermissionUseCase {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public PermissionResult createPermission(CreatePermissionCommand command) {
        PermissionCode code = PermissionCode.of(command.permissionCode());

        if (permissionRepository.existsByCode(code)) {
            throw new PermissionAlreadyExistsException(code);
        }

        Permission permission = Permission.createPermission(command.permissionCode());
        return PermissionResult.from(permissionRepository.save(permission));
    }

    @Override
    public PermissionResult updatePermission(UUID permissionId, UpdatePermissionCommand command) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        PermissionCode newCode = PermissionCode.of(command.permissionCode());

        if (!newCode.equals(permission.getPermissionCode())) {
            if (permissionRepository.existsByCode(newCode)) {
                throw new PermissionAlreadyExistsException(newCode);
            }
            permission.changePermissionCode(command.permissionCode());
        }

        return PermissionResult.from(permissionRepository.save(permission));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResult getPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .map(PermissionResult::from)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResult getPermissionByCode(String permissionCode) {
        PermissionCode code = PermissionCode.of(permissionCode);
        return permissionRepository.findByCode(code)
                .map(PermissionResult::from)
                .orElseThrow(() -> new PermissionNotFoundException(code));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionPageResult listPermissions(PermissionPageRequest request) {
        List<PermissionResult> content = permissionRepository.findAll(request.page(), request.size())
                .stream()
                .map(PermissionResult::from)
                .toList();

        long total = permissionRepository.count();
        return PermissionPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public void deletePermission(UUID permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new PermissionNotFoundException(permissionId);
        }
        if (roleRepository.existsByPermissionId(permissionId)) {
            throw new PermissionInUseException(permissionId);
        }
        permissionRepository.deleteById(permissionId);
    }
}