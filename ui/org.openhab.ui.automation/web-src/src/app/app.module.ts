import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { SharedPropertiesService } from './services/shared-properties.service';
import { ItemsComponent } from './components/items/items.component';
import { CreateComponent } from './components/create/create.component';
import { ModalComponent } from './components/modal/modal.component';
import { RouterModule, Routes } from '@angular/router';
import { OverviewComponent } from './components/overview/overview.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule, MatInputModule, MatSelectModule, MatNativeDateModule, MatDatepickerModule } from '@angular/material';

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
        path: '', redirectTo: 'create', pathMatch: 'full'
      }
];

@NgModule({
  declarations: [
    AppComponent,
    ItemsComponent,
    CreateComponent,
    ModalComponent,
    OverviewComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    RouterModule.forRoot(routes, { useHash: true }),
    BrowserAnimationsModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatNativeDateModule,
    MatDatepickerModule
  ],
  providers: [SharedPropertiesService],
  bootstrap: [AppComponent]
})
export class AppModule { }
