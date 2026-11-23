package org.openimmunizationsoftware.cdsi.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Mirrors the style of {@code GatherNecessaryDataTest}: verifies the traceability
 * contract of {@link InvalidInputException} so that projects depending on
 * cdsi-engine can pinpoint exactly which value, on which attribute, of which
 * part of the input was rejected, instead of relying on a bare message string.
 */
public class InvalidInputExceptionTest {

  @Test
  public void fieldsAreRetainedAndMessageNamesThem() {
    InvalidInputException ex = new InvalidInputException(
        "vaccination", "vaccineCvx", "999", "Unrecognized cvx code");

    assertEquals("vaccination", ex.getInputPart());
    assertEquals("vaccineCvx", ex.getAttributeName());
    assertEquals("999", ex.getInvalidValue());
    assertNotNull(ex.getMessage());
    assertTrue("message should name the input part", ex.getMessage().contains("vaccination"));
    assertTrue("message should name the attribute", ex.getMessage().contains("vaccineCvx"));
    assertTrue("message should name the value", ex.getMessage().contains("999"));
    assertTrue("message should carry the detail", ex.getMessage().contains("Unrecognized cvx code"));
  }

  @Test
  public void nullValueIsRenderedInMessage() {
    InvalidInputException ex = new InvalidInputException(
        "forecastInput", "forecastInput", null, "No ForecastInput has been set");

    assertEquals(null, ex.getInvalidValue());
    assertTrue(ex.getMessage().contains("null"));
  }

  @Test
  public void causeIsRetainedWhenProvided() {
    Throwable cause = new IllegalStateException("boom");
    InvalidInputException ex = new InvalidInputException(
        "observation", "observationCode", "X", "Unrecognized observation code", cause);

    assertEquals(cause, ex.getCause());
    assertEquals("observation", ex.getInputPart());
    assertEquals("observationCode", ex.getAttributeName());
    assertEquals("X", ex.getInvalidValue());
  }
}
