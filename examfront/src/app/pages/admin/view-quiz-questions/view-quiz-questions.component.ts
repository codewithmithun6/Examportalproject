import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { QuestionService } from 'src/app/services/question.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-view-quiz-questions',
  templateUrl: './view-quiz-questions.component.html',
  styleUrls: ['./view-quiz-questions.component.css']
})
export class ViewQuizQuestionsComponent implements OnInit{

  qId:any;
  qTitle:any;
  questions:any = [];

  constructor(
    private _rout:ActivatedRoute,
    private _question:QuestionService,
    private _snack: MatSnackBar
    ){}
  ngOnInit(): void {

    this.qId = this._rout.snapshot.params['qid'];
    this.qTitle = this._rout.snapshot.params['title'];
    this._question.getQuestionsOfQuiz(this.qId).subscribe(
      (data:any)=>{
        this.questions = data;
        console.log(this.questions);
      },
      (error)=>{
        console.log(error);
      }
    );
       
    
  }

  // delete quesion
  public deleteQuestion(qid:any){
    Swal.fire({
      icon:'info',
      showCancelButton:true,
      confirmButtonText: 'Delete',
      title: 'Are you sure, want ot delete question?',
    }).then((result: { isConfirmed: any; })=>{
      
      if(result.isConfirmed)
      {
        //confirm
        this._question.deleteQuestion(qid).subscribe(
          (data:any)=>{
            this._snack.open('Question deleted successfully !!','',{
              duration:3000,
            });
            this.questions = this.questions.filter((q:any)=>q.quesId!=qid);
          },
          (error)=>{
            this._snack.open('Error in deleting quetion !!','',{
              duration:3000,
            });
            console.log(error);          
        });
      }
    });

  }

}
