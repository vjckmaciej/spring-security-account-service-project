package account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    @NotBlank
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String new_password;
}
