package com.vn.caro_game.mappers;

import com.vn.caro_game.dtos.request.UserCreation;
import com.vn.caro_game.dtos.response.UserResponse;
import com.vn.caro_game.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreation userCreation);
    
    UserResponse toResponse(User user);
}
