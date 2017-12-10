import { ItemModel } from './item.model';

export class ThingModel {
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
