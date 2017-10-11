import { Item } from '../../models/item';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-item-detail',
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.css']
})
export class ItemDetailComponent {
  @Input() item: Item;
  constructor(private sharedProperties: SharedPropertiesService) {}
}
