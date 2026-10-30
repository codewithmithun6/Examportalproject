import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuestionService } from 'src/app/services/question.service';
import Swal from 'sweetalert2';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';

@Component({
  selector: 'app-add-question',
  templateUrl: './add-question.component.html',
  styleUrls: ['./add-question.component.css']
})
export class AddQuestionComponent implements OnInit{
  public Editor = ClassicEditor;
qId:any;
qTitle:any;
selectedFile: File | null = null;
importing = false;
question:any ={
  quiz:{},
  
  content:'',
  option1:'',
  option2:'',
  option3:'',
  option4:'',
  answer:'',
};

  constructor(
    private _route:ActivatedRoute,
    private _question:QuestionService
  ){}
  ngOnInit(): void {
    this.qId = this._route.snapshot.params['qid'];
    this.qTitle = this._route.snapshot.params['title'];
    this.question.quiz['qId'] = this.qId;
    
  }

  public formSubmit(){
    // alert('Submited successfully!!');
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

    //form submit
    this._question.addQuestion(this.question).subscribe(
      (data:any)=>{
        Swal.fire('Success !!','Question added successfully.. Add another one','success');
        this.question.content='',
        this.question.option1='',
        this.question.option2='',
        this.question.option3='',
        this.question.option4='',
        this.question.answer=''

      },
      (error)=>{
        Swal.fire('Error','Error while adding question !!','error');
      },
    );
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    const allowedExtensions = ['xlsx', 'csv', 'txt'];
    const extension = file?.name.split('.').pop()?.toLowerCase();

    if (file && (!extension || !allowedExtensions.includes(extension))) {
      this.selectedFile = null;
      input.value = '';
      Swal.fire('Unsupported file', 'Choose an .xlsx, .csv, or tab-delimited .txt file.', 'error');
      return;
    }
    this.selectedFile = file;
  }

  importQuestions(): void {
    if (!this.selectedFile || this.importing) {
      return;
    }

    this.importing = true;
    this._question.importQuestions(Number(this.qId), this.selectedFile).subscribe({
      next: (result) => {
        this.importing = false;
        this.selectedFile = null;
        Swal.fire('Import complete', `${result.importedCount} questions added to ${this.qTitle}.`, 'success');
      },
      error: (error) => {
        this.importing = false;
        const message = error.error?.message || 'Unable to import questions. Check the template and try again.';
        Swal.fire('Import failed', message, 'error');
      }
    });
  }

  downloadTemplate(): void {
    const content = 'Question,Option A,Option B,Option C,Option D,Correct Answer\r\n';
    const blob = new Blob(['\uFEFF', content], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'question-import-template.csv';
    link.click();
    URL.revokeObjectURL(url);
  }

}
