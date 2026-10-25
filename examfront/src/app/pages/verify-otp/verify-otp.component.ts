import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-verify-otp',
  templateUrl: './verify-otp.component.html',
  styleUrls: ['./verify-otp.component.css']
})
export class VerifyOtpComponent implements OnInit{

  OTP:any;

  constructor(
    private _router:Router,
    private _snack: MatSnackBar
  ){}
  ngOnInit(): void {
    
  }

  formSubmit(){

    if(this.OTP=='' || this.OTP==null ){

      this._snack.open('OTP is required !!','',{
        duration:3000,
      });
      return;

    }

    this._router.navigate(['/reset-password']);
  }

}
