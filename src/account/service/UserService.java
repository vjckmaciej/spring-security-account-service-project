package account.service;

import account.config.Role;
import account.domain.User;
import account.dto.*;
import account.exceptions.BreachedPasswordException;
import account.exceptions.DuplicatePasswordException;
import account.exceptions.UserExistException;
import account.mapper.UserMapper;
import account.repository.UserRepository;
import account.security.event.SecurityEventAction;
import account.security.event.SecurityEventService;
import account.utils.RoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final SecurityEventService securityEventService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Set<String> breachedPasswords = Set.of(
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
            "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public UserDetailsResponseDTO register(UserDetailsDTO userDetailsDTO) throws UserExistException {
        if (userRepository.existsByEmailIgnoreCase(userDetailsDTO.getEmail())) {
            log.info("User with email {} already exists", userDetailsDTO.getEmail());
            throw new UserExistException();
        }

        if (breachedPasswords.contains(userDetailsDTO.getPassword())) {
            throw new BreachedPasswordException();
        }

        String encodedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
        User user = User.builder()
                .name(userDetailsDTO.getName())
                .lastname(userDetailsDTO.getLastname())
                .email(userDetailsDTO.getEmail().toLowerCase())
                .password(encodedPassword)
                .roles(Set.of(userRepository.count() == 0 ? Role.ROLE_ADMINISTRATOR : Role.ROLE_USER))
                .build();

        User savedUser = userRepository.save(user);
        securityEventService.log(SecurityEventAction.CREATE_USER, "Anonymous", savedUser.getEmail(), "/api/auth/signup");
        log.info("User {} has been registered successfully", userDetailsDTO.getName());
        return new UserDetailsResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getLastname(),
                savedUser.getEmail(),
                savedUser.getRoles()
                        .stream()
                        .map(Enum::name)
                        .sorted()
                        .toList()
        );
    }

    public ChangedPasswordResponseDTO changePassword(String email, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        if (breachedPasswords.contains(newPassword)) {
            throw new BreachedPasswordException();
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new DuplicatePasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        securityEventService.log(SecurityEventAction.CHANGE_PASSWORD, user.getEmail(), user.getEmail(), "/api/auth/changepass");

        return new ChangedPasswordResponseDTO(user.getEmail().toLowerCase(), "The password has been updated successfully");
    }

    public List<UserDetailsResponseDTO> getAllUsers() {
        var users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return userMapper.toDtoList(users);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
            throw new IllegalArgumentException("Can't remove ADMINISTRATOR role!");
        }

        userRepository.deleteByEmail(email);
        securityEventService.log(SecurityEventAction.DELETE_USER, currentUsername(), user.getEmail(), "/api/admin/user");
    }

    @Transactional
    public User changeRole(ChangeRoleRequestDTO changeRoleRequestDTO) {
        // find user
        User user = userRepository.findByEmailIgnoreCase(changeRoleRequestDTO.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        // map role from request to enum Role (accept both with and without ROLE_ prefix)
        Role targetRole = RoleUtils.fromString(changeRoleRequestDTO.getRole())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        // interpret the operation
        String operation = changeRoleRequestDTO.getOperation();
        if (operation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad operation!");
        }

        // copy of role due to unmodifiable set
        Set<Role> roles = new HashSet<>(user.getRoles());
        boolean userHasAdminGroup    = RoleUtils.isAdminGroup(roles);
        boolean userHasBusinessGroup = RoleUtils.isBusinessGroup(roles);
        boolean targetIsAdmin        = RoleUtils.isAdminGroup(targetRole);
        boolean targetIsBusiness     = RoleUtils.isBusinessGroup(targetRole);

        final String path = "/api/admin/user/role";
        final String rolePlain = targetRole.name().replace("ROLE_", "");
        final String subject = currentUsername(); // who grants/revokes role

        if ("GRANT".equalsIgnoreCase(operation)) {
            // check if its a mix of admin and bussiness group (should be impossible)
            if ((targetIsAdmin && userHasBusinessGroup) || (targetIsBusiness && userHasAdminGroup)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!"
                );
            }
            roles.add(targetRole);
            user.setRoles(roles);
            User saved = userRepository.save(user);
            securityEventService.log(SecurityEventAction.GRANT_ROLE, subject, "Grant role " + rolePlain + " to " + saved.getEmail(), path);

            return saved;
        }

        if ("REMOVE".equalsIgnoreCase(operation)) {
            // validation for REMOVE
            if (!roles.contains(targetRole)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
            }
            if (targetIsAdmin) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }
            if (roles.size() == 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            }

            roles.remove(targetRole);
            user.setRoles(roles);
            User saved = userRepository.save(user);

            securityEventService.log(
                    SecurityEventAction.REMOVE_ROLE,
                    subject,
                    "Remove role " + rolePlain + " from " + saved.getEmail(),
                    path
            );
            return saved;
        }

        // unknown operation
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad operation!");
    }

    @Transactional
    public String changeAccess(UserAccessRequestDTO userAccessRequestDTO) {
        String email = userAccessRequestDTO.getUser().toLowerCase();
        String operation = userAccessRequestDTO.getOperation();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        // prevent from locking admin
        if ("LOCK".equalsIgnoreCase(operation) && user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }

        if ("LOCK".equalsIgnoreCase(operation)) {
            if (!user.isLocked()) {
                user.setLocked(true);
                userRepository.save(user);
            }
            securityEventService.log(SecurityEventAction.LOCK_USER, currentUsername(), "Lock user " + user.getEmail(), "/api/admin/user/access");
            return "User " + user.getEmail() + " locked!";
        }

        if ("UNLOCK".equalsIgnoreCase(operation)) {
            user.setLocked(false);
            user.setFailedAttempts(0);
            securityEventService.log(SecurityEventAction.UNLOCK_USER, currentUsername(), "Unlock user " + user.getEmail(), "/api/admin/user/access");
            return "User " + user.getEmail() + " unlocked!";
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad operation!");
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "Anonymous" : auth.getName();
    }
}
