import { Module } from '../../models/module';
import { Item } from '../../models/item';
import { Thing } from '../../models/thing';
import { ItemService } from '../../services/item.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ThingService } from '../../services/thing.service';
import { Component, OnInit } from '@angular/core';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/concat';


@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css'],
  providers: [ItemService, ThingService]
})
export class CreateComponent implements OnInit {
titleText = 'Welcome to the Automation UI for openHAB project!';
  items: Item[];
  editableItems: Item[];
  isLoading = true;
  isConditionsChosen = false;

  ngOnInit(): void {
    this.items = [];
    this.editableItems = [];
    this.getItemsAndThings();
  }

  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService,
  private thingService: ThingService) { }

  getItemsAndThings(): void {
    this.isLoading = true;
    let items = [];
    let things = [];
    this.itemService.getItems()
    .concat(this.thingService.getThings())
    .subscribe(res => {
      if (this.isThingArray(res)) {
        console.log('Fetched ' + res.length + ' things');
        things = res;
      } else if (this.isItemArray(res)) {
        console.log('Fetched ' + res.length + ' items');
        items = res;
      }
      if (things.length > 0 && items.length > 0) {
        this.items = this.setItemName(items, things);
        this.editableItems = this.sortItems(this.items);
        this.isLoading = false;
        console.log(this.items.length + ' items with names');
        console.log(this.editableItems.length + ' editable items');
      }
    },
    error => this.handleError(error));
  }

  handleError(error: any): void {
    console.log('Error! ', error);
  }

  setItemName(items: Item[], things: Thing[]): Item[] {
    const itemsWithNames = [];
    for (const thing of things) {
      if (thing.channels && thing.channels.length > 0) {
        for (const channel of thing.channels) {
          if (channel.linkedItems && channel.linkedItems.length > 0) {
            for (const linkedItem of channel.linkedItems) {
              for (const item of items) {
                if (linkedItem === item.name) {
                  item.thingName = thing.label;
                  itemsWithNames.push(item);
                }
              }
            }
          }
        }
      }
    }
    return itemsWithNames;
  }

  sortItems(items: Item[]): Item[] {
    const editableItems = [];
    for (const item of items) {
      if (item.stateDescription) {
        if (!item.stateDescription.readOnly) {
          editableItems.push(item);
        }
      } else {
        editableItems.push(item);
      }
    }
    return editableItems;
  }

  next(): void {
    this.isConditionsChosen = true;
  }

  createRule(): void {
    const body = this.getRuleJson(null);
    console.log('Not implemented');
  }

  getRuleJson(uid: string): any {
    return {
      tags: [],
      conditions: this.sharedProperties.getModuleJSON('condition'),
      description: 'Description',
      name: 'Name',
      triggers: this.sharedProperties.getModuleJSON('trigger'),
      configDescriptions: [],
      actions: this.sharedProperties.getModuleJSON('action'),
      uid: uid
    };
  }

  isThingArray(arr: Thing[] | Item[]): arr is Thing[] {
    return arr.length > 0 && (<Thing>arr[0]).statusInfo !== undefined;
  }

  isItemArray(arr: Thing[] | Item[]): arr is Item[] {
    return arr.length > 0 && (<Item>arr[0]).link !== undefined;
  }

  goBack(): void {
    this.isConditionsChosen = false;
  }
}
