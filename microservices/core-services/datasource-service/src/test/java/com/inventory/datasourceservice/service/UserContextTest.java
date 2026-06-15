package com.inventory.datasourceservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserContext Unit Tests")
class UserContextTest {

    private UserContext userContext;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private ServletRequestAttributes mockAttributes;

    @BeforeEach
    void setUp() {
        userContext = new UserContext();
    }

    @Nested
    @DisplayName("getCurrentUserId() method tests")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("should return user id from header")
        void shouldReturnUserIdFromHeader() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-User-Id")).thenReturn("user-123");

                String userId = userContext.getCurrentUserId();

                assertEquals("user-123", userId);
            }
        }

        @Test
        @DisplayName("should return default when no request attributes")
        void shouldReturnDefaultWhenNoRequestAttributes() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                String userId = userContext.getCurrentUserId();

                assertEquals("system", userId);
            }
        }

        @Test
        @DisplayName("should return default when header is empty")
        void shouldReturnDefaultWhenHeaderIsEmpty() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-User-Id")).thenReturn("");

                String userId = userContext.getCurrentUserId();

                assertEquals("system", userId);
            }
        }

        @Test
        @DisplayName("should return default when header is null")
        void shouldReturnDefaultWhenHeaderIsNull() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-User-Id")).thenReturn(null);

                String userId = userContext.getCurrentUserId();

                assertEquals("system", userId);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUsername() method tests")
    class GetCurrentUsernameTests {

        @Test
        @DisplayName("should return username from header")
        void shouldReturnUsernameFromHeader() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Username")).thenReturn("testuser");

                String username = userContext.getCurrentUsername();

                assertEquals("testuser", username);
            }
        }

        @Test
        @DisplayName("should return default when no request attributes")
        void shouldReturnDefaultWhenNoRequestAttributes() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                String username = userContext.getCurrentUsername();

                assertEquals("system", username);
            }
        }
    }

    @Nested
    @DisplayName("getClientIp() method tests")
    class GetClientIpTests {

        @Test
        @DisplayName("should return IP from X-Forwarded-For header")
        void shouldReturnIpFromXForwardedForHeader() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

                String ip = userContext.getClientIp();

                assertEquals("192.168.1.100", ip);
            }
        }

        @Test
        @DisplayName("should return first IP from comma-separated X-Forwarded-For")
        void shouldReturnFirstIpFromCommaSeparatedXForwardedFor() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");

                String ip = userContext.getClientIp();

                assertEquals("192.168.1.100", ip);
            }
        }

        @Test
        @DisplayName("should return IP from X-Real-IP header when X-Forwarded-For is empty")
        void shouldReturnIpFromXRealIpHeaderWhenXForwardedForIsEmpty() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
                when(mockRequest.getHeader("X-Real-IP")).thenReturn("192.168.1.101");

                String ip = userContext.getClientIp();

                assertEquals("192.168.1.101", ip);
            }
        }

        @Test
        @DisplayName("should return remote address when headers are empty")
        void shouldReturnRemoteAddressWhenHeadersAreEmpty() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
                when(mockRequest.getHeader("X-Real-IP")).thenReturn(null);
                when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

                String ip = userContext.getClientIp();

                assertEquals("127.0.0.1", ip);
            }
        }

        @Test
        @DisplayName("should return unknown when no request attributes")
        void shouldReturnUnknownWhenNoRequestAttributes() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                String ip = userContext.getClientIp();

                assertEquals("unknown", ip);
            }
        }

        @Test
        @DisplayName("should skip unknown value in X-Forwarded-For")
        void shouldSkipUnknownValueInXForwardedFor() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
                when(mockRequest.getHeader("X-Real-IP")).thenReturn("192.168.1.102");

                String ip = userContext.getClientIp();

                assertEquals("192.168.1.102", ip);
            }
        }
    }

    @Nested
    @DisplayName("getUserAgent() method tests")
    class GetUserAgentTests {

        @Test
        @DisplayName("should return user agent from header")
        void shouldReturnUserAgentFromHeader() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

                String userAgent = userContext.getUserAgent();

                assertEquals("Mozilla/5.0", userAgent);
            }
        }

        @Test
        @DisplayName("should return null when no request attributes")
        void shouldReturnNullWhenNoRequestAttributes() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                String userAgent = userContext.getUserAgent();

                assertNull(userAgent);
            }
        }
    }
}
