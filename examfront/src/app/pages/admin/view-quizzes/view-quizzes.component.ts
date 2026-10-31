import { Component, OnDestroy, OnInit } from '@angular/core';
import { QuizService } from 'src/app/services/quiz.service';
import Swal from 'sweetalert2';
import { Subject, Subscription, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-view-quizzes',
  templateUrl: './view-quizzes.component.html',
  styleUrls: ['./view-quizzes.component.css']
})
export class ViewQuizzesComponent implements OnInit, OnDestroy{
  quizzes: any[] = [];
  searchTerm = '';
  searching = false;
  private readonly searchChanges = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(private _quiz:QuizService){}
  ngOnInit(): void {
    this.searchSubscription = this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((query) => this._quiz.searchQuizzes(query).pipe(
        catchError(() => {
          this.searching = false;
          Swal.fire('Search failed', 'Unable to search quizzes. Please try again.', 'error');
          return of(null);
        })
      ))
    ).subscribe((data) => {
      if (data) {
        this.quizzes = data;
      }
      this.searching = false;
    });
    this.searchChanges.next('');
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  search(): void {
    this.searching = true;
    this.searchChanges.next(this.searchTerm.trim());
  }

  onSearchTermChange(value: string): void {
    this.searchTerm = value;
    this.search();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.search();
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
