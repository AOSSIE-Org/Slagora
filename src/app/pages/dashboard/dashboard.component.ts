import { Component, OnInit } from '@angular/core';
import { NbDialogRef, NbDialogService, NbIconConfig, NbToastrService } from '@nebular/theme';
import { HttpErrorResponse } from '@angular/common/http';
import { TemplateRef } from '@angular/core';

@Component({
  selector: 'ngx-dashboard',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  
  constructor() {
    
  }

  ngOnInit(): void {
  }
 
}
