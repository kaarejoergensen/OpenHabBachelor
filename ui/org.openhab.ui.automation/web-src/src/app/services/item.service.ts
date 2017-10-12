import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Item } from '../models/item';
import { Headers, Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';

import 'rxjs/add/operator/toPromise';


@Injectable()
export class ItemService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}
/*
  getItems(): Promise<Item[]> {
    return this.http.get(this.apiURL + '/rest/items')
    .toPromise()
    .then(response => response.json() as Item[])
    .catch(this.handleErrors);
  }*/

  private handleErrors(error: any): Promise<any> {
    console.error('An error occoured', error);
    return Promise.reject(error.message || error);
  }

    public getItems(): Observable<any> {
    return this.http.get(this.apiURL + '/rest/items').
      map((response: Response) => response.json());
}
   public getThings(): Observable<any> {
    return this.http.get(this.apiURL + '/rest/things').
      map((response: Response) => response.json());
}

}
