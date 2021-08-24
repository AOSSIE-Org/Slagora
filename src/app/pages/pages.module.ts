import { NgModule } from '@angular/core';
import { NbAccordionModule, NbButtonModule, NbCalendarModule, NbCardModule, NbDatepickerModule, NbDialogModule, NbIconModule, NbInputModule, NbLayoutModule, NbMenuModule, NbSelectModule, NbStepperModule, NbTooltipModule } from '@nebular/theme';

import { ThemeModule } from '../@theme/theme.module';
import { PagesComponent } from './pages.component';
import { PagesRoutingModule } from './pages-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    PagesRoutingModule,
    ThemeModule,
    NbMenuModule,
    NbButtonModule,
    NbCalendarModule,
    NbAccordionModule,
    NbIconModule,
    NbTooltipModule,
    NbCardModule,
    NbStepperModule,
    FormsModule,
    NbInputModule,
    NbDatepickerModule,
    NbDialogModule.forChild(),
    NbSelectModule,
  ],
  declarations: [
    PagesComponent,
    DashboardComponent,
  ]
})
export class PagesModule {
}
