import { Component, OnInit } from '@angular/core';
import { SharedPropertiesService } from './services/shared-properties.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  router: any;
  titleText = 'Control your intelligent home';

  ngOnInit(): void {
  }
  constructor(private sharedProperties: SharedPropertiesService) { }
  
  
  onHeaderClick(): void {
   // this.sharedProperties.setResult(result);
   this.router.navigate(['/overview']);
}
}

