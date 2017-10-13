export class Thing {
  label: string;
  bridgeUID: string;
  UID: string;
  thingTypeUID: string;
  location: string;
  channels: Channel[];
}

export class Channel {
  linkedItems: string[];
  uid: string;
  label: string;
}
