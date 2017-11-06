import { Thing } from './thing';
export const OPERATORS = [{name: 'greater than', value: '>'}, {name: 'equal to', value: '='}, {name: 'less than', value: '<'}];
export const SWITCH_STATES = [{name: 'turned off', value: 'OFF'}, {name: 'turned on', value: 'ON'}];
export const DAYS = [{name: 'M', value: 'MON'}, {name: 'T', value: 'TUE'}, {name: 'W', value: 'WED'},
  {name: 'T', value: 'THU'}, {name: 'F', value: 'FRI'}, {name: 'S', value: 'SAT'}, {name: 'S', value: 'SUN'}];

export const EVENT_TYPE = 'event';
export const CONDITION_TYPE = 'condition';
export const ACTION_TYPE = 'action';

export class Rule {
  uid: string;
  enabled: boolean;
  name: string = null;
  description: string = null;
  events: RuleModule[] = [];
  conditions: RuleModule[] = [];
  actions: RuleModule[] = [];
}

export class RuleModule {
  id: string;
  type: string;
  // Common
  itemName: string;
  thing: Thing;
  // Event
  time: string;
  // Condition
  operator: string;
  state: string;
  days: string[] = [];
  // Action
  command: string;
}
