import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, ReplaySubject } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { APIUser } from '../models/user.model';
import { JwtService } from '.';
import { JwtToken } from '../models/jwtToken.model';
@Injectable()
export class APIUserService {
  private rootUrl = environment.API_URL;

  getheadersNoAuth() {
    let headerDict = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Credentials': 'true',
      'Access-Token': '',
      'No-Auth': 'True',
      'observe': 'response'
    };
    return headerDict;
  }

  getheadersWithAuth() {
    let headerDict = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Credentials': 'true',
      'observe': 'response'
    };
    return headerDict;
  }

  private currentUserSubject = new BehaviorSubject<APIUser>({} as APIUser);
  public currentUser = this.currentUserSubject.asObservable().pipe(distinctUntilChanged());

  private isAuthenticatedSubject = new ReplaySubject<boolean>(1);
  public isAuthenticated = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient,
    private jwtService: JwtService) { }

  //Unauthenticated user actions

  login(token: string) {
    let reqHeaders = { headers: new HttpHeaders(this.getheadersNoAuth()) };
    return this.http.get(this.rootUrl + `/auth/signin/${token}`, reqHeaders)
      .pipe(map(
        (data: any) => {
          console.log(data)
          let user = new APIUser().deserialize(data.user);
          // Save JWT sent from server in localstorage
          this.jwtService.saveToken(data.token.token);
          // Set isAuthenticated to true
          this.isAuthenticatedSubject.next(true);
          this.currentUserSubject.next(user);

          return user
        })
      );
  }


  logout() {
    let reqHeaders = { headers: new HttpHeaders(this.getheadersWithAuth()) };
    return this.http.get(this.rootUrl + '/auth/logout', reqHeaders)
      .pipe(map(
        (data: any) => {
          this.purgeAuth();
        }));
  }

  getUser() {
    let reqHeaders = { headers: new HttpHeaders(this.getheadersWithAuth()) };
    return this.http.get(this.rootUrl + '/auth/user', reqHeaders)
      .pipe(map(
        (data: any) => {
          let user = new APIUser().deserialize(data);
          this.currentUserSubject.next(user);
          return user
        }));
  }

  //Utility methods

  // Verify JWT in localstorage with server & load user's info.
  // This runs once on application startup.
  populate() {
    // If JWT detected, attempt to get & store user's info
    let token = this.jwtService.getToken();
    if (token) {
      this.getUser()
        .subscribe(
          (res: APIUser) => {
            console.log("got user at startup")
            this.setAuth(res, token)
          },
          err => this.purgeAuth()
        );
    } else {
      // Remove any potential remnants of previous auth states
      console.log("in else clause")
      this.purgeAuth();
    }
  }

  private setAuth(user: APIUser, token: String) {
    // Save JWT sent from server in localstorage
    // this.jwtService.saveToken(token);
    // Set current user data into observable
    this.currentUserSubject.next(user);
    // Set isAuthenticated to true
    this.isAuthenticatedSubject.next(true);
  }

  purgeAuth() {
    console.log("in purge auth")
    // Remove JWT from localstorage
    this.jwtService.destroyToken();
    // Set current user to an empty object
    this.currentUserSubject.next({} as APIUser);
    // Set auth status to false
    this.isAuthenticatedSubject.next(false);
  }

  getCurrentUser(): APIUser {
    return this.currentUserSubject.value;
  }
}
