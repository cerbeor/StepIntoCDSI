package org.openimmunizationsoftware.cdsi.core.data;

/**
 * Structured exception used to report that a value supplied as engine input is
 * invalid. Carries enough context for a caller (including projects that depend
 * on cdsi-engine) to pinpoint exactly which value, on which attribute, of which
 * part of the input, was rejected - instead of a bare message string.
 *
 * <p>Instances are also retained by {@link DataModel#getInvalidVaccineDoseMap()}
 * so that a run can continue past bad doses while still exposing the full
 * traceability information afterwards.
 */
public class InvalidInputException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  /** Which part of the input the offending value came from (e.g. "vaccination", "observation"). */
  private final String inputPart;

  /** Which attribute of that part was invalid (e.g. "vaccineCvx", "observationCode"). */
  private final String attributeName;

  /** The offending value as supplied by the caller. */
  private final Object invalidValue;

  public InvalidInputException(String inputPart, String attributeName, Object invalidValue, String message) {
    super(buildMessage(inputPart, attributeName, invalidValue, message));
    this.inputPart = inputPart;
    this.attributeName = attributeName;
    this.invalidValue = invalidValue;
  }

  public InvalidInputException(String inputPart, String attributeName, Object invalidValue, String message, Throwable cause) {
    super(buildMessage(inputPart, attributeName, invalidValue, message), cause);
    this.inputPart = inputPart;
    this.attributeName = attributeName;
    this.invalidValue = invalidValue;
  }

  private static String buildMessage(String inputPart, String attributeName, Object invalidValue, String message) {
    StringBuilder sb = new StringBuilder();
    if (inputPart != null && inputPart.length() > 0) {
      sb.append("[").append(inputPart).append("] ");
    }
    if (attributeName != null && attributeName.length() > 0) {
      sb.append(attributeName).append(" = ").append(quote(invalidValue)).append(": ");
    }
    if (message != null) {
      sb.append(message);
    }
    return sb.toString();
  }

  private static String quote(Object value) {
    if (value == null) {
      return "null";
    }
    return "'" + String.valueOf(value) + "'";
  }

  public String getInputPart() {
    return inputPart;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Object getInvalidValue() {
    return invalidValue;
  }
}
