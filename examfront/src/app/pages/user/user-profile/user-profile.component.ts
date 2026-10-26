import { Component, OnInit } from '@angular/core';
import { LoginService } from 'src/app/services/login.service';
import baseUrl from 'src/app/services/helper';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit{

  user: any = null;
  profileImage = '../../../assets/logo.jpg';
  constructor(private login:LoginService){}
  ngOnInit(): void {
    this.login.getCurrentUser().subscribe({
      next: (user: any) => {
        this.user = user;
        this.login.setUser(user);
        this.profileImage = user.profile && user.profile !== 'default.png'
          ? (user.profile.startsWith('http') ? user.profile : `${baseUrl}${user.profile}`)
          : '../../../assets/logo.jpg';
      },
      error: () => this.user = this.login.getUser()
    });
  }

  shareProfile(): void {
    const text = [
      `${this.user.firstName} ${this.user.lastName}`,
      `Email: ${this.user.email || ''}`,
      `Mobile: ${this.user.phone || ''}`
    ].join('\n');

    if (navigator.share) {
      navigator.share({ title: 'My profile', text }).catch(() => undefined);
      return;
    }

    navigator.clipboard.writeText(text).then(
      () => Swal.fire('Copied', 'Profile details copied to clipboard.', 'success'),
      () => Swal.fire('Unable to share', 'Clipboard access is unavailable in this browser.', 'error')
    );
  }

}
