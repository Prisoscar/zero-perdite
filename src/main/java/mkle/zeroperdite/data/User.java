package mkle.zeroperdite.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Entity
@Table(name = "_user")
public class User extends AbstractEntityNoVersion {

    @NotNull(message = "username cannot be null")
    @Size(min = 3, max = 50, message = "username should contain between 3 and 50 characters")
    private String username;
    @Size(max = 200, message = "name cannot contain more than 200 characters")
    private String name;
    @JsonIgnore
    @NotNull(message = "password cannot be null")
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @NotNull(message = "roles cannot be null")
    private Set<Role> roles;
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public User setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        return this;
    }
    public Set<Role> getRoles() {
        return roles;
    }

    public User setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public User setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
        return this;
    }

}
