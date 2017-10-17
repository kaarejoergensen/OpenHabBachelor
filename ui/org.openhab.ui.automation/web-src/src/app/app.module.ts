import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { SharedPropertiesService } from './services/shared-properties.service';
import { ItemsComponent } from './components/items/items.component';
import { CreateComponent } from './components/create/create.component';
import { ConditionComponent } from './components/condition/condition.component';

@NgModule({
  declarations: [
    AppComponent,
    ItemsComponent,
    CreateComponent,
    ConditionComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule
  ],
  providers: [SharedPropertiesService],
  bootstrap: [AppComponent]
})
export class AppModule { }
