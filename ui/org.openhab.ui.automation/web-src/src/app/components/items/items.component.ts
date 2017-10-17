import { Item } from '../../models/item';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css']
})
export class ItemsComponent implements OnInit {
  @Input() items: Item[];
  selectedItems: Item[];
  selectedItem: Item;
  constructor(private sharedProperteis: SharedPropertiesService) { }

  ngOnInit() {
    this.selectedItems = [];
  }

  onSelect(item: Item): void {
    const index = this.selectedItems.indexOf(item);
    if (index > -1) {
      this.selectedItems.splice(index, 1);
    } else {
      this.selectedItems.push(item);
    }
    this.selectedItem = item;
  }
}
