package account.web;

import account.dto.*;
import account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SignUpController {
    private final UserService userService;

    @PostMapping(path = "/api/auth/changepass")
    public ChangedPasswordResponseDTO changePassword(@RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO, Authentication authentication) {
        String newPassword = changePasswordRequestDTO.getNew_password();
        return userService.changePassword(authentication.getName(), newPassword);
    }

    @PostMapping(path = "/api/auth/signup")
    public UserDetailsResponseDTO signup(@RequestBody @Valid UserDetailsDTO userDetailsDTO) {
        return userService.register(userDetailsDTO);
    }
}
