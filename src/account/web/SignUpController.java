package account.web;

import account.domain.User;
import account.dto.UserDetailsDTO;
import account.dto.UserDetailsResponseDTO;
import account.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
public class SignUpController {
    private final UserService userService;

    @Autowired
    public SignUpController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/api/auth/signup")
    public UserDetailsResponseDTO signup(@RequestBody @Valid UserDetailsDTO userDetailsDTO) {
        return userService.register(userDetailsDTO);
    }

    @GetMapping(path = "/api/empl/payment")
    public UserDetailsResponseDTO payment(Authentication authentication) {
        String email = authentication.getName();

        Optional<User> userOptional = userService.getUserByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            throw new RuntimeException("User not found");
        }

        return new UserDetailsResponseDTO(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail().toLowerCase()
        );
    }
}
