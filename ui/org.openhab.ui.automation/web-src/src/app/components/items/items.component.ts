import { Item } from '../../models/item';
import { Rule } from '../../models/rule';
import { Thing } from '../../models/thing';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ModuleCreatorDialogComponent } from '../module-creator-dialog/module-creator-dialog.component';
import { Component, Input, ViewChild, Inject, ElementRef, EventEmitter, Output } from '@angular/core';
import { ViewContainerRef, ReflectiveInjector, ComponentFactoryResolver, Injector } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-items',
  templateUrl: './items.component.html',
  styleUrls: ['./items.component.css']
})
export class ItemsComponent {
  @Output() ruleUpdated = new EventEmitter();
  @ViewChild('modal') conditionModal;
  things: Thing[];
  thingType: string;
  mod: any;
  constructor(private sharedProperties: SharedPropertiesService, private dialog: MatDialog,
    private injector: Injector) {
    this.things = this.injector.get('things');
    this.thingType = this.injector.get('thingType');
    this.mod = this.injector.get('mod');
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
        this.ruleUpdated.emit(this.mod);
      }
    });
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
  currentComponent = [];

  @ViewChild('dynamicComponentContainer', { read: ViewContainerRef }) dynamicComponentContainer: ViewContainerRef;
  @Output() ruleUpdated = new EventEmitter();
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
    
    (<any>component.instance).ruleUpdated.subscribe(r => this.ruleUpdated.emit(r));
    this.currentComponent.push(component);
  }
  
  constructor(private resolver: ComponentFactoryResolver) { }
}
