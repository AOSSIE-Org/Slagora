import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NbAuthBlockComponent, NbAuthModule } from '@nebular/auth';
import { NbLayoutModule, NbCardModule, NbCheckboxModule, NbAlertModule, NbInputModule, NbButtonModule, NbIconModule } from '@nebular/theme';
import { ThemeModule } from '../@theme/theme.module';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SigninComponent } from './signin/signin.component';
import { SignupComponent } from './signup/signup.component';
import { ResetUserPasswordComponent } from './reset-user-password/reset-user-password.component';

@NgModule({
    imports: [
        ThemeModule,
        NbCardModule,
        CommonModule,
        NbLayoutModule,
        NbCardModule,
        NbCheckboxModule,
        NbAlertModule,
        NbInputModule,
        NbButtonModule,
        FormsModule,
        NbIconModule,
        AuthRoutingModule,
        NbAuthModule
    ],
    declarations: [
        AuthComponent,
        SignupComponent,
        SigninComponent,
        ForgotPasswordComponent,
        ResetPasswordComponent,
        ResetUserPasswordComponent
    ]
})
export class AuthModule { }