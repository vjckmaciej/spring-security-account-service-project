package account.security.event;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SECURITY_EVENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecurityEventAction action;

    @Column(nullable = false)
    private String subject; // who did

    @Column(nullable = false)
    private String object;  // did on what

    @Column(nullable = false)
    private String path;    // endpoint URI
}
