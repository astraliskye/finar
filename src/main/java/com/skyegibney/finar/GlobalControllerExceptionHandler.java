package com.skyegibney.finar;

import com.skyegibney.finar.authorization.exceptions.DuplicateEmailException;
import com.skyegibney.finar.authorization.exceptions.DuplicateUsernameException;
import com.skyegibney.finar.authorization.exceptions.UnauthenticatedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
@RestController
public class GlobalControllerExceptionHandler {
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler({
    UnauthenticatedException.class,
    DuplicateEmailException.class,
    DuplicateUsernameException.class
  })
  public ErrorResponse handleUnauthorized(Exception e) {
    return switch (e) {
      case UnauthenticatedException unauthenticated ->
          new ErrorResponse("You are not authenticated.");
      case DuplicateUsernameException duplicateUsername ->
          new ErrorResponse("Username already exists.");
      case DuplicateEmailException duplicateEmail -> new ErrorResponse("Email already exists.");
      default -> new ErrorResponse("Something went wrong.");
    };
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(AccessDeniedException.class)
  public ErrorResponse handleForbidden(Exception e) {
    return switch (e) {
      case AccessDeniedException accessDenied -> new ErrorResponse("You are not authorized.");
      default -> new ErrorResponse("Something went wrong.");
    };
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoResourceFoundException.class)
  public ErrorResponse handleNotFound(Exception e) {
    return switch (e) {
      case NoResourceFoundException noResourceFoundException ->
          new ErrorResponse("Resource not found.");
      default -> new ErrorResponse("Something went wrong.");
    };
  }
}
