import { Item } from '../../models/item';
import { Module } from '../../models/module';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit, Input, HostListener, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnInit {
  @Input() item: Item;
  @Input() itemType: string;
  @Output() onSelected: EventEmitter<any> = new EventEmitter();
  public visible = false;
  public visibleAnimate = false;
  operators = ['>', '=', '<'];
  selectedOperator = this.operators[0];
  constructor(private sharedProperties: SharedPropertiesService) { }

  ngOnInit() {
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
      mod.type = 'core.ItemStateCondition';
      mod.addConfiguration('itemName', this.item.name);
      mod.addConfiguration('operator', this.selectedOperator);
      mod.addConfiguration('state', state);
      this.sharedProperties.updateModule(this.itemType, mod);
    }
  }

  isConditionValid(): boolean {
    return true;
  }

  getModalBody(): string {
    if (this.itemType === 'condition') {
      switch (this.item.type) {
      case 'Number':
        return '';
    }
    }
    return '';
  }

  onOperatorChange(operator: string) {
    this.selectedOperator = operator;
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
