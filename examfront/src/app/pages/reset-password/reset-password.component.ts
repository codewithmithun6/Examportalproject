import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit{
  newPassword = '';
  confirmPassword = '';
  submitting = false;
  private email = '';
  private resetToken = '';

  constructor(private userService: UserService, private router: Router, private snackBar: MatSnackBar) {}
  ngOnInit(): void {
    this.email = sessionStorage.getItem('passwordRecoveryEmail') || '';
    this.resetToken = sessionStorage.getItem('passwordRecoveryToken') || '';
    if (!this.email || !this.resetToken) {
      this.router.navigate(['/forget-password']);
    }
  }

  formSubmit(): void {
    if (!this.newPassword.trim() || !this.confirmPassword.trim()) {
      this.snackBar.open('Enter and confirm your new password.', '', { duration: 3000 });
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.snackBar.open('Passwords do not match.', '', { duration: 3000 });
      return;
    }

    this.submitting = true;
    this.userService.resetForgottenPassword({
      email: this.email,
      resetToken: this.resetToken,
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    }).subscribe({
      next: () => {
        this.submitting = false;
        sessionStorage.removeItem('passwordRecoveryEmail');
        sessionStorage.removeItem('passwordRecoveryToken');
        Swal.fire('Password reset', 'You can now sign in with your new password.', 'success')
          .then(() => this.router.navigate(['/login']));
      },
      error: (error) => {
        this.submitting = false;
        const message = typeof error.error === 'string' ? error.error : 'Unable to reset your password.';
        Swal.fire('Reset failed', message, 'error');
      }
    });
  }

}
