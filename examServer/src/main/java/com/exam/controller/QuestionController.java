package com.exam.controller;

import com.exam.exam.Question;
import com.exam.exam.Quiz;
import com.exam.service.QuestionService;
import com.exam.service.QuizAttemptService;
import com.exam.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/question")
@CrossOrigin("*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptService quizAttemptService;

    // add question service

    @PostMapping("/")
    public ResponseEntity<Question>  addQuestion(@RequestBody Question question){
        return ResponseEntity.ok(this.questionService.addQuestion(question));
    }

    // update question

    @PutMapping("/")
    public ResponseEntity<Question> updateQuestion(@RequestBody Question question){
        return ResponseEntity.ok(this.questionService.updateQuestion(question));
    }

    // get all question of any quiz

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getQuestionsOfQuiz(@PathVariable("quizId") Long quizId){

//        Quiz quiz = new Quiz();
//        quiz.setqId(quizId);
//        Set<Question> questionsOfQuiz = this.questionService.getQuestionsOfQuiz(quiz);
//        return ResponseEntity.ok(questionsOfQuiz);

        Quiz quiz = this.quizService.getQuiz(quizId);
        Set<Question> questions = quiz.getQuestions();
        List list = new ArrayList<>(questions);

        if(list.size()>Integer.parseInt(quiz.getNumberOfQuestions())){
             list = list.subList(0, Integer.parseInt(quiz.getNumberOfQuestions()+1));

        }


        Collections.shuffle(list);
        return ResponseEntity.ok(list);
    }

    // get all question of any quiz for admin

    @GetMapping("/quiz/all/{quizId}")
    public ResponseEntity<?> getQuestionsOfQuizAdmin(@PathVariable("quizId") Long quizId){

        Quiz quiz = new Quiz();
        quiz.setqId(quizId);
        Set<Question> questionsOfQuiz = this.questionService.getQuestionsOfQuiz(quiz);
        return ResponseEntity.ok(questionsOfQuiz);


//        return ResponseEntity.ok(list);
    }


    // get single question
    @GetMapping("/{quesId}")
    public Question getQuestion(@PathVariable("quesId") Long quesId){
        return this.questionService.getQuestion(quesId);
    }

//    delete question

    @DeleteMapping("/{quesId}")
    public void deleteQuestion(@PathVariable("quesId") Long quesId){
        this.questionService.deleteQuestion(quesId);
    }

    // eval quiz
    @PostMapping("/eval-quiz")
    public ResponseEntity<?> evalQuiz(@RequestBody List<Question> questions, Principal principal){
        try {
            return ResponseEntity.ok(quizAttemptService.evaluateAndSave(questions, principal.getName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/attempts")
    public ResponseEntity<?> getMyAttempts(Principal principal) {
        return ResponseEntity.ok(quizAttemptService.getUserAttempts(principal.getName()));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/attempts/all")
    public ResponseEntity<?> getAllAttempts() {
        return ResponseEntity.ok(quizAttemptService.getAllAttempts());
    }
}
