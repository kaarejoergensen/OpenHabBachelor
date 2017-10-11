import { Module } from '../../models/module';
import { Item } from '../../models/item';
import { ItemService } from '../../services/item.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css'],
  providers: [ItemService]
})
export class ItemsComponent implements OnInit {
titleText = 'Welcome to the Automation UI for openHAB project!';
  items: Item[];
  selectedItem: Item;
  ngOnInit(): void {
    this.itemService.getItems().then(items => this.items = items);
  }
  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService) { }
  onSelect(item: Item): void {
    this.selectedItem = item;
  }
}
