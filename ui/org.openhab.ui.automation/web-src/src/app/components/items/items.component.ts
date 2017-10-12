import { Module } from '../../models/module';
import { Item } from '../../models/item';
import { ItemService } from '../../services/item.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/concat';


@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css'],
  providers: [ItemService]
})
export class ItemsComponent implements OnInit {
titleText = 'Welcome to the Automation UI for openHAB project!';
  // items: Item[];
  items = [];
  editableItems = [];
  selectedItem: Item;
  things = [];

  getItems(): void {
    this.itemService.getThings().map(res => {
      this.things = res;
    }).concat(this.itemService.getItems())
    .subscribe(res => { if (res !== undefined) {
    this.setItemName(res);
    } });
  }


setItemName(items: Array<any>): void {
const itemsWithNames = [];
this.things.forEach(function(thing){
if (thing.channels !== undefined && thing.channels.length > 0) {
thing.channels.forEach(function(channel){
channel.linkedItems.forEach(function(li){
  items.forEach(function(item){
if (li === item.name) {
const newItem = {name: thing.label, item: item as Item};
itemsWithNames.push(newItem);
}
  });
});
});
}
});
this.items = itemsWithNames;
this.sortItems(itemsWithNames);
}

  sortItems(items: Array<any>): any {
    const readOnlyFalse = [];
    items.forEach(function(element){
      if (element.item.stateDescription !== undefined) {
        if (element.item.stateDescription.readOnly === false) {
          readOnlyFalse.push(element);
        }
      } else if (element.item.stateDescription === undefined) {
      readOnlyFalse.push(element);
      }
    });
    this.editableItems = readOnlyFalse;
    console.log(this.editableItems);
  }

  ngOnInit(): void {
   // this.itemService.getItems().then(items => this.items = items);
    this.getItems();
  }
  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService) { }
  onSelect(item: Item): void {
    this.selectedItem = item;
  }
}
