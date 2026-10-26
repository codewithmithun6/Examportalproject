import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from 'src/app/services/login.service';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-update-user-profile',
  templateUrl: './update-user-profile.component.html',
  styleUrls: ['./update-user-profile.component.css']
})
export class UpdateUserProfileComponent implements OnInit{

  user: any = null;
  selectedPicture: File | null = null;
  picturePreview: string | null = null;
  saving = false;

  constructor(
    private userService: UserService,
    private loginService: LoginService,
    private router: Router
  ){}
  ngOnInit(): void {
    this.loginService.getCurrentUser().subscribe({
      next: (user: any) => this.user = user,
      error: () => Swal.fire('Error', 'Unable to load your profile.', 'error')
    });
  }

  onPictureSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedPicture = input.files?.[0] ?? null;
    if (this.picturePreview) {
      URL.revokeObjectURL(this.picturePreview);
    }
    this.picturePreview = this.selectedPicture ? URL.createObjectURL(this.selectedPicture) : null;
  }

  clearForm(pictureInput: HTMLInputElement): void {
    this.selectedPicture = null;
    this.picturePreview = null;
    pictureInput.value = '';
    this.loginService.getCurrentUser().subscribe((user: any) => this.user = user);
  }

  updateData(): void {
    if (!this.user || this.saving) {
      return;
    }

    const formData = new FormData();
    formData.append('firstName', this.user.firstName || '');
    formData.append('lastName', this.user.lastName || '');
    formData.append('email', this.user.email || '');
    formData.append('phone', this.user.phone || '');
    if (this.selectedPicture) {
      formData.append('profilePicture', this.selectedPicture);
    }

    this.saving = true;
    this.userService.updateProfile(formData).subscribe({
      next: () => {
        this.saving = false;
        Swal.fire('Success', 'Your profile has been updated.', 'success')
          .then(() => this.router.navigate(['/user-dashboard/user-profile']));
      },
      error: (error) => {
        this.saving = false;
        const message = typeof error.error === 'string' ? error.error : 'Unable to update your profile.';
        Swal.fire('Update failed', message, 'error');
      }
    });
  }

}
