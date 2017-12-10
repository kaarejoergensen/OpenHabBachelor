import { Module } from './rule-do.model';
import { ThingModel } from './thing.model';
export const OPERATORS = [{name: 'greater than', value: '>'}, {name: 'equal to', value: '='}, {name: 'less than', value: '<'}];
export const OPERATORS_EVENT = [{name: 'raises above', value: '>'}, {name: 'changes to', value: '='}, {name: 'drops below', value: '<'}, 
                                  {name: 'is changed', value: '?'}];
export const SWITCH_STATES = [{name: 'turned off', value: 'OFF'}, {name: 'turned on', value: 'ON'}];
export const SWITCH_STATES_EVENT = [{name: 'turned off', value: 'OFF'}, {name: 'turned on', value: 'ON'}, {name: 'changed', value: '?'}];
export const DAYS = [{name: 'M', value: 'MON'}, {name: 'T', value: 'TUE'}, {name: 'W', value: 'WED'},
  {name: 'T', value: 'THU'}, {name: 'F', value: 'FRI'}, {name: 'S', value: 'SAT'}, {name: 'S', value: 'SUN'}];

export const EVENT_TYPE = 'event';
export const CONDITION_TYPE = 'condition';
export const ACTION_TYPE = 'action';

export class RuleModel {
  uid: string;
  enabled: boolean;
  name: string = null;
  description: string = null;
  events: RuleModelModule[] = [];
  conditions: RuleModelModule[] = [];
  actions: RuleModelModule[] = [];
}

export class RuleModelModule {
  id: string;
  type: string;
  label: string;
  // Common
  itemName: string;
  thing: ThingModel;
  operator: string;
  state: string;
  // Event
  time: string;
  // Condition
  days: string[] = [];
  // Action
  command: string;
  // Unsupported
  unsupportedModule: Module;
}
