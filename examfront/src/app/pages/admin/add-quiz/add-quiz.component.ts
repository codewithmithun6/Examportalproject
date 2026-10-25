import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoryService } from 'src/app/services/category.service';
import { QuizService } from 'src/app/services/quiz.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-add-quiz',
  templateUrl: './add-quiz.component.html',
  styleUrls: ['./add-quiz.component.css']
})
export class AddQuizComponent implements OnInit{

  categories:any=[
    // {
    //   cid:234,
    //   title:'Programming',
    // },
    // {
    //   cid:212,
    //   title:'Bollywood',
    // },
  ];

  quizData={
    title:'',
    description:'',
    maxMarks:'',
    numberOfQuestions:'',
    active:'true',
    category:{
      cid:'',
    },

  };
  constructor(private _cat:CategoryService,private _snack:MatSnackBar,private _quiz:QuizService){}
  ngOnInit(): void {

    this._cat.categories().subscribe(
      (data:any)=>{
        this.categories=data;
        console.log(this.categories);
      },
      (error)=>{
        console.log(error);
        Swal.fire('Error !!', 'error in loading data from server','error');
      }
    );
    
  }

  // add quize 
  public addQuiz(){
    //title
    // console.log(this.quizData);
    if(this.quizData.title.trim()=='' || this.quizData.title==null)
    {
      this._snack.open('Title Required !!', '',{
        duration:3000,
      });
      return;

    }

    // desc
    if(this.quizData.description.trim()=='' || this.quizData.description==null)
    {
      this._snack.open('Description Required !!', '',{
        duration:3000,
      });
      return;

    }

    //maxMarks
    if(this.quizData.maxMarks.trim()=='' || this.quizData.maxMarks==null)
    {
      this._snack.open('Maximum Marks Required !!', '',{
        duration:3000,
      });
      return;

    }

    // no of ques
    if(this.quizData.numberOfQuestions.trim()=='' || this.quizData.numberOfQuestions==null)
    {
      this._snack.open('Number of Questions Required !!', '',{
        duration:3000,
      });
      return;

    }

    // call server
    this._quiz.addQuiz(this.quizData).subscribe(
      (data:any)=>{
        Swal.fire('Success','Quiz is added Successfully!!','success');

        this.quizData={
          title:'',
          description:'',
          maxMarks:'',
          numberOfQuestions:'',
          active:'true',
          category:{
            cid:'',
          },
      
        };
      },
      (error)=>{
        Swal.fire('Error !!','error while adding quiz','error');
        console.log(error);
      }
    );
  }

}
