import {Item} from '../../models/item';
import {OPERATORS, SWITCH_STATES, DAYS, Rule, RuleModule, CONDITION_TYPE, ACTION_TYPE, EVENT_TYPE } from '../../models/rule';
import {Thing} from '../../models/thing';
import {SharedPropertiesService} from '../../services/shared-properties.service';
import {DatePipe, PercentPipe } from '@angular/common';
import {Component, ViewChild, ElementRef, Inject, AfterViewInit, ChangeDetectorRef} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import { Validators, FormControl } from '@angular/forms';

@Component({
  selector: 'app-dialog-overview-example-dialog',
  templateUrl: 'module-creator-dialog.component.html',
  styleUrls: ['./module-creator-dialog.component.css']
})
export class ModuleCreatorDialogComponent implements AfterViewInit {
  rateControl: any; 
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
  mod: RuleModule;
  
  constructor(private sharedProperties: SharedPropertiesService, private cdRef: ChangeDetectorRef,
    public dialogRef: MatDialogRef<ModuleCreatorDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.thing = data.thing;
    this.mod = data.mod;
    this.modalType = this.mod.type;
    
    if ((this.modalType === CONDITION_TYPE || this.modalType === EVENT_TYPE) && this.thing.items) {
      this.selectedItem = this.thing.items[0];
    } else if (this.thing.editableItems) {
      this.selectedItem = this.thing.editableItems[0];
    }
   if (this.selectedItem.type  && this.selectedItem.stateDescription && this.selectedItem.stateDescription.minimum && this.selectedItem.stateDescription.maximum) {
        this.rateControl = new FormControl('', [Validators.min(this.selectedItem.stateDescription.minimum), Validators.max(this.selectedItem.stateDescription.maximum), Validators.required]);
     } else if (this.selectedItem.stateDescription && this.selectedItem.stateDescription.pattern && this.selectedItem.stateDescription.pattern.split(' ').pop().startsWith('%')) {
       this.rateControl = new FormControl('', [Validators.min(0), Validators.max(100), Validators.required]);
     } else {
     this.rateControl = new FormControl('', Validators.required);
     } 
   
  }

  ngAfterViewInit(): void {
    if (this.mod) {
      if (this.mod.itemName && this.mod.thing) {
        console.log('pixxa');
        this.selectedItem = this.getItem(this.mod.thing, this.mod.itemName);
        if (this.modalType === 'condition' || this.modalType ===  'event') {
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
//      } else if (this.mod.tempTime) {
//        this.stateInput = this.mod.tempTime;
//        for (const dayString of this.mod.days) {
//          for (const day of this.days) {
//            if (dayString === day.value) {
//              this.selectedDays.push(day);
//            }
//          }
//        }
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
      const mod = new RuleModule();
      if (this.modalType === 'event') {   
        mod.type = EVENT_TYPE;
        mod.thing = this.thing;
        if (this.selectedItem.type !== 'CustomTime') {
          mod.itemName = this.selectedItem.name;
          if (this.selectedItem.type === 'Number') {
            mod.operator = this.selectedOperator.value;
            mod.state = this.stateInput;
          } else if (this.selectedItem.type === 'Switch') {
            mod.operator = '=';
            mod.state = this.selectedSwitchState.value;
          } else if (this.selectedItem.type === 'DateTime') {
            mod.operator = '=';
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
            mod.state = formattedDate;
          }
        
        } else {
          mod.days = this.selectedDays.map(function(d) {return d.value; });
        }
        }else if (this.modalType === 'condition') {
          mod.type = CONDITION_TYPE;
        mod.thing = this.thing;
        if (this.selectedItem.type !== 'CustomTime') {
          mod.itemName = this.selectedItem.name;
          if (this.selectedItem.type === 'Number') {
            mod.operator = '=';
            mod.state = this.stateInput;
          } else if (this.selectedItem.type === 'Switch') {
            mod.operator = '=';
            mod.state = this.selectedSwitchState.value;
          } else if (this.selectedItem.type === 'DateTime') {
            mod.operator = '=';
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
            mod.state = formattedDate;
          }
          }
        } else if (this.modalType === 'action') {
        mod.type = ACTION_TYPE;
        mod.thing = this.thing;
        mod.itemName = this.selectedItem.name;
        if (this.selectedItem.type === 'Number') {
          mod.command = this.stateInput;
        } else if (this.selectedItem.type === 'Switch') {
          mod.command = this.selectedSwitchState.value;
        }
      }
      if (this.mod.id) {
        mod.id = this.mod.id;
      }
      this.mod = mod;
      this.dialogRef.close({thing: this.thing, mod: this.mod});
    }
}
  onChange(): void {
  if (this.selectedItem.type  && this.selectedItem.stateDescription && this.selectedItem.stateDescription.minimum && this.selectedItem.stateDescription.maximum) {
        this.rateControl = new FormControl([Validators.min(this.selectedItem.stateDescription.minimum), Validators.max(this.selectedItem.stateDescription.maximum), Validators.required]);
        console.log(this.rateControl);
     } else if (this.selectedItem.stateDescription && this.selectedItem.stateDescription.pattern && this.selectedItem.stateDescription.pattern.split(' ').pop().startsWith('%')) {
       this.rateControl = new FormControl('', [Validators.min(0), Validators.max(100), Validators.required]);
        console.log(this.rateControl);
  } else {
     this.rateControl = new FormControl(Validators.required);
     }
  }


  isConditionValid(): boolean {
    return true;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
