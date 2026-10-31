package com.exam.service;

import com.exam.exam.Question;
import com.exam.exam.Quiz;
import com.exam.repository.QuestionRepository;
import com.exam.repository.QuizRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class QuestionImportService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_QUESTIONS = 500;
    private static final List<String> EXPECTED_HEADERS = Arrays.asList(
            "question", "optiona", "optionb", "optionc", "optiond", "correctanswer");

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuestionImportService(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public int importQuestions(Long quizId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a question file to upload");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("The upload must be 5 MB or smaller");
        }

        String filename = file.getOriginalFilename();
        String extension = filename == null || !filename.contains(".")
                ? "" : filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        List<List<String>> rows;
        try (InputStream input = file.getInputStream()) {
            if ("xlsx".equals(extension)) {
                rows = readExcel(input);
            } else if ("csv".equals(extension)) {
                rows = readDelimited(input, ',');
            } else if ("txt".equals(extension)) {
                rows = readDelimited(input, '\t');
            } else {
                throw new IllegalArgumentException("Upload an .xlsx, .csv, or tab-delimited .txt file");
            }
        }

        validateHeaders(rows);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz was not found"));

        List<Question> questions = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (isBlankRow(row)) {
                continue;
            }
            int rowNumber = index + 1;
            if (questions.size() >= MAX_QUESTIONS) {
                throw new IllegalArgumentException("A maximum of " + MAX_QUESTIONS + " questions can be imported at once");
            }
            if (row.size() < EXPECTED_HEADERS.size()) {
                throw new IllegalArgumentException("Row " + rowNumber + " must contain all six columns");
            }

            String questionText = value(row, 0);
            String optionA = value(row, 1);
            String optionB = value(row, 2);
            String optionC = value(row, 3);
            String optionD = value(row, 4);
            String correctLetter = value(row, 5).toUpperCase(Locale.ROOT);
            if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty()
                    || optionC.isEmpty() || optionD.isEmpty()) {
                throw new IllegalArgumentException("Row " + rowNumber + " requires a question and all four options");
            }
            if (!Arrays.asList("A", "B", "C", "D").contains(correctLetter)) {
                throw new IllegalArgumentException("Row " + rowNumber + " Correct Answer must be A, B, C, or D");
            }

            Question question = new Question();
            question.setQuiz(quiz);
            question.setContent(questionText);
            question.setOption1(optionA);
            question.setOption2(optionB);
            question.setOption3(optionC);
            question.setOption4(optionD);
            question.setAnswer(answerFor(correctLetter, optionA, optionB, optionC, optionD));
            questions.add(question);
        }

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("The file contains no question rows");
        }

        questionRepository.saveAll(questions);
        return questions.size();
    }

    private List<List<String>> readExcel(InputStream input) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("The Excel workbook has no worksheets");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<List<String>> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                List<String> values = new ArrayList<>();
                for (int column = 0; column < EXPECTED_HEADERS.size(); column++) {
                    Cell cell = row == null ? null : row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.add(cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                rows.add(values);
            }
            return rows;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to read the Excel file. Check that it is a valid .xlsx workbook");
        }
    }

    private List<List<String>> readDelimited(InputStream input, char delimiter) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setIgnoreEmptyLines(false)
                .build();
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                List<String> values = new ArrayList<>();
                for (String value : record) {
                    values.add(value.trim());
                }
                rows.add(values);
            }
            return rows;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read the delimited file. Check its encoding and format");
        }
    }

    private void validateHeaders(List<List<String>> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("The file is empty");
        }
        List<String> headers = new ArrayList<>();
        for (String header : rows.get(0)) {
            headers.add(normalizeHeader(header));
        }
        if (headers.size() < EXPECTED_HEADERS.size()) {
            throw new IllegalArgumentException("The header must contain: Question, Option A, Option B, Option C, Option D, Correct Answer");
        }
        for (int index = 0; index < EXPECTED_HEADERS.size(); index++) {
            if (!EXPECTED_HEADERS.get(index).equals(headers.get(index))) {
                throw new IllegalArgumentException("Column " + (index + 1) + " must be " + displayHeader(index));
            }
        }
    }

    private String normalizeHeader(String header) {
        return header.replace("\uFEFF", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private String displayHeader(int index) {
        return Arrays.asList("Question", "Option A", "Option B", "Option C", "Option D", "Correct Answer").get(index);
    }

    private String value(List<String> row, int column) {
        return column < row.size() && row.get(column) != null ? row.get(column).trim() : "";
    }

    private boolean isBlankRow(List<String> row) {
        return row.stream().allMatch(value -> value == null || value.trim().isEmpty());
    }

    private String answerFor(String letter, String optionA, String optionB, String optionC, String optionD) {
        switch (letter) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            default: return optionD;
        }
    }
}