import { Configuration } from './configuration';
import { OnInit } from '@angular/core';

export class Module implements OnInit {
  id: string;
  uid: string;
  configuration: Configuration[];
  type: string;
  // Only used in triggers
  correspondingConditionId: string;

  ngOnInit(): void {
    this.configuration = [];
  }

  addConfiguration(name: string, command: string): void {
    this.configuration.push({
      name: name,
      command: command
    });
  }

  getConfiguration(name: string): string {
    for (const conf of this.configuration) {
      if (conf.name === name) {
        return conf.command;
      }
    }
    return '';
  }
}
