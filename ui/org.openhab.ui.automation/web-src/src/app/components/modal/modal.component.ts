import { Item } from '../../models/item';
import { Module } from '../../models/module';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnChanges, Input, HostListener, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnChanges {
  @Input() thing: Thing;
  @Input() thingType: string;
  @Output() onSelected: EventEmitter<any> = new EventEmitter();
  public visible = false;
  public visibleAnimate = false;
  operators = [{name: 'greater than', value: '>'}, {name: 'equal to', value: '='}, {name: 'less than', value: '<'}];
  selectedOperator = this.operators[0];
  selectedItem: Item;
  constructor(private sharedProperties: SharedPropertiesService) { }

  ngOnChanges() {
    if (this.thing) {
      if (this.thingType === 'condition' && this.thing.items) {
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
  save(state: string): void {
    if (this.isConditionValid()) {
      this.onSelected.emit();
      this.hide();
      const mod = new Module();
      if (this.thingType === 'condition') {
        mod.type = 'core.ItemStateCondition';
        mod.addConfiguration('itemName', this.selectedItem.name);
        mod.addConfiguration('operator', this.selectedOperator.value);
        mod.addConfiguration('state', state);
      } else {
        mod.type = 'core.ItemCommandAction';
        mod.addConfiguration('itemName', this.selectedItem.name);
        mod.addConfiguration('command', state);
      }
      this.sharedProperties.updateModule(this.thingType, mod);
    }
  }

  isConditionValid(): boolean {
    return true;
  }

  getModalBody(): string {
    if (this.thingType === 'condition') {
      switch (this.selectedItem.type) {
      case 'Number':
        return '';
    }
    }
    return '';
  }

  onOperatorChange(operator: any) {
    this.selectedOperator = operator;
  }

  onItemChange(item: any) {
    this.selectedItem = item;
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
