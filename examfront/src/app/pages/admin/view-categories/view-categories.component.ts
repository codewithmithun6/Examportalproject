import { Component, OnInit } from '@angular/core';
import { CategoryService } from 'src/app/services/category.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-view-categories',
  templateUrl: './view-categories.component.html',
  styleUrls: ['./view-categories.component.css']
})
export class ViewCategoriesComponent implements OnInit{

  

  categories:any = [
    // {
    //   cid: 23,
    //   title: 'programming',
    //   description: 'This is programing for testing category',
    // },
    // {
    //   cid: 34,
    //   title: 'GK practice',
    //   description: 'This is programing for testing category',
    // },
    // {
    //   cid: 35,
    //   title: 'Aptitude practice',
    //   description: "This is programing for testing category",
    // },
    // {
    //   cid: 36,
    //   title: 'Bollybood',
    //   description: "This is programing for testing category",
    // },
   
  ];
  _snack: any;
  constructor(private _category:CategoryService){}
  ngOnInit(): void {

    this._category.categories().subscribe(
      (data:any)=>{
        this.categories=data;
        console.log(this.categories);
      },
      (error)=>{
        console.log(error);
        Swal.fire('Error !!', 'Error in loading data', 'error');
      }
    );
    
  }

  // delete quesion
  public deleteCategory(cid:any){
    Swal.fire({
      icon:'info',
      showCancelButton:true,
      confirmButtonText: 'Delete',
      title: 'Are you sure, want ot delete category?',
    }).then((result: { isConfirmed: any; })=>{
      
      if(result.isConfirmed)
      {
        //confirm        
        this._category.deleteCategory(cid).subscribe(
          (data:any)=>{
            this._snack.open('Category deleted successfully !!','',{
              duration:3000,
            });
            this.categories = this.categories.filter((cat:any)=>cat.cid!=cid);
          },
          (error:any)=>{
            this._snack.open('Error in deleting Category !!','',{
              duration:3000,
            });
            console.log(error);          
        });
      }
    });

  }

  

}
