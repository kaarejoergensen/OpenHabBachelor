import { Rule, Condition, STATE_CONDITION_TYPE, TIME_CONDITION_TYPE, Action } from '../models/rule';
import { RuleDTO, Module } from '../models/rule-dto';
import { RuleDTOHelper } from './rule-dto-helper';
export class RuleMapperHelper {

  static mapRuleToDTO(rule: Rule): RuleDTO {
    const ruleDTO = new RuleDTO();

    ruleDTO.name = rule.name;
    ruleDTO.description = rule.description;
    rule.conditions.forEach(c => RuleDTOHelper.updateModule('condition', this.mapConditionToModule(c), ruleDTO));
    rule.actions.forEach(a => RuleDTOHelper.updateModule('action', this.mapActionToModule(a), ruleDTO));

    return ruleDTO;
  }

  static mapConditionToModule(condition: Condition): Module {
    const mod = new Module();
    if (condition.type === STATE_CONDITION_TYPE) {
      mod.type = 'core.ItemStateCondition';
      mod.label = 'an item has a given state';
      mod.description = 'Compares the item state with the given value';
      Module.addConfiguration('itemName', condition.itemName, mod);
      Module.addConfiguration('operator', condition.operator, mod);
      Module.addConfiguration('state', condition.state, mod);
    } else if (condition.type === TIME_CONDITION_TYPE) {
      mod.type = 'timer.DayOfWeekCondition';
      mod.label = 'it is a certain day of the week';
      mod.description = 'checks for the current day of the week';
      Module.addConfiguration('days', condition.days, mod);
      Module.addConfiguration('tempTime', condition.tempTime, mod);
    }
    return mod;
  }

  static mapActionToModule(action: Action): Module {
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
    ruleDTO.conditions.forEach(c => rule.conditions.push(this.mapModuleToCondition(c, ruleDTO.triggers)));
    ruleDTO.actions.forEach(a => rule.actions.push(this.mapModuleToAction(a)));

    return rule;
  }

  static mapModuleToCondition(mod: Module, triggers?: Module[]): Condition {
    const condition = new Condition();
    if (mod.type === 'core.ItemStateCondition') {
      condition.type = STATE_CONDITION_TYPE;
      condition.itemName = Module.getConfiguration('itemName', mod);
      condition.operator = Module.getConfiguration('operator', mod);
      condition.state = Module.getConfiguration('state', mod);
    } else if (mod.type === 'timer.DayOfWeekCondition') {
      condition.type = TIME_CONDITION_TYPE;
      condition.days = Module.getConfiguration('days', mod);
      if (triggers) {
        const timers = triggers.filter(t => t.type === 'timer.TimeOfDayTrigger');
        if (timers && timers.length > 0) {
          condition.tempTime = Module.getConfiguration('time', timers[0]);
        }
      }
    }
    return condition;
  }

  static mapModuleToAction(mod: Module): Action {
    const action = new Action();
    action.itemName = Module.getConfiguration('itemName', mod);
    action.command = Module.getConfiguration('command', mod);
    return action;
  }
}
