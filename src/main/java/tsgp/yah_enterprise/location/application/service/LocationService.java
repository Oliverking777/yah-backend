package tsgp.yah_enterprise.location.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.location.application.dto.AssignUserToLocationCommand;
import tsgp.yah_enterprise.location.application.dto.CreateLocationCommand;
import tsgp.yah_enterprise.location.application.dto.LocationPageRequest;
import tsgp.yah_enterprise.location.application.dto.LocationPageResult;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.dto.LocationUserResult;
import tsgp.yah_enterprise.location.application.dto.UpdateLocationCommand;
import tsgp.yah_enterprise.location.application.port.in.AssignUserToLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.ChangeLocationStatusUseCase;
import tsgp.yah_enterprise.location.application.port.in.CreateLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.ListLocationUsersUseCase;
import tsgp.yah_enterprise.location.application.port.in.ListLocationsUseCase;
import tsgp.yah_enterprise.location.application.port.in.RemoveUserFromLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.UpdateLocationUseCase;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;
import tsgp.yah_enterprise.location.domain.exception.LocationUserNotFoundException;
import tsgp.yah_enterprise.location.domain.exception.UserAlreadyAssignedException;
import tsgp.yah_enterprise.location.domain.exception.UserNotAssignedException;
import tsgp.yah_enterprise.location.domain.model.Location;
import tsgp.yah_enterprise.location.domain.model.LocationAssignment;
import tsgp.yah_enterprise.location.domain.model.LocationCode;
import tsgp.yah_enterprise.location.domain.model.LocationType;
import tsgp.yah_enterprise.location.domain.model.LocationUser;
import tsgp.yah_enterprise.location.domain.port.LocationCodeGenerator;
import tsgp.yah_enterprise.location.domain.port.UserDirectoryPort;
import tsgp.yah_enterprise.location.domain.repository.LocationAssignmentRepository;
import tsgp.yah_enterprise.location.domain.repository.LocationRepository;

/**
 * Application service implementing all location use cases.
 * Who may call each use case (Owner, Manager, Employee) is decided by the web/security layer.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LocationService implements
        CreateLocationUseCase,
        UpdateLocationUseCase,
        GetLocationUseCase,
        ListLocationsUseCase,
        ChangeLocationStatusUseCase,
        AssignUserToLocationUseCase,
        RemoveUserFromLocationUseCase,
        ListLocationUsersUseCase,
        GetUserLocationUseCase {

    private final LocationRepository locationRepository;
    private final LocationAssignmentRepository assignmentRepository;
    private final UserDirectoryPort userDirectoryPort;
    private final LocationCodeGenerator locationCodeGenerator;

    /** A generated code can only be taken already by a location made by hand in the same format: try the next one. */
    private static final int MAX_CODE_ATTEMPTS = 50;

    // ---------- locations ----------

    @Override
    public LocationResult createLocation(CreateLocationCommand command) {
        LocationType type = LocationType.from(command.locationType());
        LocationCode code = nextFreeCode();

        Location location = Location.create(command.name(), code, type, command.address());
        return LocationResult.from(locationRepository.save(location));
    }

    @Override
    public LocationResult updateLocation(UUID locationId, UpdateLocationCommand command) {
        Location location = findLocation(locationId);

        if (command.name() != null || command.locationType() != null || command.address() != null) {
            String name = command.name() != null ? command.name() : location.getName();
            LocationType type = command.locationType() != null
                    ? LocationType.from(command.locationType())
                    : location.getLocationType();
            String address = command.address() != null ? command.address() : location.getAddress();
            location.updateDetails(name, type, address);
        }

        return LocationResult.from(locationRepository.save(location));
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResult getLocationById(UUID locationId) {
        return LocationResult.from(findLocation(locationId));
    }

    @Override
    @Transactional(readOnly = true)
    public LocationPageResult listLocations(LocationPageRequest request) {
        List<LocationResult> content = locationRepository.findAll(request.page(), request.size())
                .stream()
                .map(LocationResult::from)
                .toList();

        long total = locationRepository.count();
        return LocationPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public LocationResult activateLocation(UUID locationId) {
        Location location = findLocation(locationId);
        location.activate();
        return LocationResult.from(locationRepository.save(location));
    }

    @Override
    public LocationResult deactivateLocation(UUID locationId) {
        Location location = findLocation(locationId);
        location.deactivate();
        return LocationResult.from(locationRepository.save(location));
    }

    // ---------- user assignments ----------

    @Override
    public LocationUserResult assignUser(UUID locationId, AssignUserToLocationCommand command) {
        Location location = findLocation(locationId);

        LocationUser user = userDirectoryPort.findById(command.userId())
                .orElseThrow(() -> new LocationUserNotFoundException(command.userId()));

        if (assignmentRepository.existsByUserId(user.userId())) {
            throw new UserAlreadyAssignedException(user.userId());
        }

        // The domain checks that the location and the user are both active.
        LocationAssignment assignment = LocationAssignment.assign(location, user);
        LocationAssignment saved = assignmentRepository.save(assignment);

        return LocationUserResult.from(saved, user);
    }

    @Override
    public void removeUser(UUID locationId, UUID userId) {
        findLocation(locationId); // fails with LocationNotFoundException if it does not exist

        LocationAssignment assignment = assignmentRepository.findByUserId(userId)
                .filter(a -> a.getLocationId().equals(locationId))
                .orElseThrow(() -> new UserNotAssignedException(userId, locationId));

        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationUserResult> listLocationUsers(UUID locationId) {
        findLocation(locationId);

        List<LocationAssignment> assignments = assignmentRepository.findAllByLocationId(locationId);
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = assignments.stream().map(LocationAssignment::getUserId).toList();
        Map<UUID, LocationUser> usersById = userDirectoryPort.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(LocationUser::userId, Function.identity()));

        // Assignments whose user no longer exists in IAM are skipped.
        return assignments.stream()
                .filter(a -> usersById.containsKey(a.getUserId()))
                .map(a -> LocationUserResult.from(a, usersById.get(a.getUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationResult> findLocationOfUser(UUID userId) {
        return assignmentRepository.findByUserId(userId)
                .flatMap(a -> locationRepository.findById(a.getLocationId()))
                .map(LocationResult::from);
    }

    // ---- helpers ----

    private LocationCode nextFreeCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            LocationCode code = new LocationCode(locationCodeGenerator.nextCode());
            if (!locationRepository.existsByCode(code)) return code;
        }
        throw new IllegalStateException("No free location code after " + MAX_CODE_ATTEMPTS + " attempts");
    }

    private Location findLocation(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException(locationId));
    }
}