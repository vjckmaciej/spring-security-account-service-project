package account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccessRequestDTO {
    @NotBlank
    private String user; // email

    @NotBlank
    private String operation; // LOCK / UNLOCK
}
