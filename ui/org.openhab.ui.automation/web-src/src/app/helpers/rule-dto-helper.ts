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
}
