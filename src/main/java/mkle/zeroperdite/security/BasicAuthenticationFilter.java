package mkle.zeroperdite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mkle.zeroperdite.data.Role;
import mkle.zeroperdite.data.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

public class BasicAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Basic ";
    private static final String ROOT_USERNAME = "root";
    private static final String ROOT_PASSWORD = "pVjuNTTW6rns9X";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = null;
        try {
            authentication = getAuthenticationFromBasicAuthHeader(request);
        } catch (Exception ignored) {
        }
        if (authentication != null) SecurityContextHolder.getContext().setAuthentication(authentication);

        //If everything is fine chain will continue
        filterChain.doFilter(request, response);
    }

    private Authentication getAuthenticationFromBasicAuthHeader(HttpServletRequest request) {
        String encodedBasic = request.getHeader(HEADER);
        if (encodedBasic == null) return null;
        encodedBasic = encodedBasic.replaceFirst(PREFIX, "");

        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(encodedBasic));

        String[] credentials = decoded.split(":");

        if (ROOT_USERNAME.equals(credentials[0]) && ROOT_PASSWORD.equals(credentials[1])) {
            User user = new User()
                    .setName("root")
                    .setUsername("root")
                    .setRoles(Set.of(Role.API_ROOT));
            return new UsernamePasswordAuthenticationToken(user, null, user.getRoles());
        }
        return null;
    }
}