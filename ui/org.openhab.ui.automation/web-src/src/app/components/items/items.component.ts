import { Module } from '../../models/module';
import { Item } from '../../models/item';
import { Thing } from '../../models/thing';
import { ItemService } from '../../services/item.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ThingService } from '../../services/thing.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css'],
  providers: [ItemService, ThingService]
})
export class ItemsComponent implements OnInit {
titleText = 'Welcome to the Automation UI for openHAB project!';
  items: Item[];
  things: Thing[];
  selectedItem: Item;

  ngOnInit(): void {
    this.itemService.getItems().subscribe(items => this.items = items);
    this.thingService.getThings().subscribe(things => this.things = things);
  }

  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService,
  private thingService: ThingService) { }

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
