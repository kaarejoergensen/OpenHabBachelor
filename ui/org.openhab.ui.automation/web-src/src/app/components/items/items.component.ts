import { Item } from '../../models/item';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModalComponent } from '../modal/modal.component';
import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css']
})
export class ItemsComponent implements OnInit {
  @Input() things: Thing[];
  @Input() thingType: string;
  @ViewChild('modal') conditionModal;
  selectedThings: Thing[];
  selectedThing: Thing;
  constructor(private sharedProperteis: SharedPropertiesService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selectedThings = [];
  }

  onSelect(thing: Thing): void {
    const index = this.selectedThings.indexOf(thing);
    if (index > -1) {
      this.selectedThings.splice(index, 1);
    } else {
      this.selectedThing = thing;
      this.conditionModal.show();
    }
  }

  addThingToSelected(thing: Thing): void {
    if (this.selectedThings.indexOf(thing) === -1) {
      this.selectedThings.push(thing);
    }
  }

  getModalHeader(): string {
    if (this.thingType === 'condition') {
      return 'Create condition';
    } else {
      return 'Create action';
    }
  }

//  openDialog(): void {
//    const dialogRef = this.dialog.open(ModalComponent);
//
//    dialogRef.afterClosed().subscribe(result => {
//      if (this.selectedThings.indexOf(result) === -1) {
//        this.selectedThings.push(result);
//      }
//    });
//  }
}
