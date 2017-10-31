import { Item } from '../../models/item';
import { Rule, OPERATORS } from '../../models/rule';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModuleCreatorDialogComponent } from '../module-creator-dialog/module-creator-dialog.component';
import { Component, Input, ViewChild, Inject, ElementRef, EventEmitter, Output } from '@angular/core';
import { ViewContainerRef, ReflectiveInjector, ComponentFactoryResolver } from '@angular/core';
import { Injector, trigger, transition, style, animate } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css'],
  animations: [
  trigger('fadeIn', [
    transition(':enter', [   // :enter is alias to 'void => *'
      style({opacity: 0}),
      animate(500, style({opacity: 1})) 
    ])
  ])
]
})
export class ItemsComponent {
  @Output() modUpdated = new EventEmitter();
  @Output() modDeleted = new EventEmitter();
  @ViewChild('modal') conditionModal;
  things: Thing[];
  thingType: string;
  mod: any;
  item: Item;
  
  constructor(private sharedProperties: SharedPropertiesService, private dialog: MatDialog,
    private injector: Injector) {
    this.things = this.injector.get('things');
    this.thingType = this.injector.get('thingType');
    this.mod = this.injector.get('mod');
    if (this.mod && this.mod.itemName && this.mod.thing) {
      this.item = this.getItem(this.mod.thing, this.mod.itemName);
    }
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
        this.mod = result.mod;
        if (this.mod && this.mod.itemName && this.mod.thing) {
          this.item = this.getItem(this.mod.thing, this.mod.itemName);
        }
        this.modUpdated.emit(this.mod);
      }
    });
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
  
  getOperator(operator: string): string {
    for (const op of OPERATORS) {
      if (op.value === operator) {
        return op.name;
      }
    }
  }
  
  onDeleteMod(): void {
    this.modDeleted.emit(this.mod);
    this.mod = null;
  }
  
  parseState(state: string): string {
    if (this.item && this.item.type === 'DateTime') {
      return new Date(state).toLocaleString();
    }
    return state;
  }
  
  parseDays(days: string[]): string {
    let result = '';
    for (let i = 0; i < days.length - 2; i++) {
      const day = days[i];
      result += this.capitalize(day) + ', ';
    }
    if (days.length > 1) {
      result += this.capitalize(days[days.length - 2]) + ' and ' + this.capitalize(days[days.length - 1]);
    } else {
      if (days.length > 0) {
        result = this.capitalize(days[0]);
      }
    }
    
    return result;
  }
  
  capitalize(s: string): string {
      return s.length > 0 ? (s.length > 1 ? s.charAt(0) + s.slice(1).toLowerCase() : s.charAt(0)) : s;
  }
}

@Component({
  selector: 'app-dynamic-component',
  entryComponents: [ItemsComponent],
  template: `
    <div #dynamicComponentContainer></div>
  `,
})
export class DynamicComponent {
  currentComponents = [];

  @ViewChild('dynamicComponentContainer', { read: ViewContainerRef }) dynamicComponentContainer: ViewContainerRef;
  @Output() modUpdated = new EventEmitter();
  @Output() modDeleted = new EventEmitter();
  @Input() set componentData(data: {component: any, inputs: any }) {
    if (!data) {
      return;
    }
    const inputProviders = Object.keys(data.inputs).map((inputName) => ({provide: inputName, useValue: data.inputs[inputName]}));
    const resolvedInputs = ReflectiveInjector.resolve(inputProviders);

    const injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, this.dynamicComponentContainer.parentInjector);

    const factory = this.resolver.resolveComponentFactory(data.component);

    const component = factory.create(injector);

    this.dynamicComponentContainer.insert(component.hostView);
    
    (<any>component.instance).modUpdated.subscribe(r => this.modUpdated.emit(r));
    (<any>component.instance).modDeleted.subscribe(r => {
      this.modDeleted.emit(r);
      if (this.currentComponents.length > 1) {
        const index = this.currentComponents.indexOf(component);
        this.currentComponents.splice(index, 1);
        component.destroy();
      }
    });
    this.currentComponents.push(component);
  }
  
  constructor(private resolver: ComponentFactoryResolver) { }
}
