package account.service;

import account.domain.Payment;
import account.dto.PaymentDTO;
import account.repository.PaymentRepository;
import account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private static final String PERIOD_REGEX = "^(0[1-9]|1[0-2])-\\d{4}$";
    private static final DateTimeFormatter MM_YYYY = DateTimeFormatter.ofPattern("MM-uuuu");

    @Transactional
    public void addBulk(List<PaymentDTO> paymentDTOs) {
        for (PaymentDTO paymentDTO : paymentDTOs) {
            var email = paymentDTO.getEmployee().toLowerCase();

            validatePeriodOrThrow(paymentDTO.getPeriod());

            if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
                throw new IllegalArgumentException("Employee not found");
            }

            if (paymentRepository.existsByEmployeeIgnoreCaseAndPeriod(email, paymentDTO.getPeriod())) {
                throw new IllegalArgumentException("Duplicate employee-period");
            }

            if (paymentDTO.getSalary() < 0) {
                throw new IllegalArgumentException("Salary cannot be negative!");
            }
        }

        for (PaymentDTO paymentDTO : paymentDTOs) {
            var payment = new Payment();
            payment.setEmployee(paymentDTO.getEmployee().toLowerCase());
            payment.setPeriod(paymentDTO.getPeriod());
            payment.setSalary(paymentDTO.getSalary());
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void updatePayment(PaymentDTO paymentDTO) {
        var email = paymentDTO.getEmployee().toLowerCase();
        validatePeriodOrThrow(paymentDTO.getPeriod());

        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            throw new IllegalArgumentException("Employee not found");
        }

        var payment = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(email, paymentDTO.getPeriod())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setSalary(paymentDTO.getSalary());
        paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByEmailAndPeriod(String email, String period) {
        validatePeriodOrThrow(period);
        return paymentRepository.findByEmployeeIgnoreCaseAndPeriod(email.toLowerCase(), period);    }

    public List<Payment> getAllPaymentsByEmail(String email) {
        var list = paymentRepository.findAllByEmployeeIgnoreCase(email.toLowerCase());
        list.sort(byPeriodDesc());
        return list;    }

    private YearMonth toYearMonth(String period) {
        return YearMonth.parse(period, MM_YYYY);
    }

    private Comparator<Payment> byPeriodDesc() {
        return comparing((Payment p) -> toYearMonth(p.getPeriod())).reversed();
    }

    private void validatePeriodOrThrow(String period) {
        if (period == null || !period.matches(PERIOD_REGEX)) {
            throw new IllegalArgumentException("Wrong date!");
        }
    }


}
