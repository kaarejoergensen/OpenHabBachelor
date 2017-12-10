
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleMapperHelperService } from './rule-mapper-helper.service';
import {RuleModel, RuleModelModule} from '../models/rule.model'; 
import {RuleDTO, Module} from '../models/rule-do.model'; 

describe('Service: RuleMapperHelperService', () => {
  let service: RuleMapperHelperService;
  let fixture: ComponentFixture<RuleMapperHelperService>;
  const ruleDTO = new RuleDTO();
  const unsupported = new Module();
  
  //TESTDATA 
   const eventModule: RuleModelModule = {
      id: '1',
      type: 'event',
      label: 'an item raises above/drops below a value',
      // Common
      itemName: 'testItem',
      thing: undefined,
      operator: '=',
      state: '5',
      // Event
      time: undefined,
      // Condition
      days: [  ],
      // Action
      command: undefined,
      // Unsupported
      unsupportedModule: undefined
    }
  
  
  const conditionModule: RuleModelModule = {
      id: '2',
      type: 'condition',
      label: 'an item has a given state',
      // Common
      itemName: 'testItem',
      thing: undefined,
      operator: '>',
      state: '5.0',
      // Event
      time: undefined,
      // Condition
      days: [ ],
      // Action
      command: undefined,
      // Unsupported
      unsupportedModule: undefined
    }
  
  const actionModule: RuleModelModule = {
      id: '3',
      type: 'action',
      label: 'send a command',
      // Common
      itemName: 'testItem',
      thing: undefined,
      operator: undefined,
      state: '5.0',
      // Event
      time: undefined,
      // Condition
      days: [ ],
      // Action
      command: '5.0',
      // Unsupported
      unsupportedModule: undefined
    }
 
  const testRule: RuleModel = {
      uid: '1.1',
      enabled: true,
      name: 'testRule',
      description: 'testRule',
      events: [eventModule],
      conditions: [conditionModule],
      actions: [actionModule]
  }
  
  const openHABEvent: Module = {
    configuration: {
    createdIn:'AutomationUI',
    itemName: 'testItem', 
    state: '5',
    operator: '='
        },
    id: '1',
    label: 'an item raises above/drops below a value',
    type: 'ItemCommandAboveBelowTrigger',
    description: 'This triggers the rule if the item raises above/drops below a certain value.'

    }
   
  const openHABCondition: Module = {
    id: '2',
    type: 'core.ItemStateCondition',
    description: 'Compares the item state with the given value',
    label: 'an item has a given state',
    configuration: {
    createdIn:'AutomationUI',
    itemName: 'testItem', 
    state: '5.0',
    operator: '>'
    }
  }
  
    const openHABAction: Module = {
    id: '3',
    type: 'core.ItemCommandAction',
    label: 'send a command',
    description:'Sends a command to a specified item.',
    configuration: {
    createdIn: 'AutomationUI',
    itemName: 'testItem', 
    command: '5.0'
    }
  }
 
  const openHABRule: RuleDTO = {
    enabled: true,
    uid: '1.1',
    name: 'testRule',
    description: 'testRule',
    triggers: [openHABEvent],
    conditions: [openHABCondition],
    actions: [openHABAction],
    getJSON: {tags: [], name: 'testRule', description: 'testRule', configDescriptions: [], uid:'1.1',
    conditions: [{
    type: 'core.ItemStateCondition',
    'description': 'Compares the item state with the given value',
    label: 'an item has a given state',
    id: '2',
    configuration: {
    createdIn:'AutomationUI',
    itemName: 'testItem', 
    state: '5.0',
    operator: '>'
      }
     }], 
    triggers: [{
    id: '1',
    label: 'an item raises above/drops below a value',
    type: 'ItemCommandAboveBelowTrigger',
    'description': 'This triggers the rule if the item raises above/drops below a certain value.',
    configuration: {
    createdIn:'AutomationUI',
    itemName: 'testItem', 
    state: '5',
    operator: '='
        }
    }], 
    actions: [{
    id: '3',
    type: 'core.ItemCommandAction',
    label: 'send a command',
    'description':'Sends a command to a specified item.',
    configuration: {
    createdIn: 'AutomationUI',
    itemName: 'testItem', 
    command: '5.0'
    }
  }]
  } as any
}
  

    beforeEach(() => {
        service = new RuleMapperHelperService();
    
    });
 
  it('mapModuleToEvent should make object equal to eventModule', () => {
        let eventMod = new RuleModelModule();
        eventMod = RuleMapperHelperService.mapModuleToEvent(openHABEvent);
        expect((eventMod).id).toEqual(eventModule.id);
        expect((eventMod).type).toEqual(eventModule.type);
        expect((eventMod).type).toEqual(eventModule.type);
        expect((eventMod).label).toEqual(eventModule.label);
        expect((eventMod).itemName).toEqual(eventModule.itemName);
        expect((eventMod).operator).toEqual(eventModule.operator);
        expect((eventMod).state).toEqual(eventModule.state);
        expect((eventMod).time).toEqual(eventModule.time);
        expect((eventMod).days).toEqual(eventModule.days);
        expect((eventMod).unsupportedModule).toEqual(eventModule.unsupportedModule);
    
    });
  
    it('mapModuletoCondtion should make object equal to conditonModule', () => {
        let conditionMod = new RuleModelModule();
        conditionMod = RuleMapperHelperService.mapModuleToCondition(openHABCondition);
        expect((conditionMod).id).toEqual(conditionModule.id);
        expect((conditionMod).type).toEqual(conditionModule.type);
        expect((conditionMod).type).toEqual(conditionModule.type);
        expect((conditionMod).label).toEqual(conditionModule.label);
        expect((conditionMod).itemName).toEqual(conditionModule.itemName);
        expect((conditionMod).operator).toEqual(conditionModule.operator);
        expect((conditionMod).state).toEqual(conditionModule.state);
        expect((conditionMod).time).toEqual(conditionModule.time);
        expect((conditionMod).days).toEqual(conditionModule.days);
        expect((conditionMod).unsupportedModule).toEqual(conditionModule.unsupportedModule);
    });
  
      it('mapModuletoAction should make object equal to actionModule', () => {
        let actionMod = new RuleModelModule();
        actionMod = RuleMapperHelperService.mapModuleToAction(openHABAction);
        expect((actionMod).id).toEqual(actionModule.id);
        expect((actionMod).type).toEqual(actionModule.type);
        expect((actionMod).type).toEqual(actionModule.type);
        expect((actionMod).label).toEqual(actionModule.label);
        expect((actionMod).itemName).toEqual(actionModule.itemName);
        expect((actionMod).operator).toEqual(actionModule.operator);
        expect((actionMod).command).toEqual(actionModule.state);
        expect((actionMod).time).toEqual(actionModule.time);
        expect((actionMod).days).toEqual(actionModule.days);
        expect((actionMod).unsupportedModule).toEqual(actionModule.unsupportedModule);
    });
    
        it('mapEventToModule should make object equal to openHABEvent', () => {
        let eventMod = new Module();
        eventMod = RuleMapperHelperService.mapEventToModule(eventModule, ruleDTO);     
        expect((eventMod).id).toEqual(openHABEvent.id);
        expect((eventMod).label).toEqual(openHABEvent.label);
        expect((eventMod).description).toEqual(openHABEvent.description);
        expect((eventMod).type).toEqual(openHABEvent.type);
        expect((eventMod).configuration).toEqual(openHABEvent.configuration);         
    });
  
        it('mapConditonToModule should make object equal to openHABCondition', () => {
        let conditonMod = new Module();
        conditonMod = RuleMapperHelperService.mapConditionToModule(conditionModule);     
        expect((conditonMod).id).toEqual(openHABCondition.id);
        expect((conditonMod).label).toEqual(openHABCondition.label);
        expect((conditonMod).description).toEqual(openHABCondition.description);
        expect((conditonMod).type).toEqual(openHABCondition.type);
        expect((conditonMod).configuration).toEqual(openHABCondition.configuration);         
    });
   
        it('mapActionToModule should make object equal to openHABAction', () => {
        let actionMod = new Module();
        actionMod = RuleMapperHelperService.mapActionToModule(actionModule);     
        expect((actionMod).id).toEqual(openHABAction.id);
        expect((actionMod).label).toEqual(openHABAction.label);
        expect((actionMod).description).toEqual(openHABAction.description);
        expect((actionMod).type).toEqual(openHABAction.type);
        expect((actionMod).configuration).toEqual(openHABAction.configuration);         
    });
   
        it('mapDTORule should make object equal to testRule', () => {
        let ruleMod = new RuleModel();
        ruleMod = RuleMapperHelperService.mapDTOtoRule(openHABRule); 
           
        expect((ruleMod).uid).toEqual(testRule.uid);
        expect((ruleMod).enabled).toEqual(testRule.enabled);
        expect((ruleMod).name).toEqual(testRule.name);
        expect((ruleMod).description).toEqual(testRule.description);
        expect((ruleMod).events[0].id).toEqual(testRule.events[0].id);        
        expect((ruleMod).conditions[0].id).toEqual(testRule.conditions[0].id);     
        expect((ruleMod).actions[0].id).toEqual(testRule.actions[0].id);     
    });
          
        it('ruletoDTO should make object equal to openHABRule', () => {
        let ruleMod = new RuleDTO();
        let json = {};
        let json2 = {}; 
        ruleMod = RuleMapperHelperService.mapRuleToDTO(testRule); 
        json = ruleMod.getJSON();
        json2 = openHABRule.getJSON;
        console.log(json);
        console.log(json2);
        expect((ruleMod).uid).toEqual(openHABRule.uid);
        expect((ruleMod).enabled).toEqual(openHABRule.enabled);
        expect((ruleMod).name).toEqual(openHABRule.name);
        expect((ruleMod).description).toEqual(openHABRule.description);
        expect((ruleMod).triggers[0].id).toEqual(openHABRule.triggers[0].id);        
        expect((ruleMod).conditions[0].id).toEqual(openHABRule.conditions[0].id);     
        expect((ruleMod).actions[0].id).toEqual(openHABRule.actions[0].id);
        expect(json).toEqual(json2);
    });
    
      it('ruletoDTO should make JSON equal to openHABRule.getJSON', () => {
        let ruleMod = new RuleDTO();
        let json = {};
        let testjson = {}; 
        ruleMod = RuleMapperHelperService.mapRuleToDTO(testRule); 
        json = ruleMod.getJSON();
        testjson = openHABRule.getJSON;
        expect(json).toEqual(testjson);
    });

  
    
  
});
