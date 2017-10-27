import { Item } from '../../models/item';
import { OPERATORS, SWITCH_STATES, DAYS, Rule, Condition, STATE_CONDITION_TYPE, TIME_CONDITION_TYPE, Action } from '../../models/rule';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, ViewChild, ElementRef, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-dialog-overview-example-dialog',
  templateUrl: 'module-creator-dialog.component.html',
  styleUrls: ['./module-creator-dialog.component.css']
})
export class ModuleCreatorDialogComponent {
  thing: Thing;
  selectedItem: Item;
  modalType: string;
  @ViewChild('stateInput') stateInput: ElementRef;
  @ViewChild('datePicker') datePicker: ElementRef;
  operators = OPERATORS;
  selectedOperator = this.operators[0];
  switchStates = SWITCH_STATES;
  selectedSwitchState = this.switchStates[0];
  days = DAYS;
  selectedDays = [];
  rule: Rule;
  constructor(private sharedProperties: SharedPropertiesService,
  public dialogRef: MatDialogRef<ModuleCreatorDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.thing = data.thing;
    this.modalType = data.modalType;
    this.rule = data.rule;
    if (this.modalType === 'condition' && this.thing.items) {
      this.selectedItem = this.thing.items[0];
    } else if (this.thing.editableItems) {
      this.selectedItem = this.thing.editableItems[0];
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

  save(): void {
    if (this.isConditionValid()) {
      if (this.modalType === 'condition') {
        const condition = new Condition();
        if (this.selectedItem.type !== 'CustomTime') {
          condition.type = STATE_CONDITION_TYPE;
          condition.itemName = this.selectedItem.name;
          if (this.selectedItem.type === 'Number') {
            condition.operator = this.selectedOperator.value;
            condition.state = this.stateInput.nativeElement.value;
          } else if (this.selectedItem.type === 'Switch') {
            condition.operator = '=';
            condition.state = this.selectedSwitchState.value;
          } else if (this.selectedItem.type === 'DateTime') {
            condition.operator = '=';
            const date = new Date(this.datePicker.nativeElement.value);
            const time = this.stateInput.nativeElement.value;
            const split = time.split(':');
            if (split.length === 2) {
              date.setHours(Number(split[0]), Number(split[1]));
            }
            let formattedDate = date.toISOString().slice(0, -1);
            if (this.selectedItem.state) {
                formattedDate += this.selectedItem.state.slice(-5);
            }
            condition.state = formattedDate;
          }
        } else {
          condition.type = TIME_CONDITION_TYPE;
          condition.days = this.selectedDays.map(function(d) {return d.value; } );
          condition.tempTime = this.stateInput.nativeElement.value;
        }
        this.rule.conditions.push(condition);
      } else {
        const action = new Action();
        action.itemName = this.selectedItem.name;
        if (this.selectedItem.type === 'Number') {
          action.command = this.stateInput.nativeElement.value;
        } else if (this.selectedItem.type === 'Switch') {
          action.command = this.selectedSwitchState.value;
        }
        this.rule.actions.push(action);
      }
      this.dialogRef.close({thing: this.thing, rule: this.rule});
    }
  }

  isConditionValid(): boolean {
    return true;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
