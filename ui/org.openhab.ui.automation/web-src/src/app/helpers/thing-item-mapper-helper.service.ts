import {ItemModel} from '../models/item.model';
import {RuleModel} from '../models/rule.model';
import {ThingModel} from '../models/thing.model';
import {Injectable} from '@angular/core';

@Injectable()
export class ThingItemMapperHelperService {

  static addItemsToThings(items: ItemModel[], things: ThingModel[]): ThingModel[] {
    const thingsWithItems = [];
    for (const thing of things) {
      if (thing.channels && thing.channels.length > 0) {
        for (const channel of thing.channels) {
          if (channel.linkedItems && channel.linkedItems.length > 0) {
            for (const linkedItem of channel.linkedItems) {
              for (const item of items) {
                if (linkedItem === item.name) {
                  item.thingName = linkedItem;
                  if (thing.items !== undefined) {
                    thing.items.push(item);
                  } else {
                    thing['items'] = [];
                    thing.items.push(item);
                  }
                  if (!item.stateDescription || !item.stateDescription.readOnly) {
                    if (thing.editableItems !== undefined) {
                      thing.editableItems.push(item);
                    } else {
                      thing['editableItems'] = [];
                      thing.editableItems.push(item);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return things.filter(t => t.items !== undefined && t.items.length > 0);
  }

  static addThingToRule(rule: RuleModel, things: ThingModel[]): void {
    const modules = rule.events.concat(rule.actions, rule.conditions);
    for (const mod of modules) {
      if (mod.unsupportedModule) {
        console.log('addThingToRule mod unsupported, not adding thing: ' + mod.id + ' ' + mod.label);
        continue;
      }
      if (!mod.itemName) {
        console.log('addThingToRule mod itemName null, adding CustomTime: ' + JSON.stringify(mod));
      }
      const filteredThings = things.filter(t => t.items.filter(i => i.name === mod.itemName).length > 0);
      if (filteredThings.length > 0) {
        mod.thing = filteredThings[0];
      } else {
        console.log('no thing found for module ' + JSON.stringify(mod));
      }
    }
  }

  static createTimeThing(): ThingModel {
    const thing = new ThingModel();
    thing.label = 'Time';
    const item = new ItemModel();
    item.type = 'CustomTime';
    item.label = 'time';
    thing.items = [item];
    return thing;
  }

  static isThingArray(arr: ThingModel[] | ItemModel[]): arr is ThingModel[] {
    return arr.length > 0 && (<ThingModel>arr[0]).statusInfo !== undefined;
  }

  static isItemArray(arr: ThingModel[] | ItemModel[]): arr is ItemModel[] {
    return arr.length > 0 && (<ItemModel>arr[0]).link !== undefined;
  }
}
