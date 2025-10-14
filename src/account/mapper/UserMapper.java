package account.mapper;

import account.domain.User;
import account.dto.UserDetailsResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserDetailsResponseDTO toDto(User user);

    List<UserDetailsResponseDTO> toDtoList(List<User> users);

    // helper method for converting Set<Role> -> List<String>
    default List<String> mapRoles(User user) {
        return user.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
    }
}
