import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { WorkbenchApi } from './workbench-api';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{
        provide: WorkbenchApi,
        useValue: {
          namespaces: async () => [{ name: 'kb' }],
          health: async () => ({ status: 'OK' }),
          setLoadBalanced: () => undefined,
        },
      }],
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the Blazegraph workspace', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Good to see you.');
  });
});
