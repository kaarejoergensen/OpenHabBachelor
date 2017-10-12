import { environment } from '../../environments/environment';
import { Thing } from '../models/thing';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class ThingService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  getThings(): Observable<Thing[]> {
    return this.http.get(this.apiURL + '/rest/things')
    .map(response => response.json() as Thing[]);
  }
}
