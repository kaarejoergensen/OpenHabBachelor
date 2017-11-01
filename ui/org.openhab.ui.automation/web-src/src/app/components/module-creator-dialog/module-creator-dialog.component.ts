import {Item} from '../../models/item';
import {OPERATORS, SWITCH_STATES, DAYS, Rule, Condition, STATE_CONDITION_TYPE, TIME_CONDITION_TYPE, Action} from '../../models/rule';
import {Thing} from '../../models/thing';
import {SharedPropertiesService} from '../../services/shared-properties.service';
import {DatePipe} from '@angular/common';
import {Component, ViewChild, ElementRef, Inject, AfterViewInit, ChangeDetectorRef} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-dialog-overview-example-dialog',
  templateUrl: 'module-creator-dialog.component.html',
  styleUrls: ['./module-creator-dialog.component.css']
})
export class ModuleCreatorDialogComponent implements AfterViewInit {
  thing: Thing;
  selectedItem: Item;
  modalType: string;
  @ViewChild('datePicker') datePicker: ElementRef;
  operators = OPERATORS;
  selectedOperator = this.operators[0];
  switchStates = SWITCH_STATES;
  selectedSwitchState = this.switchStates[0];
  days = DAYS;
  selectedDays = [];
  stateInput = '';
  mod: any;
  constructor(private sharedProperties: SharedPropertiesService, private cdRef: ChangeDetectorRef,
    public dialogRef: MatDialogRef<ModuleCreatorDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.thing = data.thing;
    this.modalType = data.modalType;
    this.mod = data.mod;
    if (this.modalType === 'condition' && this.thing.items) {
      this.selectedItem = this.thing.items[0];
    } else if (this.thing.editableItems) {
      this.selectedItem = this.thing.editableItems[0];
    }
  }

  ngAfterViewInit(): void {
    if (this.mod) {
      if (this.mod.itemName && this.mod.thing) {
        this.selectedItem = this.getItem(this.mod.thing, this.mod.itemName);
        if (this.modalType === 'condition') {
          if (this.mod.operator) {
            for (const op of this.operators) {
              if (op.value === this.mod.operator) {
                this.selectedOperator = op;
              }
            }
          }
          if (this.mod.state) {
            if (this.selectedItem.type === 'Number') {
              this.stateInput = this.mod.state;
            } else if (this.selectedItem.type === 'Switch') {
              for (const s of this.switchStates) {
                if (s.value === this.mod.state) {
                  this.selectedSwitchState = s;
                }
              }
            } else if (this.selectedItem.type === 'DateTime') {
              const date = new Date(this.mod.state);
              this.datePicker.nativeElement.value = new DatePipe('en-us').transform(date, 'MM/dd/yyyy');
              this.stateInput = date.toLocaleTimeString().replace(/\./gi, ':');
            }
          }
        } else if (this.modalType === 'action') {
          if (this.selectedItem.type === 'Number') {
            this.stateInput = this.mod.command;
          } else if (this.selectedItem.type === 'Switch') {
            for (const s of this.switchStates) {
              if (s.value === this.mod.command) {
                this.selectedSwitchState = s;
              }
            }
          }
        }
      } else if (this.mod.tempTime) {
        this.stateInput = this.mod.tempTime;
        for (const dayString of this.mod.days) {
          for (const day of this.days) {
            if (dayString === day.value) {
              this.selectedDays.push(day);
            }
          }
        }
      }
      this.cdRef.detectChanges();
    }
  }

  onSelectDay(day: any) {
    const index = this.selectedDays.indexOf(day);
    if (index > -1) {
      this.selectedDays.splice(index, 1);
    } else {
      this.selectedDays.push(day);
    }
  }

  getItem(thing: Thing, itemName: string): Item {
    if (!thing.items || thing.items.length === 0) {
      return null;
    }
    for (const item of thing.items) {
      if (item.name === itemName) {
        return item;
      }
    }
    return null;
  }

  save(): void {
    if (this.isConditionValid()) {
      if (this.modalType === 'condition') {
        const condition = new Condition();
        condition.thing = this.thing;
        if (this.selectedItem.type !== 'CustomTime') {
          condition.type = STATE_CONDITION_TYPE;
          condition.itemName = this.selectedItem.name;
          if (this.selectedItem.type === 'Number') {
            condition.operator = this.selectedOperator.value;
            condition.state = this.stateInput;
          } else if (this.selectedItem.type === 'Switch') {
            condition.operator = '=';
            condition.state = this.selectedSwitchState.value;
          } else if (this.selectedItem.type === 'DateTime') {
            condition.operator = '=';
            const date = new Date(this.datePicker.nativeElement.value);
            const time = this.stateInput;
            const split = time.split(':');
            if (split.length >= 2) {
              date.setHours(Number(split[0]), Number(split[1]));
            }
            let formattedDate = new DatePipe('en-us').transform(date, 'yyyy-MM-ddTHH:mm:ss.000');
            if (this.selectedItem.state) {
              formattedDate += this.selectedItem.state.slice(-5);
            }
            condition.state = formattedDate;
          }
        } else {
          condition.type = TIME_CONDITION_TYPE;
          condition.days = this.selectedDays.map(function(d) {return d.value; });
          condition.tempTime = this.stateInput;
        }
        this.mod = condition;
      } else {
        const action = new Action();
        action.thing = this.thing;
        action.itemName = this.selectedItem.name;
        if (this.selectedItem.type === 'Number') {
          action.command = this.stateInput;
        } else if (this.selectedItem.type === 'Switch') {
          action.command = this.selectedSwitchState.value;
        }
        this.mod = action;
      }
      this.dialogRef.close({thing: this.thing, mod: this.mod});
    }
  }

  isConditionValid(): boolean {
    return true;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
