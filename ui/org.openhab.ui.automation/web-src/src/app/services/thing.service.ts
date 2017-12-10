import { environment } from '../../environments/environment';
import { ThingModel } from '../models/thing.model';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class ThingService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  getThings(): Observable<ThingModel[]> {
    return this.http.get(this.apiURL + '/rest/things')
    .map(response => response.json() as ThingModel[]);
  }
}
