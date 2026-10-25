import { TestBed } from '@angular/core/testing';

import { CotegoryService } from './cotegory.service';

describe('CotegoryService', () => {
  let service: CotegoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CotegoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
