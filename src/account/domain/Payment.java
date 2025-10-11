package account.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAYMENTS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee", "period"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String employee;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(nullable = false)
    private Long salary;
}
