package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired private TestRequestUpdateService testRequestUpdateService;
    @Autowired private TestRequestQueryService testRequestQueryService;
    @Autowired TestRequestFlowService  testRequestFlowService;
    @Autowired private UserLoggedInService userLoggedInService;


    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {
        log.info("Start -- getForConsultations");
        try{
            // Find and return Test request with the status LAB_TEST_COMPLETED for the Doctor to view
            return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
        }catch (Error e){
            log.error("Error -- getForConsultations -- " + e.getMessage());
            throw new AppException("Error while getting Test Requests" + e.getMessage());
        }finally {
            log.info("End -- getForConsultations");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {
        log.info("Start -- getForDoctor");
        try{
            // Get logged in User
            User loggedInUser = userLoggedInService.getLoggedInUser();
            // Find and return the Test Request that are assigned to the current Logged in user
            return testRequestQueryService.findByDoctor(loggedInUser);
        }catch (Error e){
            log.error("Error -- getForDoctor -- " + e.getMessage());
            throw new AppException("Error while getting Test Requests" + e.getMessage());
        }finally {
            log.info("End -- getForDoctor");
        }
    }

    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        log.info("Start -- assignForConsultation");
        try {
            // Get logged in User
            User loggedInUser = userLoggedInService.getLoggedInUser();

            // Assign the test to the current logged in User for consultation
            return testRequestUpdateService.assignForConsultation(id, loggedInUser);
        }catch (AppException e) {
            log.error("Error -- assignForConsultation -- " + e.getMessage());
            throw asBadRequest(e.getMessage());
        }finally {
            log.info("End -- assignForConsultation");
        }
    }

    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {
        log.info("Start -- updateConsultation");
        try {
            // Get logged in User
            User loggedInUser = userLoggedInService.getLoggedInUser();

            // Update Consultation
            return testRequestUpdateService.updateConsultation(id, testResult, loggedInUser);
        } catch (ConstraintViolationException e) {
            log.error("Error -- updateConsultation -- " + e.getMessage());
            throw asConstraintViolation(e);
        }catch (AppException e) {
            log.error("Error -- updateConsultation -- " + e.getMessage());
            throw asBadRequest(e.getMessage());
        }finally {
            log.info("End -- updateConsultation");
        }
    }
}
