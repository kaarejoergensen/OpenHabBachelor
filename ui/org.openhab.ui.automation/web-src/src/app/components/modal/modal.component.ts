import { Item } from '../../models/item';
import { Module } from '../../models/module';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnChanges, Input, HostListener, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';


@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnChanges {
  @Input() thing: Thing;
  @Input() modalType: string;
  @Output() onSelected: EventEmitter<any> = new EventEmitter();
  @ViewChild('stateInput') stateInput: ElementRef;
  @ViewChild('datePicker') datePicker: ElementRef;
  public visible = false;
  public visibleAnimate = false;
  operators = [{name: 'greater than', value: '>'}, {name: 'equal to', value: '='}, {name: 'less than', value: '<'}];
  selectedOperator = this.operators[0];
  switchStates = [{name: 'turned off', value: 'OFF'}, {name: 'turned on', value: 'ON'}];
  selectedSwitchState = this.switchStates[0];
  days = [{name: 'M', value: 'MON'}, {name: 'T', value: 'THU'}, {name: 'W', value: 'WED'},
  {name: 'T', value: 'THU'}, {name: 'F', value: 'FRI'}, {name: 'S', value: 'SAT'}, {name: 'S', value: 'SUN'}];
  selectedDays = [];
  selectedItem: Item;
  constructor(private sharedProperties: SharedPropertiesService) { }

  ngOnChanges() {
    if (this.thing) {
      if (this.modalType === 'condition' && this.thing.items) {
        this.selectedItem = this.thing.items[0];
      } else if (this.thing.editableItems) {
        this.selectedItem = this.thing.editableItems[0];
      }
    }
  }

  @HostListener('document:keyup', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.keyCode === 27 && this.visible) {
      this.hide();
    }
  }
  save(): void {
    if (this.isConditionValid()) {
      this.onSelected.emit();
      this.hide();
      const mod = new Module();
      if (this.modalType === 'condition') {
        if (this.selectedItem.type !== 'CustomTime') {
          mod.type = 'core.ItemStateCondition';
          mod.name = 'an item has a given state';
          mod.description = 'Compares the item state with the given value';
          mod.addConfiguration('itemName', this.selectedItem.name);
          if (this.selectedItem.type === 'Number') {
            mod.addConfiguration('operator', this.selectedOperator.value);
            mod.addConfiguration('state', this.stateInput.nativeElement.value);
          } else if (this.selectedItem.type === 'Switch') {
            mod.addConfiguration('operator', '=');
            mod.addConfiguration('state', this.selectedSwitchState.value);
          } else if (this.selectedItem.type === 'DateTime') {
            mod.addConfiguration('operator', '=');
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
            mod.addConfiguration('state', formattedDate);
          }
        } else {
          mod.type = 'timer.DayOfWeekCondition';
          mod.name = 'it is a certain day of the week';
          mod.description = 'checks for the current day of the week';
          mod.addConfiguration('days', this.selectedDays.map(function(d) {return d.value; } ));
          mod.addConfiguration('tempTime', this.stateInput.nativeElement.value);
        }
      } else {
        mod.type = 'core.ItemCommandAction';
        mod.name = 'send a command';
        mod.description = 'Sends a command to a specified item.';
        mod.addConfiguration('itemName', this.selectedItem.name);
        if (this.selectedItem.type === 'Number') {
          mod.addConfiguration('command', this.stateInput.nativeElement.value);
        } else if (this.selectedItem.type === 'Switch') {
          mod.addConfiguration('command', this.selectedSwitchState.value);
        }
      }
      this.sharedProperties.updateModule(this.modalType, mod);
    }
  }

  isConditionValid(): boolean {
    return true;
  }

  getModalBody(): string {
    if (this.modalType === 'condition') {
      switch (this.selectedItem.type) {
      case 'Number':
        return '';
    }
    }
    return '';
  }

  onSelectDay(day: any) {
    const index = this.selectedDays.indexOf(day);
    if (index > -1) {
      this.selectedDays.splice(index, 1);
    } else {
      this.selectedDays.push(day);
    }
  }

  public show(): void {
    this.visible = true;
    setTimeout(() => this.visibleAnimate = true, 100);
  }

  public hide(): void {
    this.visibleAnimate = false;
    setTimeout(() => this.visible = false, 300);
  }

  public onContainerClicked(event: MouseEvent): void {
    if ((<HTMLElement>event.target).classList.contains('modal')) {
      this.hide();
    }
  }
}
