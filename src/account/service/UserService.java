package account.service;

import account.config.Role;
import account.domain.User;
import account.dto.ChangeRoleRequestDTO;
import account.dto.ChangedPasswordResponseDTO;
import account.dto.UserDetailsDTO;
import account.dto.UserDetailsResponseDTO;
import account.exceptions.BreachedPasswordException;
import account.exceptions.DuplicatePasswordException;
import account.exceptions.UserExistException;
import account.mapper.UserMapper;
import account.repository.UserRepository;
import account.utils.RoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
        User savedUSer = userRepository.save(user);
        log.info("User {} has been registered successfully", userDetailsDTO.getName());
        return new UserDetailsResponseDTO(
                savedUSer.getId(),
                savedUSer.getName(),
                savedUSer.getLastname(),
                savedUSer.getEmail(),
                savedUSer.getRoles()
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
        String op = changeRoleRequestDTO.getOperation();
        if (op == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad operation!");
        }

        // copy of role due to unmodifiable set
        Set<Role> roles = new HashSet<>(user.getRoles());

        boolean userHasAdminGroup    = RoleUtils.isAdminGroup(roles);
        boolean userHasBusinessGroup = RoleUtils.isBusinessGroup(roles);
        boolean targetIsAdmin        = RoleUtils.isAdminGroup(targetRole);
        boolean targetIsBusiness     = RoleUtils.isBusinessGroup(targetRole);

        if ("GRANT".equalsIgnoreCase(op)) {
            // check if its a mix of admin and bussiness group (should be impossible)
            if ((targetIsAdmin && userHasBusinessGroup) || (targetIsBusiness && userHasAdminGroup)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!"
                );
            }
            roles.add(targetRole);
            user.setRoles(roles);
            return userRepository.save(user);
        }

        if ("REMOVE".equalsIgnoreCase(op)) {
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
            return userRepository.save(user);
        }

        // unknown operation
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad operation!");
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
