export class Module {
  id: string;
  uid: string;
  configuration = [];
  type: string;
  // Only used in triggers
  correspondingConditionId: string;

  addConfiguration(name: string, command: string): void {
    const obj = {};
    obj[name] = command;
    this.configuration.push(obj);
  }

  getConfiguration(name: string): string {
    for (const conf of this.configuration) {
      if (conf[name] !== undefined) {
        return conf[name];
      }
    }
    return '';
  }
}
