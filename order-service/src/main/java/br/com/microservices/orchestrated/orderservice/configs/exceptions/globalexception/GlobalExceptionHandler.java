package br.com.microservices.orchestrated.orderservice.configs.exceptions.globalexception;

import br.com.microservices.orchestrated.orderservice.configs.exceptions.ErrorToSendEvent;
import br.com.microservices.orchestrated.orderservice.configs.exceptions.EventNotFoundException;
import br.com.microservices.orchestrated.orderservice.configs.exceptions.ValidationException;
import br.com.microservices.orchestrated.orderservice.configs.exceptions.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex) {

        log.warn("Endpoint não encontrado: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "ENDPOINT_NOT_FOUND",
                "Endpoint não encontrado",
                ex.getRequestURL()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            WebRequest request) {

        log.error("Erro de validação: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getFieldErrors()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(
            HttpClientErrorException ex,
            WebRequest request) {

        log.debug("Erro ao chamar serviço externo: {} - Status: {}", ex.getStatusCode(), ex.getStatusCode().value());

        String message = "Erro ao processar requisição no serviço remoto";
        String errorCode = "EXTERNAL_SERVICE_ERROR";

        try {
            String responseBody = ex.getResponseBodyAsString();
            log.debug("Resposta do serviço remoto (raw): {}", responseBody);

            if (!responseBody.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);

                log.debug("Mapa da resposta parseado: {}", responseMap);

                // Se a resposta remota contém uma mensagem, usar essa
                if (responseMap.containsKey("message")) {
                    Object messageObj = responseMap.get("message");
                    message = messageObj != null ? String.valueOf(messageObj) : message;
                    log.debug("Mensagem encontrada: {}", message);
                }

                // Se contém um erro, usar como código
                if (responseMap.containsKey("error")) {
                    Object errorObj = responseMap.get("error");
                    errorCode = errorObj != null ? String.valueOf(errorObj) : errorCode;
                    log.debug("Código de erro encontrado: {}", errorCode);
                }
            } else {
                log.debug("Resposta do serviço remoto está vazia");
            }
        } catch (Exception e) {
            // Se não conseguir parsear o JSON, usar mensagem padrão
            log.warn("Não foi possível parsear resposta remota como JSON. Erro: {}", e.getMessage(), e);
        }

        log.info("Retornando erro para cliente - Status: {}, Código: {}, Mensagem: {}",
                ex.getStatusCode().value(), errorCode, message);

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                errorCode,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        log.error("Erro em tempo de execução: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Erro interno do servidor: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Erro interno do servidor. Entre em contato com o administrador.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        log.error("Erro de validação nos argumentos do método: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        var errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Erro de validação nos argumentos do método",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Acesso negado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ErrorToSendEvent.class)
    public ResponseEntity<ErrorResponse> handleErrorToSendEvent(ErrorToSendEvent exception){
        log.error("Erro ao enviar evento para o Kafka", exception);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERROR_TO_SEND_EVENT",
                "Erro ao enviar evento para o Kafka",
                exception.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFoundException(
            EventNotFoundException exception,
            WebRequest request) {
        log.error("Evento não encontrado: {}", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "EVENT_NOT_FOUND",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
