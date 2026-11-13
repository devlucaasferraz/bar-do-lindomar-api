package dev.lucasferraz.bardolindomarapi.exceptions;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErroCampo(String field, String message) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var problem = problema(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos.", request);
        List<ErroCampo> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErroCampo(error.getField(), error.getDefaultMessage())).toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            jakarta.validation.ConstraintViolationException.class})
    public ProblemDetail requisicaoInvalida(Exception ex, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Dados ou parâmetros inválidos.", request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflito(DataIntegrityViolationException ex, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, "A operação conflita com os dados existentes.", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse error) {
            var status = HttpStatus.valueOf(error.getStatusCode().value());
            return problema(status, status.is4xxClientError()
                    ? "Requisição inválida ou recurso indisponível." : "Erro interno do servidor.", request);
        }
        log.error("Erro inesperado ao processar a requisição", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.", request);
    }

    public static ProblemDetail problema(HttpStatus status, String detail, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
