import { Item } from '../../models/item';
import { Rule, OPERATORS, RuleModule } from '../../models/rule';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModuleCreatorDialogComponent } from '../module-creator-dialog/module-creator-dialog.component';
import { Component, Input, ViewChild, Inject, ElementRef, EventEmitter, Output } from '@angular/core';
import { ViewContainerRef, ReflectiveInjector, ComponentFactoryResolver } from '@angular/core';
import { Injector, trigger, transition, style, animate, AfterViewInit, OnChanges, SimpleChanges } from '@angular/core';
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
export class ItemsComponent implements OnChanges {
  @Output() modUpdated = new EventEmitter();
  @Output() modDeleted = new EventEmitter();
  @ViewChild('modal') conditionModal;
  @Input() things: Thing[];
  @Input() mod: any;
  item: Item;
  
  constructor(private sharedProperties: SharedPropertiesService, private dialog: MatDialog) {
        if (this.mod && this.mod.itemName && this.mod.thing) {
      this.item = this.getItem(this.mod.thing, this.mod.itemName);
    }
    
  }
  
  ngOnChanges(changes: SimpleChanges): void {
    console.log('ITEMCHANGE');
  }
  

  onSelect(thing: Thing): void {
    this.openDialog(thing);
  }

  openDialog(selectedThing: Thing): void {
    const dialogRef = this.dialog.open(ModuleCreatorDialogComponent, {
      data: {thing: selectedThing, mod: this.mod}
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
    const newMod = new RuleModule();
    newMod.type = this.mod.type;
    this.mod = newMod;
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
    <div>
      <a *ngIf="buttonEnabled" [@fadeIn] mat-icon-button color="green" (click)="newModule()" style="float: left">
        <mat-icon style="color: green;">add</mat-icon>
        <span style="color: green;">New {{moduleType}}</span>
      </a>
    </div>
  `,
  animations: [
  trigger('fadeIn', [
    transition(':enter', [
      style({opacity: 0}),
      animate(500, style({opacity: 1})) 
    ])
  ])
]
})
export class DynamicComponent implements AfterViewInit, OnChanges {
  currentComponents = [];

  @Input() mods: RuleModule[];
  @Input() things: Thing[];
  @Input() moduleType: string;
  @Input() changeDetected: number;
  @ViewChild('dynamicComponentContainer', { read: ViewContainerRef }) dynamicComponentContainer: ViewContainerRef;
  @Output() modUpdated = new EventEmitter();
  @Output() modDeleted = new EventEmitter();
  buttonEnabled = false;
  constructor(private resolver: ComponentFactoryResolver) { }
  
  ngAfterViewInit(): void {
    this.loadComponents();
  }
  
  ngOnChanges(changes: SimpleChanges): void {
    console.log('Changes');
//    this.loadComponents();
  }
  
  newModule(): void {
    this.mods = this.mods.filter(m => m.id);
    const mod = new RuleModule();
    mod.type = this.moduleType;
    this.mods.push(mod);
    this.buttonEnabled = false;
    this.loadComponents();
  }
  
  loadComponents(): void {
    if (this.mods.length === 0) {
      this.newModule();
      return;
    }
//    for (const component of this.currentComponents) {
//      const index = this.currentComponents.indexOf(component);
//      this.currentComponents.splice(index, 1);
//      component.destroy();
//    }
    for (const mod of this.mods) {
      const factory = this.resolver.resolveComponentFactory(ItemsComponent);
      const componentRef = this.dynamicComponentContainer.createComponent(factory);
      this.dynamicComponentContainer.insert(componentRef.hostView);
      (<ItemsComponent>componentRef.instance).things = this.things;
      (<ItemsComponent>componentRef.instance).mod = mod;
      (<ItemsComponent>componentRef.instance).modUpdated.subscribe(r => {
        if (!r.id && this.moduleType !== 'event') {
          this.buttonEnabled = true;
        }
        this.modUpdated.emit(r);
      });
      (<ItemsComponent>componentRef.instance).modDeleted.subscribe(r => {
        this.modDeleted.emit(r);
        if (this.currentComponents.length > 1) {
          const index = this.currentComponents.indexOf(componentRef);
          this.currentComponents.splice(index, 1);
          componentRef.destroy();
        }
      });
      this.currentComponents.push(componentRef);
    }
  }
}
