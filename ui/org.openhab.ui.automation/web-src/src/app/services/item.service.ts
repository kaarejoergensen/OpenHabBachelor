import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Item } from '../models/item';
import { Headers, Http } from '@angular/http';
import { Observable} from 'rxjs/Observable';

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';


@Injectable()
export class ItemService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  getItems(): Observable<Item[]> {
    return this.http.get(this.apiURL + '/rest/items')
    .map(response => response.json() as Item[]);
  }
}
