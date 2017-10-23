import { Item } from './item';
export class Thing {
  statusInfo: StatusInfo;
  label: string;
  bridgeUID: string;
  UID: string;
  thingTypeUID: string;
  location: string;
  channels: Channel[];
  items = [];
  editableItems = [];
}

export class StatusInfo {
  status: string;
  statusDetail: string;
}

export class Channel {
  linkedItems: string[];
  uid: string;
  label: string;
}
