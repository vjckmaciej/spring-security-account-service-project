package account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeRoleRequestDTO {
    @NotBlank
    @Email
    private String user;

    @NotBlank
    private String role;

    @NotBlank
    private String operation;
}
