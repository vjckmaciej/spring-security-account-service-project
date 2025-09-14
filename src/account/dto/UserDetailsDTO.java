package account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class UserDetailsDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotBlank
    @Email
    @Pattern(regexp = ".*@acme\\.com", message = "Email must end with @acme.com")
    private String email;

    @NotBlank
    private String password;
}
