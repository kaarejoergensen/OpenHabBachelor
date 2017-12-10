import { RuleModelModule, RuleModel, ACTION_TYPE, CONDITION_TYPE, EVENT_TYPE } from '../models/rule.model';

export class RuleHelperService {
  
  static updateModule(mod: RuleModelModule, rule: RuleModel): void {
    const modules = this.getModules(mod.type, rule);
    const maxId = this.getMaxId(rule);
    
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
  }
  
  static removeModule(mod: RuleModelModule, rule: RuleModel): void {
    const modules = this.getModules(mod.type, rule);
    if (mod.id) {
      const index = this.searhArray(modules, mod.id);
      if (index !== -1) {
        modules.splice(index, 1);
      }
    }
  }

  private static searhArray(modules: RuleModelModule[], id: string): number {
    for (let i = 0; modules != null && i < modules.length; i++) {
      if (modules[i].id === id) {
        return i;
      }
    }
    return -1;
  }

  static getModules(moduleType: string, rule: RuleModel): RuleModelModule[] {
    if (moduleType === EVENT_TYPE) {
      return rule.events;
    } else if (moduleType === CONDITION_TYPE) {
      return rule.conditions;
    } else if (moduleType === ACTION_TYPE) {
      return rule.actions;
    }
  }

  private static getMaxId(rule: RuleModel): string {
    let maxId = 0;
    const modules = [EVENT_TYPE, ACTION_TYPE, CONDITION_TYPE];
    for (let i = 0; i < modules.length; i++) {
      const moduleArray = this.getModules(modules[i], rule);
      for (let j = 0; j < moduleArray.length; j++) {
        const mod = moduleArray[j];
        if (mod.id && !isNaN(parseInt(mod.id, 10)) && parseInt(mod.id, 10) > maxId) {
          maxId = parseInt(mod.id, 10);
        }
      }
    }
    return (++maxId).toString();
  }
}
