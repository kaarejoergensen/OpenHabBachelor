import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModuleCreatorDialogComponent } from './module-creator-dialog.component';

describe('ModuleCreatorDialogComponent', () => {
  let component: ModuleCreatorDialogComponent;
  let fixture: ComponentFixture<ModuleCreatorDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModuleCreatorDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleCreatorDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
