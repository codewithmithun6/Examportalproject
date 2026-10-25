import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/services/user.service';
import {MatSnackBar} from '@angular/material/snack-bar'
import Swal from 'sweetalert2'

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
  constructor(private userService:UserService,private snack:MatSnackBar){}
  
  public user = {
    userName: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  };
  ngOnInit(): void {
    
  }

  formSubmit(){
    console.log(this.user);
    if(this.user.userName=='' || this.user.userName==null){
      // alert('User is required !!');
      this.snack.open('Username is required !!','',
      {
        duration: 3000,
      }
      )
      return;
    }

    // validate

    //add user: userService 
    this.userService.addUser(this.user).subscribe(
      (data:any)=>{
        // success
        console.log(data);
        // alert('Success');
        Swal.fire('Congratulations Successfully done !!','Your User Id is '+data.id, 'success');

      },
      (error)=>{
        //error

        console.log(error);
        // alert('Somethin went wrong !!');
        this.snack.open('Something went wrong !!','',
        {
          duration: 3000,
        });
      }
    )

  }

  // this.user

}
