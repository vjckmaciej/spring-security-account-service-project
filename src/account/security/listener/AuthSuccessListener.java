package account.security.listener;

import account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // reset failed attempts to 0
        String username = event.getAuthentication().getName().toLowerCase();
        userRepository.findByEmailIgnoreCase(username).ifPresent(user -> {
            if (user.getFailedAttempts() != 0) {
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        });
    }
}
