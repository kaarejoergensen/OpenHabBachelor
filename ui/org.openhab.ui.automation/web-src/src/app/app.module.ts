import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { SharedPropertiesService } from './services/shared-properties.service';
import { ItemsComponent } from './components/items/items.component';
import { CreateComponent } from './components/create/create.component';
import { ModalComponent } from './components/modal/modal.component';
import { RouterModule } from '@angular/router';
import { OverviewComponent } from './components/overview/overview.component';

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
    RouterModule.forRoot([
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
    ])
  ],
  providers: [SharedPropertiesService],
  bootstrap: [AppComponent]
})
export class AppModule { }
