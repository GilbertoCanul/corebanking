package dev.gcanul.corebanking.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTests {

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Test
    @DisplayName("Should return 401 and JSON body on commence")
    void shouldReturn401AndJsonBodyOnCommence() throws IOException, ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(outputStream));
        when(request.getServletPath()).thenReturn("/api/v1/test");

        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String responseContent = outputStream.toString();
        assertThat(responseContent).contains("\"error\":\"Unauthorized\"");
        assertThat(responseContent).contains("\"path\":\"/api/v1/test\"");
    }

    // Helper simple para mockear el output stream del servlet
    private static class DelegatingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream os;
        public DelegatingServletOutputStream(ByteArrayOutputStream os) { this.os = os; }
        @Override public void write(int b) { os.write(b); }
        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
    }
}