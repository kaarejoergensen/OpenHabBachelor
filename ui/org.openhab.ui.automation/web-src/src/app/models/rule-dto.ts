export class RuleDTO {
  uid: string;
  name: string;
  description: string;
  triggers: Module[] = [];
  conditions: Module[] = [];
  actions: Module[] = [];

  getJSON(): any {
    const body = {
      tags: [],
      conditions: this.conditions.map(function(c) { return c.getJSON(); }),
      description: this.description,
      name: this.name,
      triggers: this.triggers.map(function(t) { return t.getJSON(); }),
      configDescriptions: [],
      actions: this.actions.map(function(a) {return a.getJSON(); })
    };
    if (this.uid !== undefined && this.uid !== null) {
      body['uid'] = this.uid;
    }
    return body;
  }
}

export class Module {
  id: string;
  name: string;
  description: string;
  type: string;
  configuration = {};
  // Only used in triggers
  correspondingConditionId: string;

  addConfiguration(name: string, command: any): void {
    this.configuration[name] = command;
  }

  getConfiguration(name: string): string {
    if (this.configuration[name] !== undefined) {
      return this.configuration[name];
    }
    return '';
  }

  removeConfiguration(name: string): void {
    this.configuration[name] = undefined;
  }

  getJSON(): any {
    return {
      'id': this.id,
      'label': this.name,
      'description': this.description,
      'type': this.type,
      'configuration': this.configuration
    };
  }
}
