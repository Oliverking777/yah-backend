package tsgp.yah_enterprise.iam.infrastructure.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes an error in the same JSON shape as the shared ApiResponse, for errors that happen inside the security
 * filters, before any controller (and before GlobalExceptionHandler) can run.
 * The JSON is built by hand on purpose, so this class does not depend on a particular Jackson version.
 */
@Component
public class JsonErrorWriter {

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = "{\"success\":false"
                + ",\"status\":" + status.value()
                + ",\"message\":\"" + escape(message) + "\""
                + ",\"path\":\"" + escape(request.getRequestURI()) + "\""
                + ",\"timestamp\":\"" + Instant.now() + "\"}";
        response.getWriter().write(body);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}