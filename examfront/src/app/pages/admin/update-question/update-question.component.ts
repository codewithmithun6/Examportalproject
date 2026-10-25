import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import { QuestionService } from 'src/app/services/question.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-update-question',
  templateUrl: './update-question.component.html',
  styleUrls: ['./update-question.component.css']
})
export class UpdateQuestionComponent implements OnInit{

  public Editor = ClassicEditor;

  quesId=0;
  question:any;
  quesTitle:any;

  // question:any ={
  //   quiz:{},
    
  //   content:'',
  //   option1:'',
  //   option2:'',
  //   option3:'',
  //   option4:'',
  //   answer:'',
  // };


  constructor(
    private _route: ActivatedRoute,
    private _question: QuestionService,
    private _router: Router
  ){}
  ngOnInit(): void {

    this.quesId =this._route.snapshot.params['quesid'];
    this.quesTitle = this._route.snapshot.params['title']
    // alert(this.quesId);
    this._question.getQuestion(this.quesId).subscribe(
      (data:any)=>{
        this.question = data;
        console.log(this.question);
      },
      (error)=>{
        console.log(error);
      }
    );    
    
  }

  // form submit

  

   // update questions form submit
   public updateData(){

    // validation 

    if(this.question.content.trim()=='' || this.question.content==null){
      return;
    }

    if(this.question.option1.trim()=='' || this.question.option1==null){
      return;
    }

    if(this.question.option2.trim()=='' || this.question.option2==null){
      return;
    }

    if(this.question.answer.trim()=='' || this.question.answer==null){
      return;
    }

    // alert('test for update');
    this._question.updateQuestion(this.question).subscribe(
      (data:any)=>{
        Swal.fire('Success !!','Question updated successfully !!','success').then((e)=>{
          // this._router.navigate(['/admin/view-questions']);
        });
      },
      (error)=>{
        Swal.fire('Error !!','Error while updating the Question !!','error');
        console.log(error);
      },
    );

  }

   // form submit

  

}
