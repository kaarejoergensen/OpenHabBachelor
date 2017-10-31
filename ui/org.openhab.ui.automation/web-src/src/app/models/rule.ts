import { Thing } from './thing';
export const OPERATORS = [{name: 'greater than', value: '>'}, {name: 'equal to', value: '='}, {name: 'less than', value: '<'}];
export const SWITCH_STATES = [{name: 'turned off', value: 'OFF'}, {name: 'turned on', value: 'ON'}];
export const DAYS = [{name: 'M', value: 'MON'}, {name: 'T', value: 'TUE'}, {name: 'W', value: 'WED'},
  {name: 'T', value: 'THU'}, {name: 'F', value: 'FRI'}, {name: 'S', value: 'SAT'}, {name: 'S', value: 'SUN'}];

export const STATE_CONDITION_TYPE = 'state';
export const TIME_CONDITION_TYPE = 'time';

export class Rule {
  uid: string;
  enabled: boolean;
  name: string = null;
  description: string = null;
  conditions: Condition[] = [];
  actions: Action[] = [];
}

export class Condition {
  id: string;
  type: string;
  // State type
  itemName: string;
  operator: string;
  state: string;
  thing: Thing;
  // Time type
  days: string[] = [];
  tempTime: string;
}

export class Action {
  id: string;
  itemName: string;
  command: string;
  thing: Thing;
}
