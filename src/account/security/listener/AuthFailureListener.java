package account.security.listener;

import account.config.Role;
import account.repository.UserRepository;
import account.security.event.SecurityEventAction;
import account.security.event.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuthFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final SecurityEventService events;
    private final UserRepository userRepository;
    private static final int BRUTE_FORCE_COUNTER_LIMIT = 5;

    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        // logic of handling failed login to prevent 5 brute force attacks. We will also block user after 5th fail
        String username = (String) event.getAuthentication().getPrincipal();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String path = request.getRequestURI();

        userRepository.findByEmailIgnoreCase(username).ifPresentOrElse(user -> {
            // cant block admin
            boolean isAdmin = user.getRoles().contains(Role.ROLE_ADMINISTRATOR);
            if (isAdmin) {
                events.log(SecurityEventAction.LOGIN_FAILED, username, path, path);
                return;
            }

            int currentAttempts = user.getFailedAttempts();
            int updatedAttempts  = currentAttempts + 1;
            user.setFailedAttempts(updatedAttempts);
            userRepository.save(user);

            // log failed login
            events.log(SecurityEventAction.LOGIN_FAILED, username, path, path);

            if (updatedAttempts >= BRUTE_FORCE_COUNTER_LIMIT && !Boolean.TRUE.equals(user.isLocked())) {
                // BRUTE_FORCE and LOCK_USER
                events.log(SecurityEventAction.BRUTE_FORCE, username, path, path);
                user.setLocked(true);
                userRepository.save(user);
                events.log(SecurityEventAction.LOCK_USER, username,
                        "Lock user " + user.getEmail(), path);
            }
        }, () -> {
            // failed login handling
            events.log(SecurityEventAction.LOGIN_FAILED, username, path, path);
        });
    }
}
