import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../auth-guard/auth.guard';
import { AuthComponent } from './auth.component';
import { SigninComponent } from './signin/signin.component';

export const routes: Routes = [
    {
      path: '',
      component: AuthComponent,
      children: [
        {
          path: 'signin',
          component: SigninComponent,
        },
        {
          path: '',
          redirectTo: 'signin',
          pathMatch: 'full',
        }
      ],
    },
  ];

  @NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
  })
  export class AuthRoutingModule {
  }