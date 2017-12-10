export class RuleDTO {
  enabled: boolean;
  uid: string;
  name: string;
  description: string;
  triggers: Module[] = [];
  conditions: Module[] = [];
  actions: Module[] = [];

  getJSON(): any {
    const body = {
      tags: [],
      conditions: this.conditions.map(function(c) { return Module.getJSON(c); }),
      description: this.description,
      name: this.name,
      triggers: this.triggers.map(function(t) { return Module.getJSON(t); }),
      configDescriptions: [],
      actions: this.actions.map(function(a) {return Module.getJSON(a); })
    };
    if (this.uid !== undefined && this.uid !== null) {
      body['uid'] = this.uid;
    }
    return body;
  }
  
}

export class Module {
  id: string;
  label: string;
  description: string;
  type: string;
  configuration = {};

  static addConfiguration(name: string, command: any, mod: Module): void {
    mod.configuration[name] = command;
  }

  static getConfiguration(name: string, mod: Module): any {
    if (mod.configuration[name] !== undefined) {
      return mod.configuration[name];
    }
    return '';
  }

  static removeConfiguration(name: string, mod: Module): void {
    mod.configuration[name] = undefined;
  }

  static getJSON(mod: Module): any {
    return {
      'id': mod.id,
      'label': mod.label,
      'description': mod.description,
      'type': mod.type,
      'configuration': mod.configuration
    };
  }
}
