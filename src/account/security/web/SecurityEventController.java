package account.security.web;

import account.security.event.SecurityEvent;
import account.security.event.SecurityEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SecurityEventController {
    private final SecurityEventService securityEventService;

    @GetMapping({"/api/security/events", "/api/security/events/"})
    public List<SecurityEvent> getEvents() {
        return securityEventService.findAllOrdered();
    }
}
