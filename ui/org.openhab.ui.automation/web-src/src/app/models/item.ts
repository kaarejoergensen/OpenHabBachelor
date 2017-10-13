export class Item {
  link: string;
  state: string;
  stateDescription: StateDescription;
  type: string;
  name: string;
  label: string;
  thingName: string;
}

export class StateDescription {
  minimum: number;
  maximum: number;
  step: number;
  pattern: string;
  readonly: boolean;
}
