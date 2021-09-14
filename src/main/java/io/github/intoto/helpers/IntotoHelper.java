package io.github.intoto.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.dsse.models.IntotoEnvelope;
import io.github.dsse.models.Signature;
import io.github.dsse.models.Signer;
import io.github.intoto.exceptions.InvalidModelException;
import io.github.intoto.models.Statement;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;
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
   * Creates a JSON String representation of a DSSE Envelope.
   *
   * @param statement the Statement to add to the envelope
   * @param signer the Signer that will be used to sign the payloads.
   * @param prettyPrint if true it will pretty print the final Envelope JSON representation
   * @return a JSON representation for the envelope.
   * @throws InvalidModelException
   * @throws JsonProcessingException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   * @throws InvalidKeyException
   */
  public static String produceIntotoEnvelopeAsJson(
      Statement statement, Signer signer, boolean prettyPrint)
      throws InvalidModelException, JsonProcessingException, NoSuchAlgorithmException,
          SignatureException, InvalidKeyException {
    // Get the Base64 encoded Statement to use as the payload
    String jsonStatement = validateAndTransformToJson(statement, false);
    String base64EncodedStatement = Base64.getEncoder().encodeToString(jsonStatement.getBytes());

    IntotoEnvelope envelope = new IntotoEnvelope();
    // Create the signed payload with the DSSEv1 format and sign it!
    byte[] signedDsseV1Payload =
        signer.sign(
            createPreAuthenticationEncoding(envelope.getPayloadType(), base64EncodedStatement));

    Signature signature = new Signature();
    signature.setKeyId(signer.getKeyId());
    // The sig contains the base64 encoded version of the signedDsseV1Payload
    signature.setSig(Base64.getEncoder().encodeToString(signedDsseV1Payload));
    // Let's complete the envelope
    envelope.setPayload(base64EncodedStatement);
    envelope.setSignatures(List.of(signature));
    if (prettyPrint) {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(envelope);
    }
    return objectMapper.writeValueAsString(envelope);
  }

  /**
   * Produces an {@link IntotoEnvelope} and signs the payload with the given Signer. Note: There is
   * another convenience method that returns the serialized JSON representation for the envelope
   *
   * @param statement the Statement to add to the envelope
   * @param signer the Signer that will be used to sign the payloads.
   * @return
   * @throws InvalidModelException
   * @throws JsonProcessingException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   * @throws InvalidKeyException
   */
  public static IntotoEnvelope produceIntotoEnvelope(Statement statement, Signer signer)
      throws InvalidModelException, JsonProcessingException, NoSuchAlgorithmException,
          SignatureException, InvalidKeyException {
    // Get the Base64 encoded Statement to use as the payload
    String jsonStatement = validateAndTransformToJson(statement, false);
    String base64EncodedStatement = Base64.getEncoder().encodeToString(jsonStatement.getBytes());

    IntotoEnvelope envelope = new IntotoEnvelope();
    // Create the signed payload with the DSSEv1 format and sign it!
    byte[] signedDsseV1Payload =
        signer.sign(
            createPreAuthenticationEncoding(envelope.getPayloadType(), base64EncodedStatement));
    Signature signature = new Signature();
    signature.setKeyId(signer.getKeyId());
    // The sig contains the base64 encoded version of the signedDsseV1Payload
    signature.setSig(Base64.getEncoder().encodeToString(signedDsseV1Payload));
    // Let's complete the envelope
    envelope.setPayload(base64EncodedStatement);
    envelope.setSignatures(List.of(signature));
    return envelope;
  }

  /**
   * Generates the Pre-Authentication Encoding
   *<pre>
   * "DSSEv1" + SP + LEN(type) + SP + type + SP + LEN(body) + SP + body
   *
   * where:
   * + = concatenation
   * SP = ASCII space [0x20]
   * "DSSEv1" = ASCII [0x44, 0x53, 0x53, 0x45, 0x76, 0x31]
   * LEN(s) = ASCII decimal encoding of the byte length of s, with no leading zeros
   *<pre/>
   * @param payloadType the type of payload. Fixed for in-toto Envelopes
   * @param payload the base64 encoded Statement in JSON
   * @return Strin
   */
  public static String createPreAuthenticationEncoding(String payloadType, String payload) {
    return String.format(
        "DSSEv1 %d %s %d %s", payloadType.length(), payloadType, payload.length(), payload);
  }

  /**
   * Validates a {@link Statement} and transforms it to its JSON representation.
   *
   * @param statement the statement thet needs to be validated and tested.
   * @param prettyPrint indicates if you want the output result to be formatted for human reading.
   * @return the String with the JSON representation of the Statement.
   * @throws JsonProcessingException thrown when there is a problem serializing the Statement into
   *     JSON
   * @throws InvalidModelException thrown when there are problems with the statement.
   */
  public static String validateAndTransformToJson(Statement statement, boolean prettyPrint)
      throws JsonProcessingException, InvalidModelException {

    Set<ConstraintViolation<Statement>> results = validator.validate(statement);

    if (results.isEmpty()) {
      objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      if (prettyPrint) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statement);
      }
      return objectMapper.writeValueAsString(statement);
    } else {
      String errorMessage =
          results.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(",/n"));
      throw new InvalidModelException(errorMessage);
    }
  }
}
