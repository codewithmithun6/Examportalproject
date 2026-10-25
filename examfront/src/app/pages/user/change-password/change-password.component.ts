import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { LoginService } from 'src/app/services/login.service';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent implements OnInit{

 public cUser={
  userName:'',
  oldPassword: '',
  newPassword: '',
 }

 user:any=null;
  http: any;

  constructor(
    private _user: UserService, 
    private _snack: MatSnackBar,
    private _route: ActivatedRoute,
    private _login: LoginService
    ){}
  ngOnInit(): void {
    this.user = this._login.getUser();    
    console.log(this.user);
    
    this.cUser.userName = this._route.snapshot.params['userName'];
    console.log(this.cUser.userName);
    
  }

  formSubmit(){


    if(this.cUser.oldPassword.trim()=='' || this.cUser.oldPassword.trim()==null)
    {
      this._snack.open('Old Password  is Required !!','',{
        duration:3000,
      });
      return;
    }

    if(this.cUser.newPassword.trim()=='' || this.cUser.newPassword.trim()==null)
    {
      this._snack.open('New Password  is Required !!','',{
        duration:3000,
      });
      return;
    }

    // validate old password not same to new password
    if(this.cUser.oldPassword==this.cUser.newPassword)
    {
      this._snack.open('old password and new Password should not be same !!','',{
        duration:3000,
      });
      return;
    }

    
    

    // all done

    // this._user.changePassword(userName: string, oldPassword: string, newPassword: string).subscribe(
    //     (data:any)=>{
    //     this._user.userName=''
    //     this.user.oldPassword=''    
    //     this.user.newPassword=''
    //     alert(data);
    //     console.log(data);
    //     Swal.fire('Success !!', 'Password has been changed successfully', 'success');
    //   },
    //   (error)=>{
    //     console.log(error);
    //     Swal.fire('Error !!', 'Server error', 'error');

    //   }

    // );

    

  //   changePassword(userName: string, oldPassword: string, newPassword: string) {
  //     const request = {
  //         username: username,
  //         oldPassword: oldPassword,
  //         newPassword: newPassword
  //     };

  //     this.http.post('/api/users/change-password', request).subscribe(
  //         response => {
  //             console.log('Password changed successfully');
  //             // Handle success, e.g., show a success message
  //         },
  //         error => {
  //             console.error('Error changing password', error);
  //             // Handle error, e.g., show an error message
  //         }
  //     );
  // }
  // changePassword(username: any, string: any, oldPassword: any, string1: any, newPassword: any, string2: any) {
  //   throw new Error('Method not implemented.');
  // }




    this._user.changePassword(this.cUser).subscribe(
      (data:any)=>{
        // this.user.oldPassword=''    
        // this.user.newPassword=''
        alert(data);
        console.log(data);
        Swal.fire('Success !!', 'Password has been changed successfully', 'success');
      },
      (error)=>{
        console.log(error);
        Swal.fire('Error !!', 'Server error', 'error');

      }

    );
  }

}
function changePassword(username: any, string: any, oldPassword: any, string1: any, newPassword: any, string2: any) {
  throw new Error('Function not implemented.');
}

