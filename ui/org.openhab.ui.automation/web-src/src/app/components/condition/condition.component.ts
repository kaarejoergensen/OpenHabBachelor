import { Item } from '../../models/item';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit, Input, HostListener, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-condition',
  templateUrl: './condition.component.html',
  styleUrls: ['./condition.component.css']
})
export class ConditionComponent implements OnInit {
  @Input() item: Item;
  @Output() onSelected: EventEmitter<any> = new EventEmitter();
  public visible = false;
  public visibleAnimate = false;
  constructor(private sharedProperties: SharedPropertiesService) { }

  ngOnInit() {
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
    }
  }

  isConditionValid(): boolean {
    return true;
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
