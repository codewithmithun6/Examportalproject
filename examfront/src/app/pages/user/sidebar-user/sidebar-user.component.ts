import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { CategoryService } from 'src/app/services/category.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-sidebar-user',
  templateUrl: './sidebar-user.component.html',
  styleUrls: ['./sidebar-user.component.css']
})
export class SidebarUserComponent implements OnInit{

  userName:any;
  categories:any;

  constructor(
    private _cat:CategoryService,
    private _snack: MatSnackBar,
    private _user:UserService,
    private _route:ActivatedRoute
  ){}
  ngOnInit(): void {

    this.userName = this._route.snapshot.params['userName'];
    console.log(this.userName);

    this._user.getUser(this.userName).subscribe(
      (data:any)=>{
        this.userName = data;
      },
      (error)=>{
        this._snack.open('Error in loading users from server','',{
          duration:3000,
        });
      }
    );

    this._cat.categories().subscribe(
      (data:any)=>{
      this.categories = data;  
      },
      (error)=>{
        this._snack.open('Error in loading categories from server','',{
          duration:3000,
        });
      }
    );
    
  }

}
