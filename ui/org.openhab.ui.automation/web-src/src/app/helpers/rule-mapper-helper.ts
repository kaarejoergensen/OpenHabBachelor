import { Rule, Condition, STATE_CONDITION_TYPE, TIME_CONDITION_TYPE, Action } from '../models/rule';
import { RuleDTO, Module } from '../models/rule-dto';
import { RuleDTOHelper } from './rule-dto-helper';
export class RuleMapperHelper {
  static mapRuleToDTO(rule: Rule): RuleDTO {
    const ruleDTO = new RuleDTO();

    ruleDTO.name = rule.name;
    ruleDTO.description = rule.description;
    rule.conditions.forEach(c => RuleDTOHelper.updateModule('condition', this.mapCondition(c), ruleDTO));
    rule.actions.forEach(a => RuleDTOHelper.updateModule('action', this.mapAction(a), ruleDTO));

    return ruleDTO;
  }

  static mapCondition(condition: Condition): Module {
    const mod = new Module();
    if (condition.type === STATE_CONDITION_TYPE) {
      mod.type = 'core.ItemStateCondition';
      mod.name = 'an item has a given state';
      mod.description = 'Compares the item state with the given value';
      mod.addConfiguration('itemName', condition.itemName);
      mod.addConfiguration('operator', condition.operator);
      mod.addConfiguration('state', condition.state);
    } else if (condition.type === TIME_CONDITION_TYPE) {
      mod.type = 'timer.DayOfWeekCondition';
      mod.name = 'it is a certain day of the week';
      mod.description = 'checks for the current day of the week';
      mod.addConfiguration('days', condition.days);
      mod.addConfiguration('tempTime', condition.tempTime);
    }
    return mod;
  }

  static mapAction(action: Action): Module {
    const mod = new Module();
    mod.type = 'core.ItemCommandAction';
    mod.name = 'send a command';
    mod.description = 'Sends a command to a specified item.';
    mod.addConfiguration('itemName', action.itemName);
    mod.addConfiguration('command', action.command);
    return mod;
  }
}
