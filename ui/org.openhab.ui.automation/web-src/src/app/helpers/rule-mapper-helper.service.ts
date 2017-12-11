import {RuleModel, RuleModelModule, EVENT_TYPE, CONDITION_TYPE, ACTION_TYPE} from '../models/rule.model';
import {RuleDTO, Module} from '../models/rule-do.model';
import {RuleDTOHelperService} from './rule-dto-helper.service';
import { RuleHelperService } from './rule-helper.service';



export class RuleMapperHelperService {
  static mapRuleToDTO(rule: RuleModel): RuleDTO {
    
    const ruleDTO = new RuleDTO();
    ruleDTO.enabled = rule.enabled;
    ruleDTO.uid = rule.uid;
    ruleDTO.name = rule.name;
    ruleDTO.description = rule.description;
    rule.events.forEach(e => RuleDTOHelperService.updateModule('trigger', this.mapEventToModule(e, ruleDTO, rule), ruleDTO));
    rule.conditions.forEach(c => RuleDTOHelperService.updateModule('condition', this.mapConditionToModule(c), ruleDTO));
    rule.actions.forEach(a => RuleDTOHelperService.updateModule('action', this.mapActionToModule(a), ruleDTO));
  
    return ruleDTO;
  }

  static mapEventToModule(event: RuleModelModule, ruleDTO: RuleDTO, rule: RuleModel): Module {
    if (event.unsupportedModule) {
      return event.unsupportedModule;
    }
    const mod = new Module();
    Module.addConfiguration('createdIn', 'AutomationUI', mod);
    mod.id = event.id;
    if (event.itemName) {
      Module.addConfiguration('itemName', event.itemName, mod);
      if (event.operator) {
        if (event.operator === '?') {
          mod.type = 'core.ItemStateUpdateTrigger';
          mod.label = 'an item state is updated';
          mod.description = 'This triggers the rule if an item state is updated (even if it does not change).';
        } else {
          mod.type = 'ItemCommandAboveBelowTrigger';
          Module.addConfiguration('operator', event.operator, mod);
          Module.addConfiguration('state', event.state, mod);
          mod.label = 'an item raises above/drops below a value';
          mod.description = 'This triggers the rule if the item raises above/drops below a certain value.';
        }
      }
    } else {
      mod.type = 'timer.TimeOfDayTrigger';
      mod.label = 'it is a fixed time of day';
      mod.description = 'Triggers at a specified time';
      Module.addConfiguration('time', event.time, mod);
      Module.addConfiguration('days', event.days, mod);

      const dayOfWeek = new Module();
      Module.addConfiguration('createdIn', 'AutomationUI', dayOfWeek);
      dayOfWeek.type = 'timer.DayOfWeekCondition';
      dayOfWeek.label = 'it is a certain day of the week';
      dayOfWeek.description = 'checks for the current day of the week';
      Module.addConfiguration('days', event.days, dayOfWeek);
      dayOfWeek.id = RuleHelperService.getMaxId(rule);
      RuleDTOHelperService.updateModule('condition', dayOfWeek, ruleDTO);
    }
    return mod;
  }

  static mapConditionToModule(condition: RuleModelModule): Module {
    if (condition.unsupportedModule) {
      return condition.unsupportedModule;
    }
    const mod = new Module();
    mod.id = condition.id; 
    Module.addConfiguration('createdIn', 'AutomationUI', mod);
    if (condition.itemName) {
      mod.type = 'core.ItemStateCondition';
      mod.label = 'an item has a given state';
      mod.description = 'Compares the item state with the given value';
      Module.addConfiguration('itemName', condition.itemName, mod);
      Module.addConfiguration('operator', condition.operator, mod);
      Module.addConfiguration('state', condition.state, mod);
    } else {
      mod.type = 'BetweenTimesCondition';
      mod.label = 'Between two points in time';
      mod.description = 'checks if the current time is between two points in time.';
      const split = condition.time.split('/');
      if (split.length > 1) {
        Module.addConfiguration('firstTime', split[0], mod);
        Module.addConfiguration('secondTime', split[1], mod);
      }
    }
    return mod;
  }

  static mapActionToModule(action: RuleModelModule): Module {
    if (action.unsupportedModule) {
      return action.unsupportedModule;
    }
    const mod = new Module();
    mod.id = action.id; 
    Module.addConfiguration('createdIn', 'AutomationUI', mod);
    mod.type = 'core.ItemCommandAction';
    mod.label = 'send a command';
    mod.description = 'Sends a command to a specified item.';
    Module.addConfiguration('itemName', action.itemName, mod);
    Module.addConfiguration('command', action.command, mod);
    return mod;
  }

  static mapDTOtoRule(ruleDTO: RuleDTO): RuleModel {
    const rule = new RuleModel();

    rule.uid = ruleDTO.uid;
    rule.enabled = ruleDTO.enabled;
    rule.name = ruleDTO.name;
    rule.description = ruleDTO.description;
    ruleDTO.triggers.forEach(t => rule.events.push(this.mapModuleToEvent(t)));
    ruleDTO.conditions.filter(c => c.type !== 'timer.DayOfWeekCondition').forEach(c => rule.conditions.push(this.mapModuleToCondition(c)));
    ruleDTO.actions.forEach(a => rule.actions.push(this.mapModuleToAction(a)));

    return rule;
  }

  static mapModuleToEvent(mod: Module): RuleModelModule {
    const event = new RuleModelModule();
    event.type = EVENT_TYPE;
    event.id = mod.id;
    event.label = mod.label;
    if (Module.getConfiguration('createdIn', mod) !== 'AutomationUI') {
      event.unsupportedModule = mod;
    } else if (mod.type === 'core.ItemStateUpdateTrigger') {
      event.itemName = Module.getConfiguration('itemName', mod);
      event.operator = '?';
    } else if (mod.type === 'ItemCommandAboveBelowTrigger') {
      event.itemName = Module.getConfiguration('itemName', mod);
      event.operator = Module.getConfiguration('operator', mod);
      event.state = Module.getConfiguration('state', mod);
    } else if (mod.type === 'timer.TimeOfDayTrigger') {
      event.time = Module.getConfiguration('time', mod);
      event.days = Module.getConfiguration('days', mod);
    }
    return event;
  }

  static mapModuleToCondition(mod: Module): RuleModelModule {
    const condition = new RuleModelModule();
    condition.type = CONDITION_TYPE;
    condition.id = mod.id;
    condition.label = mod.label;
    if (Module.getConfiguration('createdIn', mod) !== 'AutomationUI') {
      condition.unsupportedModule = mod;
    } else if (mod.type === 'core.ItemStateCondition') {
      condition.itemName = Module.getConfiguration('itemName', mod);
      condition.operator = Module.getConfiguration('operator', mod);
      condition.state = Module.getConfiguration('state', mod);
    } else if (mod.type === 'BetweenTimesCondition') {
      condition.time = Module.getConfiguration('firstTime', mod) + '/' + Module.getConfiguration('secondTime', mod);
    }
    return condition;
  }

  static mapModuleToAction(mod: Module): RuleModelModule {
    const action = new RuleModelModule();
    action.type = ACTION_TYPE;
    action.id = mod.id;
    action.label = mod.label;
    if (Module.getConfiguration('createdIn', mod) !== 'AutomationUI') {
      action.unsupportedModule = mod;
    } else if (mod.type === 'core.ItemCommandAction') {
      action.itemName = Module.getConfiguration('itemName', mod);
      action.command = Module.getConfiguration('command', mod);
    }
    return action;
  }
}
