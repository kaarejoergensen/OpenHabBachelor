import { RuleModel } from '../models/rule.model';
import { ThingModel } from '../models/thing.model';
import { Injectable } from '@angular/core';

@Injectable()
export class SharedPropertiesService {
  private result: any;
  private rule: RuleModel;


  constructor() {
  }

  getResult(): any {
    return this.result;
  }

  setResult(result: any): void {
    this.result = result;
  }

  getRule(): RuleModel {
    return this.rule;
  }

  setRule(rule: RuleModel): void {
    this.rule = rule;
  }

  reset(): void {
    this.result = null;
    this.rule = null;
  }
 
}
