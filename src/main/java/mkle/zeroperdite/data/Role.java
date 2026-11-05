package mkle.zeroperdite.data;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN, API_ROOT;

    @Override
    public @Nullable String getAuthority() {
        return this.name();
    }
}
