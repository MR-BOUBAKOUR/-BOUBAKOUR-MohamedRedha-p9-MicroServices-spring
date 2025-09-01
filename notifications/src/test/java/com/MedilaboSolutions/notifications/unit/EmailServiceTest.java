//package com.MedilaboSolutions.notifications.unit;
//
//import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
//import com.MedilaboSolutions.notifications.config.EmailProperties;
//import com.MedilaboSolutions.notifications.service.EmailService;
//import com.MedilaboSolutions.notifications.service.MailtrapService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class EmailServiceTest {
//
//    @Mock
//    private EmailProperties emailProperties;
//
//    @Mock
//    private MailtrapService mailtrapService;
//
//    @InjectMocks
//    private EmailService emailService;
//
//    @Test
//    @DisplayName("Should format and delegate email content for HighRiskAssessmentEvent")
//    void shouldFormatAndDelegateEmail_ForHighRiskEvent() {
//        // Given
//        when(emailProperties.getSender()).thenReturn("sender@test.com");
//        when(emailProperties.getRecipient()).thenReturn("recipient@test.com");
//
//        HighRiskAssessmentEvent event = new HighRiskAssessmentEvent(
//                1L, "John", "Doe", "Early onset"
//        );
//
//        // When
////        emailService.sendHighRiskEmail(event);
//
//        // Then
////        verify(mailtrapEmailService).sendEmail(
////                eq("sender@test.com"),
////                eq("recipient@test.com"),
////                eq("High Risk Alert - Patient Doe"),
////                eq("Patient John Doe has been assessed as 'Early onset'. Please take necessary action.")
////        );
//    }
//}