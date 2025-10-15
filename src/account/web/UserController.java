package account.web;

import account.domain.User;
import account.dto.*;
import account.mapper.UserMapper;
import account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping(path = "/api/auth/changepass")
    public ChangedPasswordResponseDTO changePassword(@RequestBody @Valid ChangePasswordRequestDTO changePasswordRequestDTO, Authentication authentication) {
        String newPassword = changePasswordRequestDTO.getNew_password();
        return userService.changePassword(authentication.getName(), newPassword);
    }

    @PostMapping(path = "/api/auth/signup")
    public UserDetailsResponseDTO signup(@RequestBody @Valid UserDetailsDTO userDetailsDTO) {
        return userService.register(userDetailsDTO);
    }

    @GetMapping({"/api/admin/user", "/api/admin/user/"})
    public List<UserDetailsResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping(path = "/api/admin/user/{email}")
    public UserDeleteResponseDTO deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return new UserDeleteResponseDTO(email, "Deleted successfully!");
    }

    @PutMapping(path = "/api/admin/user/role")
    public UserDetailsResponseDTO changeRole(@RequestBody @Valid ChangeRoleRequestDTO changeRoleRequestDTO) {
        User updatedUser = userService.changeRole(changeRoleRequestDTO);
        return userMapper.toDto(updatedUser);
    }

    @PutMapping(path = "/api/admin/user/access")
    public Map<String, String> changeAccess(@RequestBody @Valid UserAccessRequestDTO userAccessRequestDTO) {
        String message = userService.changeAccess(userAccessRequestDTO);
        return Map.of("status", message);
    }
}
