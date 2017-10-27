import { Item } from '../../models/item';
import { Rule } from '../../models/rule';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModuleCreatorDialogComponent } from '../module-creator-dialog/module-creator-dialog.component';
import { Component, OnInit, Input, ViewChild, Inject, ElementRef, EventEmitter, Output } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css']
})
export class ItemsComponent implements OnInit {
  @Input() things: Thing[];
  @Input() thingType: string;
  @Input() mod: any;
  @Output() ruleUpdated = new EventEmitter();
  @ViewChild('modal') conditionModal;
  selectedThingsUIDS: string[];
  constructor(private sharedProperties: SharedPropertiesService, private dialog: MatDialog) { }

  ngOnInit() {
    this.selectedThingsUIDS = [];
  }

  onSelect(thing: Thing): void {
    this.openDialog(thing);
  }

  openDialog(selectedThing: Thing): void {
    const dialogRef = this.dialog.open(ModuleCreatorDialogComponent, {
      data: {thing: selectedThing, modalType: this.thingType, mod: this.mod}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.selectedThingsUIDS.indexOf(result.thing.UID) === -1) {
          this.selectedThingsUIDS.push(result.thing.UID);
        }
        this.mod = result.mod;
        this.ruleUpdated.emit(this.mod);
      }
    });
  }
}
