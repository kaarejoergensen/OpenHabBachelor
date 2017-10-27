import { Injectable } from '@angular/core';

@Injectable()
export class SharedPropertiesService {
  private result: any;

  constructor() {
  }

  getResult(): any {
    return this.result;
  }

  setResult(result: any): void {
    this.result = result;
  }

  reset(): void {
    this.result = null;
  }
}
