package account.security.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityEventService {
    private final  SecurityEventRepository securityEventRepository;

    public void log(SecurityEventAction action, String subject, String object, String path) {
        SecurityEvent securityEvent = SecurityEvent.builder()
                .date(LocalDateTime.now())
                .action(action)
                .subject((subject == null || subject.isBlank() ? "Anonymous" : subject))
                .object(object == null ? "" : object)
                .path(path == null ? "" : path)
                .build();

        securityEventRepository.save(securityEvent);
    }

    public List<SecurityEvent> findAllOrdered() {
        return securityEventRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
