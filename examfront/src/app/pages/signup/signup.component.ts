import { Component } from '@angular/core';
import { UserService } from 'src/app/services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  constructor(
    private userService: UserService,
    private snack: MatSnackBar,
    private router: Router
  ){}
  
  public user = {
    userName: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  };
  otp = '';
  otpSent = false;
  submitting = false;

  formSubmit(): void {
    if (this.submitting) {
      return;
    }

    if (!this.otpSent) {
      this.sendOtp();
      return;
    }

    if (!/^\d{6}$/.test(this.otp)) {
      this.snack.open('Enter the six-digit verification code.', '', { duration: 3000 });
      return;
    }

    this.submitting = true;
    this.userService.verifyRegistrationOtp(this.user.email, this.otp).subscribe({
      next: () => {
        this.submitting = false;
        Swal.fire('Registration complete', 'Your email is verified and your account is ready.', 'success')
          .then(() => this.router.navigate(['/login']));
      },
      error: (error) => {
        this.submitting = false;
        this.showError(error, 'Unable to verify your email.');
      }
    });
  }

  sendOtp(): void {
    const required = Object.values(this.user).every(value => String(value).trim().length > 0);
    if (!required) {
      this.snack.open('Complete all registration fields first.', '', { duration: 3000 });
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.user.email)) {
      this.snack.open('Enter a valid email address.', '', { duration: 3000 });
      return;
    }

    this.submitting = true;
    this.userService.sendRegistrationOtp(this.user).subscribe({
      next: () => {
        this.submitting = false;
        this.otpSent = true;
        this.otp = '';
        this.snack.open('Verification code sent to your email.', '', { duration: 4000 });
      },
      error: (error) => {
        this.submitting = false;
        this.showError(error, 'Unable to send verification code.');
      }
    });
  }

  resetRegistration(): void {
    this.otpSent = false;
    this.otp = '';
  }

  private showError(error: any, fallback: string): void {
    const message = typeof error.error === 'string' ? error.error : fallback;
    Swal.fire('Registration error', message, 'error');
  }
}
