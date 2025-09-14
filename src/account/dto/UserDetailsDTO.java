package account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


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
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String password;
}
