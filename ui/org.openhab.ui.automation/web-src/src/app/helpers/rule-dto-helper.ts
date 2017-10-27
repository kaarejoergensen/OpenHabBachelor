import { Module, RuleDTO } from '../models/rule-dto';
export class RuleDTOHelper {
  static updateModule(moduleType: string, mod: Module, ruleDTO: RuleDTO): void {
    const modules = this.getModules(moduleType, ruleDTO);
    const maxId = this.getMaxId(ruleDTO);
    if (!mod.id) {
      mod.id = maxId;
      modules.push(mod);
    } else {
      const index = this.searhArray(modules, mod.id);
      if (index !== -1) {
        modules[index] = mod;
      } else {
        modules.push(mod);
      }
    }
    if (moduleType === 'condition') {
      this.updateModule('trigger', this.inferTrigger(mod, ruleDTO), ruleDTO);
    }
  }

  private static searhArray(modules: Module[], id: string): number {
    for (let i = 0; modules != null && i < modules.length; i++) {
      if (modules[i].id === id) {
        return i;
      }
    }
    return -1;
  }

  private static getModules(moduleType: string, ruleDTO: RuleDTO): Module[] {
    if (moduleType === 'condition') {
      return ruleDTO.conditions;
    } else if (moduleType === 'action') {
      return ruleDTO.actions;
    } else {
      return ruleDTO.triggers;
    }
  }

  private static inferTrigger(condition: Module, ruleDTO: RuleDTO): Module {
    const trigger = new Module;
    const index = this.findExistingTriggerIndex(condition.id, ruleDTO);
    if (index !== -1) {
      trigger.id = ruleDTO.triggers[index].id;
    }
    trigger.correspondingConditionId = condition.id;
    if (condition.type !== 'timer.DayOfWeekCondition') {
      trigger.type = 'core.ItemStateUpdateTrigger';
      trigger.addConfiguration('itemName', condition.getConfiguration('itemName'));
      trigger.name = 'an item state is updated';
      trigger.description = 'This triggers the rule if an item state is updated (even if it does not change).';
    } else {
      trigger.type = 'timer.TimeOfDayTrigger';
      trigger.name = 'it is a fixed time of day';
      trigger.description = 'Triggers at a specified time';
      trigger.addConfiguration('time', condition.getConfiguration('tempTime'));
      condition.removeConfiguration('tempTime');
    }
    return trigger;
  }

  private static getMaxId(ruleDTO: RuleDTO): string {
    let maxId = 0;
    const modules = ['condition', 'action', 'trigger'];
    for (let i = 0; i < modules.length; i++) {
      const moduleArray = this.getModules(modules[i], ruleDTO);
      for (let j = 0; j < moduleArray.length; j++) {
        const mod = moduleArray[j];
        if (mod.id && !isNaN(parseInt(mod.id, 10)) && parseInt(mod.id, 10) > maxId) {
          maxId = parseInt(mod.id, 10);
        }
      }
    }
    return (++maxId).toString();
  }

  private static findExistingTriggerIndex(id: string, ruleDTO: RuleDTO): number {
    for (let i = 0; i < ruleDTO.triggers.length; i++) {
      const trigger = ruleDTO.triggers[i];
      if (trigger.correspondingConditionId === id) {
        return i;
      }
    }
    return -1;
  }
}
