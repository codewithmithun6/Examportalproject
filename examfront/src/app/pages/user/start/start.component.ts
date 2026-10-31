import { LocationStrategy } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuestionService } from 'src/app/services/question.service';
import { LoginService } from 'src/app/services/login.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.css']
})
export class StartComponent implements OnInit, OnDestroy{
  [x: string]: any;

  qid:any;
  questions:any;

  marksGot=0;
  totalMarks=0;
  correctAnswers=0;
  attempted = 0;
  unattempted = 0;
  wrongAnswers = 0;
  percentage = 0;
  attemptId: number | null = null;
  isSubmit=false;
  user: any;

  timer:any;
  private timerInterval: any;
  private evaluationStarted = false;
  private previousDocumentTitle = '';

  constructor(
    private locationSt: LocationStrategy,
    private _route: ActivatedRoute,
    private _question: QuestionService,
    private login: LoginService
  ){}
  ngOnInit(): void {

    this.preventBackButton();
    this.qid = this._route.snapshot.params['qid'];
    this.user = this.login.getUser();
    console.log(this.qid);
    this.loadQuestions();
    
  }
  loadQuestions() {
    this._question.getQuestionsOfQuizForTest(this.qid).subscribe(
      (data:any)=>{
        this.questions = data;
        // calculating progrss bar timer
        this.timer = this.questions.length * 2 * 60;

        // console.log(this.questions);

        this.questions.forEach((q:any)=>{
          q['givenAnswer'] = '';
        });
        this.startTimer();
      },
      (error)=>{
        console.log(error);
        Swal.fire("Error !!", "Error in loading questions of quiz", 'error');
      }
    );
  }

  public preventBackButton(){
    history.pushState(null, "null", window.location.href);
    this.locationSt.onPopState(()=>{
      history.pushState(null, "null", window.location.href);
    });

  }

  // submit quiz
  submitQuiz(){
    Swal.fire({
      title: 'Do you want to submit the quiz?',      
      showCancelButton: true,
      confirmButtonText: 'Submit',      
      icon: 'info',
    }).then((e)=>{
      if(e.isConfirmed){

        this.evalQuiz();        

      }
    });
  }

  startTimer(){
    this.timerInterval = window.setInterval(()=>{
      //code
      if(this.timer<=0){
        this.evalQuiz();
        clearInterval(this.timerInterval);
      }else{
        this.timer--;
      }
    },1000);
  }

  // get formated timer
  getFormatedTime(){
    let mm = Math.floor(this.timer/60);
    let ss = this.timer - mm * 60;
    return `${mm} min : ${ss} sec`;
  }

  evalQuiz(){
    if (this.evaluationStarted || !this.questions?.length) {
      return;
    }
    this.evaluationStarted = true;
    clearInterval(this.timerInterval);

    this._question.evalQuiz(this.questions).subscribe(
      (data:any)=>{
        this.marksGot = parseFloat(Number(data.marksGot).toFixed(2));
        this.totalMarks = Number(data.totalMarks ?? this.quiz?.maxMarks ?? 0);
        this.correctAnswers = data.correctAnswers;
        this.attempted = data.attemptedQuestions ?? data.attempted;
        this.unattempted = data.unattemptedQuestions ?? Math.max(0, this.questions.length - this.attempted);
        this.wrongAnswers = data.wrongAnswers ?? Math.max(0, this.attempted - this.correctAnswers);
        this.percentage = Number(data.percentage ?? (this.totalMarks > 0 ? this.marksGot * 100 / this.totalMarks : 0));
        this.attemptId = data.attemptId ?? null;
        this.isSubmit = true;

      },
      (error)=>{
        this.evaluationStarted = false;
        Swal.fire('Unable to submit quiz', 'Please check your connection and submit again.', 'error');
      }
    );
  }

  get quiz(): any {
    return this.questions?.[0]?.quiz;
  }

  get subjectName(): string {
    return this.quiz?.category?.title || 'Subject not available';
  }

  get fullName(): string {
    return [this.user?.firstName, this.user?.lastName].filter(Boolean).join(' ') || this.user?.userName || 'Learner';
  }

  public printPage(){
    this.previousDocumentTitle = document.title;
    document.title = `Quiz Result - ${this.quiz?.title || 'Exam Portal'}`;
    document.body.classList.add('printing-quiz-result');
    window.addEventListener('afterprint', () => {
      document.body.classList.remove('printing-quiz-result');
      document.title = this.previousDocumentTitle;
    }, { once: true });
    window.print();
  }

  ngOnDestroy(): void {
    clearInterval(this.timerInterval);
    document.body.classList.remove('printing-quiz-result');
  }

  }

  

