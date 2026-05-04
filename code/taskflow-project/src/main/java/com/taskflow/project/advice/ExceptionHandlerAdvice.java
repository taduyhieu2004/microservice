package com.taskflow.project.advice;

import com.taskflow.common.web.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice extends GlobalExceptionHandler {
}
