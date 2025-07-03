package Bright.BeSafeProject.exception;

import Bright.BeSafeProject.dto.ErrorStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<ErrorStatusDTO>> handleException(CustomException e){
        ErrorCode errorCode = e.getErrorCode();
        ErrorStatusDTO dto = new ErrorStatusDTO(
                LocalDateTime.now(),
                errorCode.getStatus(),
                errorCode.getErrorMessage()
        );

        return Mono.just(ResponseEntity.status(errorCode.getStatus()).body(dto));
    }
}
