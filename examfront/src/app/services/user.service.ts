import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import baseUrl from './helper';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  //add user
  public addUser(user:any){
    return this.http.post(`${baseUrl}/user/`,user);

  }

  public sendRegistrationOtp(user: any){
    return this.http.post(`${baseUrl}/user/registration/send-otp`, user, { responseType: 'text' });
  }

  public verifyRegistrationOtp(email: string, otp: string){
    return this.http.post(`${baseUrl}/user/registration/verify-otp`, { email, otp }, { responseType: 'text' });
  }

  //update user
  public updateUser(user:any){
    return this.http.post(`${baseUrl}/user/`,user);

  }

  // get single user
  public getUser(id:any){
    return this.http.get(`${baseUrl}/user/${id}`);
  }

  public updateProfile(profile: FormData){
    return this.http.put(`${baseUrl}/user/profile`, profile, { responseType: 'text' });
  }

  //change user password
  public changePassword(cUser:any){
    return this.http.put(`${baseUrl}/user/change-password`, cUser, { responseType: 'text' });

  }
}
