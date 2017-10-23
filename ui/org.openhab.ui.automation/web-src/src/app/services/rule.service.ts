import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class RuleService {
  apiURL = environment.apiURL;
  headers = new Headers({'Content-Type': 'application/json'});
  constructor(private http: Http) {}

  createRule(rule: any): Observable<boolean> {
    return this.http.post(this.apiURL + '/rest/rules', JSON.stringify(rule), {headers: this.headers})
      .map(response => response.ok as boolean);
  }

  getRules(): Observable<any[]> {
    return this.http.get(this.apiURL + '/rest/rules')
      .map(response => response.json() as any[]);
  }
}
