import { Module } from '../models/module';
import { Injectable } from '@angular/core';

@Injectable()
export class SharedPropertiesService {
  conditions: Module[];
  actions: Module[];
  triggers: Module[];

  constructor() {
    this.init();
  }

  init(): void {
    this.conditions = [];
    this.actions = [];
    this.triggers = [];
  }

  updateModule(moduleType: string, mod: Module): void {
    const modules = this.getModules(moduleType);
    const maxId = this.getMaxId();
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
      this.updateModule('trigger', this.inferTrigger(mod));
    }
  }

  searhArray(modules: Module[], id: string): number {
    for (let i = 0; modules != null && i < modules.length; i++) {
      if (modules[i].id === id) {
        return i;
      }
    }
    return -1;
  }

  getModules(moduleType: string): Module[] {
    if (moduleType === 'condition') {
      return this.conditions;
    } else if (moduleType === 'action') {
      return this.actions;
    } else {
      return this.triggers;
    }
  }

  inferTrigger(condition: Module): Module {
    const trigger = new Module;
    const index = this.findExistingTriggerIndex(condition.id);
    if (index !== -1) {
      trigger.id = this.triggers[index].id;
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

  getMaxId(): string {
    let maxId = 0;
    const modules = ['condition', 'action', 'trigger'];
    for (let i = 0; i < modules.length; i++) {
      const moduleArray = this.getModules(modules[i]);
      for (let j = 0; j < moduleArray.length; j++) {
        const mod = moduleArray[j];
        if (mod.id && !isNaN(parseInt(mod.id, 10)) && parseInt(mod.id, 10) > maxId) {
          maxId = parseInt(mod.id, 10);
        }
      }
    }
    return (++maxId).toString();
  }

  findExistingTriggerIndex(id: string): number {
    for (let i = 0; i < this.triggers.length; i++) {
      const trigger = this.triggers[i];
      if (trigger.correspondingConditionId === id) {
        return i;
      }
    }
    return -1;
  }

  getModuleJSON(moduleType: string): any[] {
    const moduleJSON = [];
    const modules = this.getModules(moduleType);
    for (const mod of modules) {
      const conditionType = typeof mod.uid === 'undefined' ? mod.type : mod.type;
      moduleJSON.push({
        'id': mod.id,
        'label': mod.name,
        'description': mod.description,
        'type': conditionType,
        'configuration': mod.configuration ? mod.configuration : {}
      });
    }
    return moduleJSON;
  }

  reset(): void {
    this.conditions = [];
    this.actions = [];
    this.triggers = [];
  }
}
