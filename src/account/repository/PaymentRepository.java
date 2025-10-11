package account.repository;

import account.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByEmployeeIgnoreCaseAndPeriod(String employee, String period);
    Optional<Payment> findByEmployeeIgnoreCaseAndPeriod(String employee, String period);
    List<Payment> findAllByEmployeeIgnoreCase(String employee);
}
