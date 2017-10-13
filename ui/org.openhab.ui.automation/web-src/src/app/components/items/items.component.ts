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
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css'],
  providers: [ItemService, ThingService]
})
export class ItemsComponent implements OnInit {
titleText = 'Welcome to the Automation UI for openHAB project!';
  items: Item[];
  editableItems: Item[];
  selectedItem: Item;
  test: Item | Thing;

  ngOnInit(): void {
    this.items = [];
    this.editableItems = [];
    this.getItemsAndThings();
    // (<Item>test).label UNION-TYPES
  }

  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService,
  private thingService: ThingService) { }

  getItemsAndThings(): void {
    this.itemService.getItems().subscribe(
      res => this.getThings(res),
      error => this.handleError(error)
    );
  }
  getThings(items: Item[]): void {
    this.thingService.getThings().subscribe(
      res => {
       this.items = this.setItemName(items, res);
       this.editableItems = this.sortItems(this.items);
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
        if (!item.stateDescription.readonly) {
          editableItems.push(item);
        }
      } else {
        editableItems.push(item);
      }
    }
    return editableItems;
  }

  onSelect(item: Item): void {
    this.selectedItem = item;
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
}
