package io.github.intoto.validators;

import io.github.intoto.models.Subject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Validator that makes sure that Subjects are unique. */
public class UniqueSubjectValidator implements ConstraintValidator<UniqueSubject, List<Subject>> {

  @Override
  public void initialize(UniqueSubject constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(List<Subject> value, ConstraintValidatorContext context) {
    if (Objects.isNull(value)) {
      return true;
    }
    List<String> uniqueSubjects =
        value.stream().map(Subject::getName).distinct().collect(Collectors.toList());
    return uniqueSubjects.size() == value.size();
  }
}
