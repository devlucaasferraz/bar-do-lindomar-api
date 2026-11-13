package dev.lucasferraz.bardolindomarapi;

import dev.lucasferraz.bardolindomarapi.exceptions.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void erroInesperadoNaoExpoeDetalhesInternos() {
        var request = new MockHttpServletRequest("GET", "/produtos");
        var problem = new ApiExceptionHandler().inesperado(new RuntimeException("SQL e senha internos"), request);
        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("Erro interno do servidor.");
        assertThat(problem.getInstance().toString()).isEqualTo("/produtos");
    }
}
