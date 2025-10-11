package account.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentDTO {
    @NotBlank
    @Email
    private String employee;

    @Pattern(regexp = "(0[1-9]|1[0-2])-(19|20)\\d{2}",
            message = "Wrong date!")
    private String period;

    @NotNull
    @PositiveOrZero(message = "Salary must be non negative!")
    private Long salary;
}
