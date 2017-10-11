import { TestBed, inject } from '@angular/core/testing';

import { SharedPropertiesService } from './shared-properties.service';

describe('SharedPropertiesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SharedPropertiesService]
    });
  });

  it('should be created', inject([SharedPropertiesService], (service: SharedPropertiesService) => {
    expect(service).toBeTruthy();
  }));
});
