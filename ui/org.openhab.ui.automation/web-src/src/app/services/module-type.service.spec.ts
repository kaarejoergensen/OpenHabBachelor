import { TestBed, inject } from '@angular/core/testing';

import { ModuleTypeService } from './module-type.service';

describe('ModuleTypeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModuleTypeService]
    });
  });

  it('should be created', inject([ModuleTypeService], (service: ModuleTypeService) => {
    expect(service).toBeTruthy();
  }));
});
