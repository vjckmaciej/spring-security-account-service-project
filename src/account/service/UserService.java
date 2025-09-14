package account.service;

import account.domain.User;
import account.dto.UserDetailsDTO;
import account.dto.UserDetailsResponseDTO;
import account.exceptions.UserExistException;
import account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetailsResponseDTO register(UserDetailsDTO userDetailsDTO) throws UserExistException {
        if  (userRepository.existsByEmailIgnoreCase(userDetailsDTO.getEmail())) {
            log.info("User with email {} already exists", userDetailsDTO.getEmail());
            throw new UserExistException();
        } else {
            String encodedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
            User user = User.builder()
                    .name(userDetailsDTO.getName())
                    .lastname(userDetailsDTO.getLastname())
                    .email(userDetailsDTO.getEmail())
                    .password(encodedPassword)
                    .build();
            User savedUSer = userRepository.save(user);
            log.info("User {} has been registered successfully", userDetailsDTO.getName());
            return new UserDetailsResponseDTO(savedUSer.getId(), savedUSer.getName(), savedUSer.getLastname(), savedUSer.getEmail());
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
