import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class ModuleTypeService {
  apiURL = environment.apiURL;
  headers = new Headers({'Content-Type': 'application/json'});
  constructor(private http: Http) { }
  
    getModules(): Observable<any[]> {
    return this.http.get(this.apiURL + '/rest/module-types')
      .map(response => response.json() as any[]);
  }
}
