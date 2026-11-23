package org.openimmunizationsoftware.cdsi.core.logic;

import org.openimmunizationsoftware.cdsi.core.data.DataModel;
import org.openimmunizationsoftware.cdsi.core.data.ForecastInput;
import org.openimmunizationsoftware.cdsi.core.data.InvalidInputException;
import org.openimmunizationsoftware.cdsi.core.domain.*;

public class GatherNecessaryDataForValidation extends LogicStep {

  public GatherNecessaryDataForValidation(DataModel dataModel) {
    super(LogicStepType.GATHER_NECESSARY_DATA_FOR_VALIDATION, dataModel);
  }

  @Override
  public LogicStep process() throws Exception {

    ForecastInput input = dataModel.getForecastInput();
    if (input == null) {
      throw new InvalidInputException("forecastInput", "forecastInput", null,
          "No ForecastInput has been set on the data model; the caller must adapt its input (web request, FITS test case, FHIR operation, etc.) into a ForecastInput before running the engine");
    }

    Patient patient = new Patient();
    dataModel.setPatient(patient);
    patient.setDateOfBirth(input.getPatientDateOfBirth());
    patient.setGender(input.getPatientSex());
    dataModel.setAssessmentDate(input.getAssessmentDate());

    ImmunizationHistory immunizationHistory = new ImmunizationHistory();
    dataModel.setImmunizationHistory(immunizationHistory);
    patient.getMedicalHistory().setImmunizationHistory(immunizationHistory);
    immunizationHistory.setMedicalHistory(patient.getMedicalHistory());

    int id = 1;
    for (ForecastInput.VaccinationInput v : input.getVaccinationList()) {
      VaccineDoseAdministered vda = new VaccineDoseAdministered();
      vda.setId(id++);
      vda.setPatient(patient);
      vda.setImmunizationHistory(immunizationHistory);
      // PR: record mapping so SatisfyTargetDose can attribute a satisfied/not-satisfied
      // outcome back to the original ForecastInput.VaccinationInput.
      dataModel.getVaccinationInputMap().put(vda, v);
      vda.setDateAdministered(v.getDateAdministered());
      if (v.getDoseCondition() != null) {
        vda.setDoseCondition(v.getDoseCondition());
      }
      String cvxCode = v.getVaccineCvx();
      VaccineType cvx = dataModel.getCvxMap().get(cvxCode);
      if (cvx == null) {
        dataModel.getInvalidVaccineDoseMap().put(vda,
            new InvalidInputException("vaccination", "vaccineCvx", cvxCode,
                "Unrecognized cvx code"));
        continue;
      }
      Vaccine vaccine = new Vaccine();
      vaccine.setVaccineType(cvx);
      vaccine.setManufacturer(v.getVaccineMvx());
      vda.setVaccine(vaccine);
      immunizationHistory.getVaccineDoseAdministeredList().add(vda);
      patient.getReceivesList().add(vda);
    }

    for (ForecastInput.ObservationInput o : input.getObservationList()) {
      String observationCodeValue = o.getObservationCode();
      if (observationCodeValue == null || observationCodeValue.equals("")) {
        continue;
      }
      Observation observation = dataModel.getObservationMap().get(observationCodeValue);
      if (observation == null) {
        dataModel.getInvalidObservationInputMap().put(o,
                new InvalidInputException("observation", "observationCode", observationCodeValue,
                        "Unrecognized observation code"));
        continue;
      }

      PatientObservation patientObservation = new PatientObservation();
      ObservationCode observationCode = new ObservationCode();
      observationCode.setCode(observation.getObservationCode());
      observationCode.setText(observation.getObservationTitle());
      patientObservation.setObservationCode(observationCode);
      patientObservation.setObservationDate(o.getObservationDate());

      patient.getMedicalHistory().getPatientObservationList().add(patientObservation);
    }

    return LogicStepFactory.createLogicStep(LogicStepType.ORGANIZE_IMMUNIZATION_HISTORY, dataModel);
  }

}
