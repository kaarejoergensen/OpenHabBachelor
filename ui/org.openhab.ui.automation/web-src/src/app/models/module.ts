export class Module {
  id: string;
  uid: string;
  configuration = {};
  type: string;
  name: string;
  description: string;
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
}
