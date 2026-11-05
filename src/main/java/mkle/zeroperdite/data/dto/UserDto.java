package mkle.zeroperdite.data.dto;


import mkle.zeroperdite.data.Role;
import mkle.zeroperdite.data.User;

import java.util.Set;

public record UserDto(String id, String username, String name, String password, Set<Role> roles, byte[] profilePicture){

    public User toUser(){
        return new User()
                .<User>setId(id)
                .setUsername(username)
                .setName(name)
                .setHashedPassword(password)
                .setRoles(roles)
                .setProfilePicture(profilePicture);
    }

}
