package account.config;

import account.security.event.SecurityEventAction;
import account.security.event.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityEventService securityEventService;

    @Override
    public void handle(HttpServletRequest servletRequest, HttpServletResponse servletResponse, AccessDeniedException accessDeniedException) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String subject = (auth == null) ? "Anonymous" : auth.getName();

        securityEventService.log(SecurityEventAction.ACCESS_DENIED, subject, servletRequest.getRequestURI(), servletRequest.getRequestURI());

        servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        String body = """
        {"timestamp":"%s","status":403,"error":"Forbidden","message":"Access Denied!","path":"%s"}
        """.formatted(LocalDateTime.now(), servletRequest.getRequestURI());
        servletResponse.getWriter().write(body);
    }
}
