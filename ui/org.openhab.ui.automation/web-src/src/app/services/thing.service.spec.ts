import { TestBed, inject } from '@angular/core/testing';

import { ThingService } from './thing.service';

describe('ThingService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ThingService]
    });
  });

  it('should be created', inject([ThingService], (service: ThingService) => {
    expect(service).toBeTruthy();
  }));
});
