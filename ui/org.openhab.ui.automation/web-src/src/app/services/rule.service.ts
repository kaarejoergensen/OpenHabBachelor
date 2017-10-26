import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/of';

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

  deleteRule(uid: string): Observable<boolean> {
    if (uid !== undefined && uid !== null) {
      return this.http.delete(this.apiURL + '/rest/rules/' + uid, {headers: this.headers})
        .map(response => response.ok as boolean);
    } else {
      return Observable.of(false);
    }
  }

  enableDisableRule(rule: any): Observable<boolean> {
    if (rule !== undefined && rule !== null) {
      return this.http.post(this.apiURL + '/rest/rules/' + rule.uid + '/enable', rule.enabled ? 'false' : 'true')
        .map(response => response.ok as boolean);
    } else {
      Observable.of(false);
    }
  }
}
