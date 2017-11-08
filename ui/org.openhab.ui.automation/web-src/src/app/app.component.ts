import { Component, OnInit } from '@angular/core';
import { SharedPropertiesService } from './services/shared-properties.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  titleText = 'Control your intelligent home';

  ngOnInit(): void {
  }
  constructor(private sharedProperties: SharedPropertiesService) { }
}
