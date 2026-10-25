import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import baseUrl from './helper';

@Injectable({
  providedIn: 'root'
})
export class CotegoryService {

  constructor(private _http:HttpClient) { }

  // load all the categories
  public categories(){
    return this._http.get(`${baseUrl}/categories`);

  }
 
}
