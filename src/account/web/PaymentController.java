package account.web;

import account.domain.User;
import account.dto.PaymentDTO;
import account.mapper.PaymentViewMapper;
import account.service.PaymentService;
import account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentViewMapper viewMapper;
    private final UserService userService;

    @GetMapping(path = "/api/empl/payment")
    public Object payment(Authentication authentication, @RequestParam(required = false) String period) {
        User user = userService.getUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (period != null) {
            var payrollRecord = paymentService.getPaymentByEmailAndPeriod(user.getEmail(), period);
            return payrollRecord
                    .map(p -> (Object) viewMapper.toView(user, p)) // Optional<Object>
                    .orElseGet(Map::of);
        } else {
            var payments = paymentService.getAllPaymentsByEmail(user.getEmail());
            return payments.stream().map(p -> viewMapper.toView(user, p)).toList();
        }
    }

    @PostMapping(path = "/api/acct/payments")
    public ResponseEntity<?> uploadPayroll(@RequestBody List<@Valid PaymentDTO> payments) {
        paymentService.addBulk(payments);
        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    @PutMapping(path = "/api/acct/payments")
    public ResponseEntity<?> updatePayroll(@RequestBody @Valid PaymentDTO payment) {
        paymentService.updatePayment(payment);
        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }
}
