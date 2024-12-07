package com.example.vida.exception;

import lombok.Data;

import java.util.Map;
@Data
public class UpdateUserValidationException extends RuntimeException {
  public UpdateUserValidationException(String message) {
    super(message);
  }

  public UpdateUserValidationException(Map<String, String> updateErrors) {
    super("Validation errors occurred while updating user.");
    this.updateErrors = updateErrors;
  }
  Map<String,String> updateErrors;
}
