import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { SharedPropertiesService } from './services/shared-properties.service';
import { ItemsComponent } from './components/items/items.component';
import { CreateComponent } from './components/create/create.component';
import { ModuleCreatorDialogComponent } from './components/module-creator-dialog/module-creator-dialog.component';
import { RouterModule, Routes } from '@angular/router';
import { OverviewComponent, DialogDeleteRuleComponent } from './components/overview/overview.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule, MatInputModule, MatSelectModule, MatNativeDateModule, MatDatepickerModule } from '@angular/material';
import { MatProgressBarModule, MatStepperModule, MatProgressSpinnerModule, MatDialogModule, MatListModule } from '@angular/material';
import { MatIconModule, MatSlideToggleModule, MatSnackBarModule } from '@angular/material';
import 'hammerjs';

const routes: Routes = [
      {
        path: 'create',
        component: CreateComponent
      },
      {
        path: 'overview',
        component: OverviewComponent
      },
      {
        path: '', redirectTo: 'overview', pathMatch: 'full'
      }
];

@NgModule({
  declarations: [
    AppComponent,
    ItemsComponent,
    CreateComponent,
    OverviewComponent,
    ModuleCreatorDialogComponent,
    DialogDeleteRuleComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    RouterModule.forRoot(routes, { useHash: true }),
    BrowserAnimationsModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatNativeDateModule,
    MatDatepickerModule,
    MatProgressBarModule,
    MatStepperModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatListModule,
    MatIconModule,
    MatSlideToggleModule,
    MatSnackBarModule
  ],
  providers: [SharedPropertiesService],
  bootstrap: [AppComponent],
  entryComponents: [ItemsComponent, ModuleCreatorDialogComponent, OverviewComponent, DialogDeleteRuleComponent]
})
export class AppModule { }
