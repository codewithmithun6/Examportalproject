import { Component, OnInit } from '@angular/core';
import { QuizService } from 'src/app/services/quiz.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-view-quizzes',
  templateUrl: './view-quizzes.component.html',
  styleUrls: ['./view-quizzes.component.css']
})
export class ViewQuizzesComponent implements OnInit{
  

  quizzes:any =[
    // {
    //   qid:123,
    //   title: 'Basic Java Quize',
    //   description:'Java is a widely-used programming language for coding web applications. It has been a popular choice among developers for over two decades, with millions of Java applications in use today. Java is a multi-platform, object-oriented, and network-centric language that can be used as a platform in itself',
    //   maxMarks:'100',
    //   numberOfQuestions:'20',
    //   active:'',
    //   category:{
    //     title:'Programming',

    //   }      

    // },
    // {
    //   qid:123,
    //   title: 'Basic Python Quize',
    //   description:'Java is a widely-used programming language for coding web applications. It has been a popular choice among developers for over two decades, with millions of Java applications in use today. Java is a multi-platform, object-oriented, and network-centric language that can be used as a platform in itself',
    //   maxMarks:'100',
    //   numberOfQuestions:'20',
    //   active:'',
    //   category:{
    //     title:'Programming',

    //   }      

    // }
  ];

  constructor(private _quiz:QuizService){}
  ngOnInit(): void {

    this._quiz.quizzes().subscribe(
      (data:any)=>{
        this.quizzes =data;
        console.log(this.quizzes);
      },
      (error)=>{
        console.log(error);
        Swal.fire('Error !!', 'Error in loading data !!', 'error');

      }
    );
    
  }

  // delete quiz
  public deleteQuiz(qId:any){
    Swal.fire({
      icon:'info',
      title:'Are you sure',
      confirmButtonText:'Delete',
      showCancelButton:true,
    }).then((result)=>{
      if(result.isConfirmed)
      {
        //delete...
        this._quiz.deleteQuiz(qId).subscribe(
          (data:any)=>{        
            Swal.fire('Success !!','Quize deleted successfully!!','success');
            this.quizzes=this.quizzes.filter((quiz:any)=>quiz.qId!=qId);
          },
          (error)=>{
            Swal.fire('Error !!','Error is while deleting quize!!','error');
    
          }
        );
      }
    })

  }


}
