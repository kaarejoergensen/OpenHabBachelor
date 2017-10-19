import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class RuleService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  createRule(rule: any): void {
    this.http.post(this.apiURL + '/rest/rules', JSON.stringify(rule));
  }

  getRules(): Observable<any[]> {
    return this.http.get(this.apiURL + '/rest/rules')
      .map(response => response.json() as any[]);
  }
}
