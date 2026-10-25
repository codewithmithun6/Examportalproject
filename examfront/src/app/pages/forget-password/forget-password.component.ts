import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';


@Component({
  selector: 'app-forget-password',
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.css']
})
export class ForgetPasswordComponent implements OnInit{

  emailAddress:any;

  constructor(
    private _snack: MatSnackBar,
    private _router:Router
  ){}
  ngOnInit(): void {
    
  }


  formSubmit(){


    if(this.emailAddress=='' || this.emailAddress==null)
    {
      this._snack.open('Email  is Required !!','',{
        duration:3000,
      });
      return;
    }

    this._router.navigate(['/verify-otp']);

}
}





