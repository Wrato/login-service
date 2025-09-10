package cl.fernando.login_service.exception;

import java.time.Instant;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cl.fernando.login_service.dto.ErrorDetail;
import cl.fernando.login_service.dto.ErrorResponse;


@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
		ErrorResponse error = new ErrorResponse(Collections.singletonList(
				new ErrorDetail(Instant.now().toString(),
						HttpStatus.BAD_REQUEST.value(),
						ex.getMessage()
				)
		));
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
}
