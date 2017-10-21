import { Item } from '../../models/item';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit, Input, ViewChild } from '@angular/core';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css']
})
export class ItemsComponent implements OnInit {
  @Input() items: Item[];
  @Input() itemType: string;
  @ViewChild('modal') conditionModal;
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
      this.selectedItem = item;
      this.conditionModal.show();
    }
  }

  addItemToSelected(item: Item): void {
    if (this.selectedItems.indexOf(item) === -1) {
      this.selectedItems.push(item);
    }
  }

  getModalHeader(): string {
    if (this.itemType === 'condition') {
      return 'Conditions modal';
    } else {
      return 'Actions modal';
    }
  }
}
