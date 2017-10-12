import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable()
export class RuleService {
  apiURL = environment.apiURL;
  constructor(private http: Http) {}

  createRule(rule: any): void {
    this.http.post(this.apiURL + '/rest/rules', JSON.stringify(rule));
  }
}
