import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NbAuthSocialLink, NbAuthResult, getDeepFromObject, NbAuthService, NB_AUTH_OPTIONS } from '@nebular/auth';
import { APIUser } from '../../@core/models/user.model';
import { APIUserService } from '../../@core/utils/user.service';

@Component({
  selector: 'ngx-signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.scss']
})
export class SigninComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
  }
}
