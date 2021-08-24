import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';
import { APIUserService } from '../@core/utils/user.service';


@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router : Router, private userService: APIUserService){}
  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot):  Observable<boolean> {
      return this.userService.isAuthenticated.pipe(take(1), map(isAuth => {
        if(!isAuth)
          this.router.navigateByUrl('/auth/signin');
        return isAuth;}))
  }
}
