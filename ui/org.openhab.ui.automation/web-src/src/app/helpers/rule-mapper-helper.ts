import { Rule, RuleModule, EVENT_TYPE, CONDITION_TYPE, ACTION_TYPE } from '../models/rule';
import { RuleDTO, Module } from '../models/rule-dto';
import { RuleDTOHelper } from './rule-dto-helper';
export const BETWEEN_TIMES_SCRIPT = `(function () {
    var fromString = "%dateFrom";
    var toString = "%dateTo";
    var now = new Date();
    var from = parseDate(fromString);
    var to = parseDate(toString);
    if (to < from) {
      var temp = to;
      to = from;
      from = temp;
    }
    return from < now && to > now;
    
    function parseDate(dateString) {
      var result = new Date();
      var split = dateString.split(":");
      if (split.length > 1) {
        result.setHours(split[0]);
        result.setMinutes(split[1]);
      }
      return result;
    }
})();`;

export class RuleMapperHelper {

  static mapRuleToDTO(rule: Rule): RuleDTO {
    const ruleDTO = new RuleDTO();

    ruleDTO.uid = rule.uid;
    ruleDTO.name = rule.name;
    ruleDTO.description = rule.description;
    rule.events.forEach(e => RuleDTOHelper.updateModule('trigger', this.mapEventToModule(e, ruleDTO), ruleDTO));
    rule.conditions.forEach(c => RuleDTOHelper.updateModule('condition', this.mapConditionToModule(c), ruleDTO));
    rule.actions.forEach(a => RuleDTOHelper.updateModule('action', this.mapActionToModule(a), ruleDTO));

    return ruleDTO;
  }
  
  static mapEventToModule(event: RuleModule, ruleDTO: RuleDTO): Module {
    const mod = new Module();
    
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
      dayOfWeek.type = 'timer.DayOfWeekCondition';
      dayOfWeek.label = 'it is a certain day of the week';
      dayOfWeek.description = 'checks for the current day of the week';
      Module.addConfiguration('days', event.days, dayOfWeek);
      RuleDTOHelper.updateModule('condition', dayOfWeek, ruleDTO);
    }
    
    return mod;
  }

  static mapConditionToModule(condition: RuleModule): Module {
    const mod = new Module();
    if (condition.itemName) {
      mod.type = 'core.ItemStateCondition';
      mod.label = 'an item has a given state';
      mod.description = 'Compares the item state with the given value';
      Module.addConfiguration('itemName', condition.itemName, mod);
      Module.addConfiguration('operator', condition.operator, mod);
      Module.addConfiguration('state', condition.state, mod);
    } else {
      mod.type = 'script.ScriptCondition';
      mod.label = 'a given script evaluates to true';
      mod.description = 'Allows the definition of a condition through a script.';
      Module.addConfiguration('type', 'application/javascript', mod);
      const split = condition.time.split('/');
      if (split.length > 1) {
        Module.addConfiguration('script', BETWEEN_TIMES_SCRIPT.replace('%dateFrom', split[0])
          .replace('%dateTo', split[1]), mod);
        Module.addConfiguration('time', condition.time, mod);
      }
    }
    return mod;
  }

  static mapActionToModule(action: RuleModule): Module {
    const mod = new Module();
    mod.type = 'core.ItemCommandAction';
    mod.label = 'send a command';
    mod.description = 'Sends a command to a specified item.';
    Module.addConfiguration('itemName', action.itemName, mod);
    Module.addConfiguration('command', action.command, mod);
    return mod;
  }

  static mapDTOtoRule(ruleDTO: RuleDTO): Rule {
    const rule = new Rule();

    rule.uid = ruleDTO.uid;
    rule.enabled = ruleDTO.enabled;
    rule.name = ruleDTO.name;
    rule.description = ruleDTO.description;
    ruleDTO.triggers.forEach(t => rule.events.push(this.mapModuleToEvent(t)));
    ruleDTO.conditions.filter(c => c.type !== 'timer.DayOfWeekCondition').forEach(c => rule.conditions.push(this.mapModuleToCondition(c)));
    ruleDTO.actions.forEach(a => rule.actions.push(this.mapModuleToAction(a)));

    return rule;
  }
  
  static mapModuleToEvent(mod: Module): RuleModule {
    const event = new RuleModule();
    event.type = EVENT_TYPE;
    event.id = mod.id;
    if (mod.type === 'core.ItemStateUpdateTrigger') {
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

  static mapModuleToCondition(mod: Module): RuleModule {
    const condition = new RuleModule();
    condition.type = CONDITION_TYPE;
    condition.id = mod.id;
    if (mod.type === 'core.ItemStateCondition') {
      condition.itemName = Module.getConfiguration('itemName', mod);
      condition.operator = Module.getConfiguration('operator', mod);
      condition.state = Module.getConfiguration('state', mod);
    } else if (mod.type === 'script.ScriptCondition') {
      condition.time = Module.getConfiguration('time', mod);
    }
    return condition;
  }

  static mapModuleToAction(mod: Module): RuleModule {
    const action = new RuleModule();
    action.type = ACTION_TYPE;
    action.id = mod.id;
    action.itemName = Module.getConfiguration('itemName', mod);
    action.command = Module.getConfiguration('command', mod);
    return action;
  }
}
