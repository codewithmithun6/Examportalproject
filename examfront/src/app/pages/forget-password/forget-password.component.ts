import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-forget-password',
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.css']
})
export class ForgetPasswordComponent {
  emailAddress = '';
  submitting = false;

  constructor(
    private _snack: MatSnackBar,
    private _router:Router,
    private userService: UserService
  ){}
  formSubmit(): void {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.emailAddress.trim())) {
      this._snack.open('Enter a valid email address.', '', { duration: 3000 });
      return;
    }

    this.submitting = true;
    this.userService.sendPasswordRecoveryOtp(this.emailAddress.trim()).subscribe({
      next: () => {
        this.submitting = false;
        sessionStorage.setItem('passwordRecoveryEmail', this.emailAddress.trim());
        sessionStorage.removeItem('passwordRecoveryToken');
        this._router.navigate(['/verify-otp']);
      },
      error: (error) => {
        this.submitting = false;
        const message = typeof error.error === 'string' ? error.error : 'Unable to send a reset code. Try again.';
        Swal.fire('Unable to send code', message, 'error');
      }
    });
  }
}







