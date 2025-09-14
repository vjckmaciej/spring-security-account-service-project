package account.service;

import account.domain.User;
import account.dto.ChangedPasswordResponseDTO;
import account.dto.UserDetailsDTO;
import account.dto.UserDetailsResponseDTO;
import account.exceptions.BreachedPasswordException;
import account.exceptions.DuplicatePasswordException;
import account.exceptions.UserExistException;
import account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Set<String> breachedPasswords = Set.of(
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
            "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetailsResponseDTO register(UserDetailsDTO userDetailsDTO) throws UserExistException {
        if (userRepository.existsByEmailIgnoreCase(userDetailsDTO.getEmail())) {
            log.info("User with email {} already exists", userDetailsDTO.getEmail());
            throw new UserExistException();
        }

        if (breachedPasswords.contains(userDetailsDTO.getPassword())) {
            throw new BreachedPasswordException();
        }

        String encodedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
        User user = User.builder().name(userDetailsDTO.getName()).lastname(userDetailsDTO.getLastname()).email(userDetailsDTO.getEmail()).password(encodedPassword).build();
        User savedUSer = userRepository.save(user);
        log.info("User {} has been registered successfully", userDetailsDTO.getName());
        return new UserDetailsResponseDTO(savedUSer.getId(), savedUSer.getName(), savedUSer.getLastname(), savedUSer.getEmail());

    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
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

        return new ChangedPasswordResponseDTO(user.getEmail().toLowerCase(),"The password has been updated successfully");
    }
}
