import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  cUser = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {}

  formSubmit(): void {
    const { oldPassword, newPassword, confirmPassword } = this.cUser;

    if (!oldPassword.trim() || !newPassword.trim() || !confirmPassword.trim()) {
      this.snackBar.open('Enter all password fields.', '', { duration: 3000 });
      return;
    }

    if (oldPassword === newPassword) {
      this.snackBar.open('New password must be different from the old password.', '', { duration: 3000 });
      return;
    }

    if (newPassword !== confirmPassword) {
      this.snackBar.open('New password and confirmation do not match.', '', { duration: 3000 });
      return;
    }

    this.userService.changePassword(this.cUser).subscribe({
      next: () => {
        this.cUser = { oldPassword: '', newPassword: '', confirmPassword: '' };
        Swal.fire('Success', 'Password has been changed successfully.', 'success');
      },
      error: (error) => {
        const message = typeof error.error === 'string'
          ? error.error
          : 'Unable to change password. Please try again.';
        Swal.fire('Unable to change password', message, 'error');
      }
    });
  }
}

