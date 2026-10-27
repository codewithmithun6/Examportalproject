import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-verify-otp',
  templateUrl: './verify-otp.component.html',
  styleUrls: ['./verify-otp.component.css']
})
export class VerifyOtpComponent implements OnInit{

  OTP = '';
  email = '';
  submitting = false;

  constructor(
    private _router:Router,
    private _snack: MatSnackBar,
    private userService: UserService
  ){}
  ngOnInit(): void {
    this.email = sessionStorage.getItem('passwordRecoveryEmail') || '';
    if (!this.email) {
      this._router.navigate(['/forget-password']);
    }
  }

  formSubmit(): void {
    if (!/^\d{6}$/.test(this.OTP)) {
      this._snack.open('Enter the six-digit verification code.', '', { duration: 3000 });
      return;
    }

    this.submitting = true;
    this.userService.verifyPasswordRecoveryOtp(this.email, this.OTP).subscribe({
      next: (resetToken) => {
        this.submitting = false;
        sessionStorage.setItem('passwordRecoveryToken', resetToken);
        this._router.navigate(['/reset-password']);
      },
      error: (error) => {
        this.submitting = false;
        const message = typeof error.error === 'string' ? error.error : 'Unable to verify the code.';
        Swal.fire('Verification failed', message, 'error');
      }
    });
  }

  resendCode(): void {
    if (this.submitting) return;
    this.submitting = true;
    this.userService.sendPasswordRecoveryOtp(this.email).subscribe({
      next: () => {
        this.submitting = false;
        this.OTP = '';
        this._snack.open('If the account exists, a new code has been sent.', '', { duration: 4000 });
      },
      error: (error) => {
        this.submitting = false;
        const message = typeof error.error === 'string' ? error.error : 'Unable to resend the code.';
        Swal.fire('Unable to resend code', message, 'error');
      }
    });
  }

}
