import { Rule, RuleModule, EVENT_TYPE, CONDITION_TYPE, ACTION_TYPE } from '../models/rule';
import { RuleDTO, Module } from '../models/rule-dto';
import { RuleDTOHelper } from './rule-dto-helper';
export class RuleMapperHelper {

  static mapRuleToDTO(rule: Rule): RuleDTO {
    const ruleDTO = new RuleDTO();

    ruleDTO.uid = rule.uid;
    ruleDTO.name = rule.name;
    ruleDTO.description = rule.description;
    rule.events.forEach(e => RuleDTOHelper.updateModule('trigger', this.mapEventToModule(e), ruleDTO));
    rule.conditions.forEach(c => RuleDTOHelper.updateModule('condition', this.mapConditionToModule(c), ruleDTO));
    rule.actions.forEach(a => RuleDTOHelper.updateModule('action', this.mapActionToModule(a), ruleDTO));

    return ruleDTO;
  }
  
  static mapEventToModule(event: RuleModule): Module {
    const mod = new Module();
    
    if (event.itemName) {
      mod.type = 'core.ItemStateUpdateTrigger';
      Module.addConfiguration('itemName', event.itemName, mod);
      mod.label = 'an item state is updated';
      mod.description = 'This triggers the rule if an item state is updated (even if it does not change).';
    } else {
      mod.type = 'timer.TimeOfDayTrigger';
      mod.label = 'it is a fixed time of day';
      mod.description = 'Triggers at a specified time';
      Module.addConfiguration('time', event.time, mod);
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
      mod.type = 'timer.DayOfWeekCondition';
      mod.label = 'it is a certain day of the week';
      mod.description = 'checks for the current day of the week';
      Module.addConfiguration('days', condition.days, mod);
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
    ruleDTO.conditions.forEach(c => rule.conditions.push(this.mapModuleToCondition(c)));
    ruleDTO.actions.forEach(a => rule.actions.push(this.mapModuleToAction(a)));

    return rule;
  }
  
  static mapModuleToEvent(mod: Module): RuleModule {
    const event = new RuleModule();
    event.type = EVENT_TYPE;
    if (mod.type === 'core.ItemStateUpdateTrigger') {
      event.itemName = Module.getConfiguration('itemName', mod);
    } else {
      event.time = Module.getConfiguration('time', mod);
    }
    return event;
  }

  static mapModuleToCondition(mod: Module): RuleModule {
    const condition = new RuleModule();
    condition.type = CONDITION_TYPE;
    if (mod.type === 'core.ItemStateCondition') {
      condition.itemName = Module.getConfiguration('itemName', mod);
      condition.operator = Module.getConfiguration('operator', mod);
      condition.state = Module.getConfiguration('state', mod);
    } else if (mod.type === 'timer.DayOfWeekCondition') {
      condition.days = Module.getConfiguration('days', mod);
    }
    return condition;
  }

  static mapModuleToAction(mod: Module): RuleModule {
    const action = new RuleModule();
    action.type = ACTION_TYPE;
    action.itemName = Module.getConfiguration('itemName', mod);
    action.command = Module.getConfiguration('command', mod);
    return action;
  }
}
