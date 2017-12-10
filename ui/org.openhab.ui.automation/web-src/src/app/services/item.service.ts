import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ItemModel } from '../models/item.model';
import { Headers, Http } from '@angular/http';
import { Observable} from 'rxjs/Observable';

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';


@Injectable()
export class ItemService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  getItems(): Observable<ItemModel[]> {
    return this.http.get(this.apiURL + '/rest/items')
    .map(response => response.json() as ItemModel[]);
  }
  
}
