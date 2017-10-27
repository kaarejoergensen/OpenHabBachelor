import { Rule } from '../models/rule';
import { Injectable } from '@angular/core';

@Injectable()
export class SharedPropertiesService {
  private result: any;
  private rule: Rule;

  constructor() {
  }

  getResult(): any {
    return this.result;
  }

  setResult(result: any): void {
    this.result = result;
  }

  getRule(): Rule {
    return this.rule;
  }

  setRule(rule: Rule): void {
    this.rule = rule;
  }

  reset(): void {
    this.result = null;
    this.rule = null;
  }
}
