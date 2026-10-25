import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-update-user-profile',
  templateUrl: './update-user-profile.component.html',
  styleUrls: ['./update-user-profile.component.css']
})
export class UpdateUserProfileComponent implements OnInit{

  userId=0;
  user:any;
  // quesTitle:any;
  constructor(
    private _route: ActivatedRoute,
    private __user:UserService,
    private _router:Router
  ){}
  ngOnInit(): void {

    this.userId = this._route.snapshot.params['id'];
    // alert(this.userId);
    this.__user.getUser(this.userId).subscribe(
      (data:any)=>{
        console.log(data);
        this.user = data;
        console.log(this.user);
      },
      (error)=>{
        console.log(error);
      }
    );
    
  }

  // update user form submit
  // public updateData(){

  //   // validation     

  //   // alert('test for update');
  //   this.__user.updateUser(this.user).subscribe(
  //     (data:any)=>{
  //       Swal.fire('Success !!','User updated successfully !!','success').then((e)=>{
  //         // this._router.navigate(['/user-dashboard/user-profile']);
  //       });
  //     },
  //     (error)=>{
  //       Swal.fire('Error !!','Error while updating the User !!','error');
  //       console.log(error);
  //     },
  //   );

  // }

}
