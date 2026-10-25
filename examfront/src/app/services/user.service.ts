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

  //update user
  public updateUser(user:any){
    return this.http.post(`${baseUrl}/user/`,user);

  }

  // get single user
  public getUser(id:any){
    return this.http.get(`${baseUrl}/user/${id}`);
  }

  //change user password
  public changePassword(cUser:any){
    return this.http.put(`${baseUrl}/user/change-password`,cUser);

  }
}
