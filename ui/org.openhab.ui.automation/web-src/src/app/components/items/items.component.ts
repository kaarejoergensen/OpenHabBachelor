import {Item} from '../../models/item';
import {Rule, OPERATORS, RuleModule, EVENT_TYPE, OPERATORS_EVENT} from '../../models/rule';
import {Thing} from '../../models/thing';
import {SharedPropertiesService} from '../../services/shared-properties.service';
import {ModuleCreatorDialogComponent} from '../module-creator-dialog/module-creator-dialog.component';
import {Component, Input, ViewChild, Inject, ElementRef, EventEmitter, Output} from '@angular/core';
import {ViewContainerRef, ReflectiveInjector, ComponentFactoryResolver} from '@angular/core';
import {Injector, trigger, transition, style, animate, AfterViewInit, OnChanges, SimpleChanges} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';

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
  mod: RuleModule;
  item: Item;

  constructor(private sharedProperties: SharedPropertiesService, private dialog: MatDialog,
    private injector: Injector) {
    this.things = this.injector.get('things');
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

  getNumerText(): string {
    for (const operator of OPERATORS_EVENT) {
      if (operator.value === this.mod.operator) {
        return operator.name;
      }
    }
    return '';
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
  @ViewChild('dynamicComponentContainer', {read: ViewContainerRef}) dynamicComponentContainer: ViewContainerRef;
  @Output() modUpdated = new EventEmitter();
  @Output() modDeleted = new EventEmitter();
  buttonEnabled = false;
  constructor(private resolver: ComponentFactoryResolver) {}

  ngAfterViewInit(): void {
    this.loadComponents();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadComponents();
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
      if (this.moduleType === 'condition') {
        this.buttonEnabled = true;
        return;
      } else {
        this.newModule();
        return;
      }
    }
    for (const mod of this.mods) {
      if (this.currentComponents.filter(c => (<ItemsComponent>c.instance).mod === mod ||
        (mod.id && (<ItemsComponent>c.instance).mod.id === mod.id)).length > 0) {
        continue;
      }
      // Inputs need to be in the following format to be resolved properly
      const data = {things: this.things, mod: mod};
      const inputProviders = Object.keys(data).map((inputName) => ({provide: inputName, useValue: data[inputName]}));
      const resolvedInputs = ReflectiveInjector.resolve(inputProviders);

      const injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, this.dynamicComponentContainer.parentInjector);
      const factory = this.resolver.resolveComponentFactory(ItemsComponent);
      const componentRef = factory.create(injector);
      this.dynamicComponentContainer.insert(componentRef.hostView);
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
    this.buttonEnabled = this.currentComponents.filter(c => !(<ItemsComponent>c.instance).mod.id).length === 0 && this.moduleType !== 'event';
  }
}
