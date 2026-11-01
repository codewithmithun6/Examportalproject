package com.exam.service.impl;

import com.exam.exam.QuizAttempt;
import com.exam.service.QuizResultNotificationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class QuizResultNotificationServiceImpl implements QuizResultNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(QuizResultNotificationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public QuizResultNotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendResult(QuizAttempt attempt) {
        if (attempt == null || attempt.getEmail() == null || attempt.getEmail().trim().isEmpty()) {
            logger.warn("Skipping quiz result email because the attempt has no recipient email");
            return;
        }
        if (mailUsername == null || mailUsername.trim().isEmpty()) {
            logger.warn("Skipping quiz result email for attempt {} because SMTP is not configured", attempt.getAttemptId());
            return;
        }

        try {
            byte[] pdf = createResultPdf(attempt);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailUsername);
            helper.setTo(attempt.getEmail());
            helper.setSubject("Quiz result: " + safe(attempt.getQuizTitle()));
            helper.setText(createEmailBody(attempt));
            helper.addAttachment(
                    "quiz-result-" + attempt.getAttemptId() + ".pdf",
                    new ByteArrayResource(pdf),
                    "application/pdf");
            mailSender.send(message);
        } catch (IOException | MessagingException | MailException ex) {
            logger.error("Unable to send result email for attempt {}", attempt.getAttemptId(), ex);
        } catch (RuntimeException ex) {
            logger.error("Unexpected error while sending result email for attempt {}", attempt.getAttemptId(), ex);
        }
    }

    private byte[] createResultPdf(QuizAttempt attempt) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                text(content, PDType1Font.HELVETICA_BOLD, 20, 52, 744, "EXAM PORTAL - QUIZ RESULT");
                line(content, 52, 726, 543, 726);

                text(content, PDType1Font.HELVETICA_BOLD, 12, 52, 699, "LEARNER DETAILS");
                text(content, PDType1Font.HELVETICA, 11, 52, 678, "Name: " + safe(attempt.getLearnerName()));
                text(content, PDType1Font.HELVETICA, 11, 52, 660, "Username: " + safe(attempt.getUserName()));
                text(content, PDType1Font.HELVETICA, 11, 52, 642, "Email: " + safe(attempt.getEmail()));

                text(content, PDType1Font.HELVETICA_BOLD, 12, 52, 606, "ASSESSMENT");
                text(content, PDType1Font.HELVETICA, 11, 52, 585, "Subject: " + safe(attempt.getSubjectName()));
                text(content, PDType1Font.HELVETICA, 11, 52, 567, "Quiz: " + safe(attempt.getQuizTitle()));
                text(content, PDType1Font.HELVETICA, 11, 52, 549, "Completed: " + (attempt.getCompletedAt() == null ? "Not available" : attempt.getCompletedAt().format(DATE_FORMAT)));
                text(content, PDType1Font.HELVETICA, 11, 52, 531, "Attempt reference: " + attempt.getAttemptId());

                text(content, PDType1Font.HELVETICA_BOLD, 12, 52, 493, "RESULT SUMMARY");
                line(content, 52, 480, 543, 480);
                text(content, PDType1Font.HELVETICA_BOLD, 13, 64, 455,
                        "Marks earned: " + number(attempt.getMarksGot()) + " / " + number(attempt.getTotalMarks()));
                text(content, PDType1Font.HELVETICA, 11, 64, 427, "Percentage: " + number(attempt.getPercentage()) + "%");
                text(content, PDType1Font.HELVETICA, 11, 64, 405, "Total questions: " + attempt.getTotalQuestions());
                text(content, PDType1Font.HELVETICA, 11, 64, 383, "Correct answers: " + attempt.getCorrectAnswers());
                text(content, PDType1Font.HELVETICA, 11, 64, 361, "Questions attempted: " + attempt.getAttemptedQuestions());
                text(content, PDType1Font.HELVETICA, 11, 64, 339, "Unattempted questions: " + attempt.getUnattemptedQuestions());
                text(content, PDType1Font.HELVETICA, 11, 64, 317, "Wrong answers: " + attempt.getWrongAnswers());

                line(content, 52, 284, 543, 284);
                text(content, PDType1Font.HELVETICA_OBLIQUE, 9, 52, 265,
                        "This report was generated by Exam Portal from the saved quiz attempt.");
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private String createEmailBody(QuizAttempt attempt) {
        return "Hello " + safe(attempt.getLearnerName()) + ",\n\n"
                + "Your quiz submission has been recorded. Your result is attached as a PDF.\n\n"
                + "Quiz: " + safe(attempt.getQuizTitle()) + "\n"
                + "Subject: " + safe(attempt.getSubjectName()) + "\n"
                + "Marks: " + number(attempt.getMarksGot()) + " / " + number(attempt.getTotalMarks()) + "\n"
                + "Percentage: " + number(attempt.getPercentage()) + "%\n"
                + "Attempt reference: " + attempt.getAttemptId() + "\n\n"
                + "Regards,\nExam Portal";
    }

    private void text(PDPageContentStream content, PDType1Font font, int size, float x, float y, String value) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(pdfSafe(value));
        content.endText();
    }

    private void line(PDPageContentStream content, float startX, float y, float endX, float endY) throws IOException {
        content.moveTo(startX, y);
        content.lineTo(endX, endY);
        content.stroke();
    }

    private String pdfSafe(String value) {
        StringBuilder safeText = new StringBuilder();
        for (char character : safe(value).toCharArray()) {
            safeText.append(character >= 32 && character <= 255 ? character : '?');
        }
        return safeText.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}