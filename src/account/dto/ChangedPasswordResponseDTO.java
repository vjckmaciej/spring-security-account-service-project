package account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangedPasswordResponseDTO {
    private String email;
    private String status;
}
