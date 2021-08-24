import { Component, OnDestroy } from '@angular/core';
import { APIUserService } from '../@core/utils';
import { NbMenuService } from '@nebular/theme';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { NORMAL_MENU_ITEMS, ADMIN_MENU_ITEMS } from './pages-menu';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'ngx-pages',
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu tag="menu" [items]="menu"></nb-menu>
      <router-outlet></router-outlet>
    </ngx-one-column-layout>
  `,
})
export class PagesComponent implements OnDestroy {

  private destroy$ = new Subject<void>();
  menu = NORMAL_MENU_ITEMS;

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  constructor(private userService: APIUserService, private menuService: NbMenuService, protected router: Router) {
    userService.currentUser.subscribe(user => {
      this.menu = NORMAL_MENU_ITEMS
    })

    this.menuService.onItemClick().subscribe((menuBag) => {
      if (menuBag.item.title == "Logout")
        userService.logout().subscribe(data => {
          this.router.navigate(['auth/signin']);
        },
          (err: HttpErrorResponse) => {
            console.log(err)
          })
      })
    }
}
