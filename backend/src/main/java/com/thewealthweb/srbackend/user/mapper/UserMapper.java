package com.thewealthweb.srbackend.user.mapper;

import com.thewealthweb.srbackend.user.dto.UserDTO;
import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // For custom mapping methods

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // Tells MapStruct to make it a Spring component
public interface UserMapper {

    @Mapping(source = "tenant.tenantId", target = "tenantId")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserDTO toDto(User user);

    // If you need to map a list/set of entities
    Set<UserDTO> toDtoSet(Set<User> users);

    // Custom mapping method for converting Set<Role> to Set<String>
    @Named("rolesToNames")
    default Set<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName) // Assuming Role has a getName() method
                .collect(Collectors.toSet());
    }
}