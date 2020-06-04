import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScalaphyDashboardComponent } from './scalaphy-dashboard.component';

describe('ScalaphyDashboardComponent', () => {
  let component: ScalaphyDashboardComponent;
  let fixture: ComponentFixture<ScalaphyDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ScalaphyDashboardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScalaphyDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
