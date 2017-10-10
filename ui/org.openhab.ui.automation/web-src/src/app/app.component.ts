import { Component } from '@angular/core';

export class Hero {
    id: number;
    name: string;
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  titleText = 'Welcome to the Automation UI for openHAB project!';
  hero: Hero = {
          id: 1,
          name: 'Winston'
  };
}