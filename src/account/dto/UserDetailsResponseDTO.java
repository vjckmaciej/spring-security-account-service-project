package account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDTO {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private List<String> roles;
}
