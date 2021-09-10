package io.github.intoto.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.intoto.exceptions.InvalidModelException;
import io.github.intoto.models.Statement;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Helper class for the intoto-java implementation. This class provides with helper methods to
 * validate and transform {@link Statement} into their JSON representations.
 */
public class IntotoHelper {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * Validates a {@link Statement} and transforms it to its JSON representation.
   *
   * @param statement the statement thet needs to be validated and tested.
   * @return the String with the JSON representation of the Statement.
   * @throws JsonProcessingException thrown when there is a problem serializing the Statement into
   *     JSON
   * @throws InvalidModelException thrown when there are problems with the statement.
   */
  public static String validateAndTransformToJson(Statement statement)
      throws JsonProcessingException, InvalidModelException {

    Set<ConstraintViolation<Statement>> results = validator.validate(statement);

    if (results.isEmpty()) {
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      return objectMapper.writeValueAsString(statement);
    } else {
      String errorMessage =
          results.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(",/n"));
      throw new InvalidModelException(errorMessage);
    }
  }
}
