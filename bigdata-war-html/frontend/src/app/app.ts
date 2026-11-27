import { Component, OnInit, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTableModule } from '@angular/material/table';
import {
  NamespaceInfo,
  SparqlBinding,
  SparqlResults,
  WorkbenchApi,
} from './workbench-api';

type ViewId = 'overview' | 'query' | 'update' | 'explore' | 'namespaces' | 'status' | 'health' | 'performance';
type NamespaceMode = 'triples' | 'quads' | 'rdr';

interface NavigationItem {
  id: ViewId;
  label: string;
  glyph: string;
  group: 'Workspace' | 'Administration';
}

@Component({
  selector: 'app-root',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSidenavModule,
    MatTableModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  readonly navigation: NavigationItem[] = [
    { id: 'overview', label: 'Overview', glyph: '◫', group: 'Workspace' },
    { id: 'query', label: 'Query', glyph: '⌕', group: 'Workspace' },
    { id: 'update', label: 'Update & load', glyph: '↥', group: 'Workspace' },
    { id: 'explore', label: 'Explore graph', glyph: '⌘', group: 'Workspace' },
    { id: 'namespaces', label: 'Namespaces', glyph: '▦', group: 'Administration' },
    { id: 'status', label: 'Running queries', glyph: '◷', group: 'Administration' },
    { id: 'health', label: 'Health', glyph: '♡', group: 'Administration' },
    { id: 'performance', label: 'Performance', glyph: '▥', group: 'Administration' },
  ];

  readonly activeView = signal<ViewId>('overview');
  readonly namespaces = signal<NamespaceInfo[]>([]);
  readonly currentNamespace = signal('kb');
  readonly queryResults = signal<SparqlResults | null>(null);
  readonly queryError = signal('');
  readonly queryRunning = signal(false);
  readonly queryPage = signal(0);
  readonly queryPageSize = signal(50);
  readonly queryHistory = signal<string[]>(this.readQueryHistory());
  readonly namespaceProperties = signal<Array<[string, string]> | null>(null);
  readonly namespaceError = signal('');
  readonly updateError = signal('');
  readonly updateMessage = signal('');
  readonly selectedFile = signal<File | null>(null);
  readonly exploreOutgoing = signal<Array<Record<string, SparqlBinding>>>([]);
  readonly exploreIncoming = signal<Array<Record<string, SparqlBinding>>>([]);
  readonly exploreError = signal('');
  readonly healthData = signal<unknown>(null);
  readonly statusData = signal<unknown>(null);
  readonly performanceData = signal('');
  readonly panelError = signal('');
  readonly panelLoading = signal(false);
  readonly loadBalanced = signal(false);

  queryText = 'SELECT * WHERE {\n  ?subject ?predicate ?object\n}\nLIMIT 100';
  updateText = '';
  updateMode: 'sparql' | 'rdf' = 'sparql';
  rdfFormat = 'text/turtle';
  analytic = false;
  explain = false;
  showInference = false;
  namespaceName = '';
  namespaceMode: NamespaceMode = 'triples';
  namespaceInference = false;
  namespaceTextIndex = true;
  namespaceGeoSpatial = false;
  namespaceIsolatableIndices = false;
  exploreUri = '';

  private readonly defaultPageSize = 50;

  constructor(private readonly api: WorkbenchApi) {}

  ngOnInit(): void {
    const savedNamespace = localStorage.getItem('blazegraph.namespace');
    if (savedNamespace) {
      this.currentNamespace.set(savedNamespace);
    }
    void this.loadNamespaces();
    void this.loadHealth();
  }

  selectView(view: ViewId): void {
    this.activeView.set(view);
    this.panelError.set('');
    if (view === 'status') void this.loadStatus();
    if (view === 'health') void this.loadHealth();
    if (view === 'performance') void this.loadPerformance();
    if (view === 'namespaces') void this.loadNamespaces();
  }

  activeLabel(): string {
    return this.navigation.find((item) => item.id === this.activeView())?.label ?? 'Overview';
  }

  async loadNamespaces(): Promise<void> {
    try {
      const namespaces = await this.api.namespaces();
      this.namespaces.set(namespaces);
      if (!namespaces.some(({ name }) => name === this.currentNamespace())) {
        const nextNamespace = namespaces[0]?.name ?? 'kb';
        this.setCurrentNamespace(nextNamespace);
      }
    } catch (error) {
      this.namespaceError.set(this.errorMessage(error));
    }
  }

  setCurrentNamespace(name: string): void {
    this.currentNamespace.set(name);
    localStorage.setItem('blazegraph.namespace', name);
  }

  async runQuery(): Promise<void> {
    const query = this.queryText.trim();
    if (!query) {
      this.queryError.set('Enter a SPARQL query to continue.');
      return;
    }
    this.queryRunning.set(true);
    this.queryError.set('');
    this.queryResults.set(null);
    this.queryPage.set(0);
    try {
      const params = new URLSearchParams({
        query,
        format: 'json',
      });
      if (this.analytic) params.set('analytic', 'true');
      if (this.explain) params.set('explain', 'true');
      const response = await this.api.sparql(this.currentNamespace(), params);
      const result = await response.json() as SparqlResults;
      if (!result.head?.vars || !result.results?.bindings) {
        throw new Error('The endpoint response was not a SPARQL JSON result set.');
      }
      this.queryResults.set(result);
      this.saveQueryHistory(query);
    } catch (error) {
      this.queryError.set(this.errorMessage(error));
    } finally {
      this.queryRunning.set(false);
    }
  }

  clearQueryResults(): void {
    this.queryResults.set(null);
    this.queryError.set('');
  }

  async restoreQuery(query: string): Promise<void> {
    this.queryText = query;
    await this.runQuery();
  }

  async runUpdate(): Promise<void> {
    this.updateError.set('');
    this.updateMessage.set('');
    try {
      let response: Response;
      if (this.updateMode === 'rdf') {
        const file = this.selectedFile();
        if (!file) {
          throw new Error('Choose an RDF file to upload.');
        }
        response = await this.api.upload(this.currentNamespace(), file, this.rdfFormat);
      } else {
        const update = this.updateText.trim();
        if (!update) {
          throw new Error('Enter a SPARQL Update command.');
        }
        response = await this.api.update(this.currentNamespace(), update);
      }
      this.updateMessage.set((await response.text()).trim() || 'The update completed successfully.');
      this.selectedFile.set(null);
    } catch (error) {
      this.updateError.set(this.errorMessage(error));
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
  }

  async createNamespace(): Promise<void> {
    this.namespaceError.set('');
    const name = this.namespaceName.trim();
    if (!/^[A-Za-z0-9._~-]+$/.test(name)) {
      this.namespaceError.set('Use letters, numbers, dots, underscores, hyphens, or tildes for the namespace name.');
      return;
    }
    if (this.namespaces().some((namespace) => namespace.name === name)) {
      this.namespaceError.set(`Namespace "${name}" already exists.`);
      return;
    }
    try {
      await this.api.createNamespace(name, {
        mode: this.namespaceMode,
        inference: this.namespaceInference && this.namespaceMode === 'triples' &&
          !this.namespaceIsolatableIndices,
        textIndex: this.namespaceTextIndex,
        geoSpatial: this.namespaceGeoSpatial,
        isolatableIndices: this.namespaceIsolatableIndices,
      });
      this.namespaceName = '';
      await this.loadNamespaces();
      this.setCurrentNamespace(name);
      this.namespaceProperties.set(null);
    } catch (error) {
      this.namespaceError.set(this.errorMessage(error));
    }
  }

  async deleteNamespace(name: string): Promise<void> {
    if (name === this.defaultNamespace()) {
      this.namespaceError.set('The default namespace cannot be deleted.');
      return;
    }
    if (!window.confirm(`Delete namespace "${name}"? This cannot be undone.`)) return;
    try {
      await this.api.deleteNamespace(name);
      if (this.currentNamespace() === name) {
        this.setCurrentNamespace(this.defaultNamespace());
      }
      this.namespaceProperties.set(null);
      await this.loadNamespaces();
    } catch (error) {
      this.namespaceError.set(this.errorMessage(error));
    }
  }

  async showNamespaceProperties(name: string): Promise<void> {
    this.namespaceError.set('');
    try {
      this.namespaceProperties.set(await this.api.namespaceProperties(name));
    } catch (error) {
      this.namespaceError.set(this.errorMessage(error));
    }
  }

  async explore(): Promise<void> {
    this.exploreError.set('');
    this.exploreOutgoing.set([]);
    this.exploreIncoming.set([]);
    const uri = this.exploreUri.trim();
    if (!uri || /[<>\s\u0000-\u001f]/.test(uri)) {
      this.exploreError.set('Enter a valid URI without whitespace or angle brackets.');
      return;
    }
    try {
      const escapedUri = uri.replace(/[\\"]/g, '\\$&');
      const [outgoing, incoming] = await Promise.all([
        this.queryForExplore(
          `SELECT ?predicate ?object WHERE { <${escapedUri}> ?predicate ?object } LIMIT 100`,
        ),
        this.queryForExplore(
          `SELECT ?subject ?predicate WHERE { ?subject ?predicate <${escapedUri}> } LIMIT 100`,
        ),
      ]);
      this.exploreOutgoing.set(outgoing.results.bindings);
      this.exploreIncoming.set(incoming.results.bindings);
    } catch (error) {
      this.exploreError.set(this.errorMessage(error));
    }
  }

  async loadStatus(): Promise<void> {
    await this.loadPanel(() => this.api.status(), this.statusData);
  }

  async loadHealth(): Promise<void> {
    await this.loadPanel(() => this.api.health(), this.healthData);
  }

  async loadPerformance(): Promise<void> {
    this.panelLoading.set(true);
    this.panelError.set('');
    try {
      this.performanceData.set(await this.api.performance());
    } catch (error) {
      this.panelError.set(this.errorMessage(error));
    } finally {
      this.panelLoading.set(false);
    }
  }

  toggleLoadBalanced(enabled: boolean): void {
    this.loadBalanced.set(enabled);
    this.api.setLoadBalanced(enabled);
    void this.loadNamespaces();
  }

  queryColumns(): string[] {
    return this.queryResults()?.head.vars ?? [];
  }

  pagedResults(): Array<Record<string, SparqlBinding>> {
    const bindings = this.queryResults()?.results.bindings ?? [];
    const start = this.queryPage() * this.queryPageSize();
    return bindings.slice(start, start + this.queryPageSize());
  }

  totalResults(): number {
    return this.queryResults()?.results.bindings.length ?? 0;
  }

  pageCount(): number {
    return Math.max(1, Math.ceil(this.totalResults() / this.queryPageSize()));
  }

  moveQueryPage(delta: number): void {
    this.queryPage.update((page) => Math.min(
      Math.max(0, page + delta),
      this.pageCount() - 1,
    ));
  }

  setQueryPageSize(size: number): void {
    this.queryPageSize.set(size);
    this.queryPage.set(0);
  }

  cellValue(row: Record<string, SparqlBinding>, column: string): string {
    return row[column]?.value ?? '';
  }

  exportResults(): void {
    const results = this.queryResults();
    if (!results) return;
    const blob = new Blob([JSON.stringify(results, null, 2)], {
      type: 'application/sparql-results+json',
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = 'blazegraph-query-results.json';
    anchor.click();
    URL.revokeObjectURL(url);
  }

  healthLabel(): string {
    const data = this.healthData();
    if (!data) return 'Checking';
    if (typeof data === 'string') return 'Available';
    const status = (data as { status?: unknown; healthy?: unknown }).status;
    const healthy = (data as { healthy?: unknown }).healthy;
    return healthy === false || status === 'ERROR' || status === 'UNHEALTHY'
      ? 'Attention'
      : 'Operational';
  }

  pretty(value: unknown): string {
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2) ?? '';
  }

  namespaceGroups(): Array<'Workspace' | 'Administration'> {
    return ['Workspace', 'Administration'];
  }

  defaultNamespace(): string {
    return this.namespaces()[0]?.name ?? 'kb';
  }

  trackNamespace(_index: number, item: NamespaceInfo): string {
    return item.name;
  }

  private async queryForExplore(query: string): Promise<SparqlResults> {
    const response = await this.api.sparql(
      this.currentNamespace(),
      new URLSearchParams({
        query,
        format: 'json',
      }),
    );
    return response.json() as Promise<SparqlResults>;
  }

  private async loadPanel<T>(load: () => Promise<T>, target: WritableSignal<T | null>): Promise<void> {
    this.panelLoading.set(true);
    this.panelError.set('');
    try {
      target.set(await load());
    } catch (error) {
      this.panelError.set(this.errorMessage(error));
    } finally {
      this.panelLoading.set(false);
    }
  }

  private saveQueryHistory(query: string): void {
    const history = [query, ...this.queryHistory().filter((item) => item !== query)].slice(0, 8);
    this.queryHistory.set(history);
    localStorage.setItem('blazegraph.query-history', JSON.stringify(history));
  }

  private readQueryHistory(): string[] {
    try {
      const history = JSON.parse(localStorage.getItem('blazegraph.query-history') ?? '[]') as unknown;
      return Array.isArray(history) && history.every((item) => typeof item === 'string')
        ? history.slice(0, 8)
        : [];
    } catch {
      return [];
    }
  }

  private errorMessage(error: unknown): string {
    return error instanceof Error ? error.message : 'The request could not be completed.';
  }
}
