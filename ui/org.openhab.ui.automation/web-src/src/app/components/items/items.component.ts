import { Item } from '../../models/item';
import { Module } from '../../models/module';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModuleCreatorDialogComponent } from '../module-creator-dialog/module-creator-dialog.component';
import { Component, OnInit, Input, ViewChild, Inject, ElementRef } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

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
  constructor(private sharedProperties: SharedPropertiesService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selectedThings = [];
  }

  onSelect(thing: Thing): void {
    const index = this.selectedThings.indexOf(thing);
    if (index > -1) {
      this.selectedThings.splice(index, 1);
    } else {
      this.selectedThing = thing;
      this.openDialog();
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

  openDialog(): void {
    const dialogRef = this.dialog.open(ModuleCreatorDialogComponent, {
      data: {thing: this.selectedThing, modalType: this.thingType}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (this.selectedThings.indexOf(result) === -1) {
        this.selectedThings.push(result);
      }
    });
  }
}
