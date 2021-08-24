import { HttpInterceptor, HttpRequest, HttpHandler, HttpUserEvent, HttpEvent } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Observable } from "rxjs";
import { tap } from 'rxjs/operators';
import { JwtService } from "../@core/utils/jwt.service";
import { APIUserService } from "../@core/utils/user.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private router: Router, private jwtService: JwtService, private userService: APIUserService) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (req.headers.get('No-Auth') == "True")
            return next.handle(req.clone());

        if (this.jwtService.getToken()) {
            const clonedreq = req.clone({
                headers: req.headers.set("X-Auth-Token", this.jwtService.getToken().toString())
            });
            return next.handle(clonedreq).pipe(
                tap(
                succ => { }
                ));
        }
        else {
            this.router.navigateByUrl('/auth/signin');
        }
    }
}