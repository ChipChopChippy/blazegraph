import { Injectable } from '@angular/core';

export interface NamespaceInfo {
  name: string;
}

export interface SparqlBinding {
  type: string;
  value: string;
  'xml:lang'?: string;
  datatype?: string;
}

export interface SparqlResults {
  head: { vars: string[] };
  results: { bindings: Array<Record<string, SparqlBinding>> };
}

@Injectable({ providedIn: 'root' })
export class WorkbenchApi {
  private loadBalanced = false;
  private readonly contextPath = this.findContextPath();

  setLoadBalanced(enabled: boolean): void {
    this.loadBalanced = enabled;
  }

  async namespaces(): Promise<NamespaceInfo[]> {
    const response = await this.request('namespace?describe-each-named-graph=false');
    const xml = this.parseXml(await response.text());
    const rdfDescriptions = Array.from(xml.getElementsByTagNameNS('*', 'Description'));
    return rdfDescriptions
      .map((description) => {
        const title = Array.from(description.children).find(
          (element) => element.localName === 'title',
        );
        return title?.textContent?.trim() ?? '';
      })
      .filter((name) => name.length > 0)
      .map((name) => ({ name }));
  }

  async sparql(
    namespace: string,
    params: URLSearchParams,
    write = false,
  ): Promise<Response> {
    return this.request(`namespace/${encodeURIComponent(namespace)}/sparql`, {
      method: 'POST',
      headers: {
        Accept: 'application/sparql-results+json, application/json, text/plain',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      },
      body: params,
    }, write);
  }

  async namespaceProperties(namespace: string): Promise<Array<[string, string]>> {
    const response = await this.request(`namespace/${encodeURIComponent(namespace)}/properties`);
    const xml = this.parseXml(await response.text());
    return Array.from(xml.getElementsByTagName('entry')).map((entry) => [
      entry.getAttribute('key') ?? '',
      entry.textContent ?? '',
    ]);
  }

  async createNamespace(name: string, options: {
    mode: 'triples' | 'quads' | 'rdr';
    inference: boolean;
    textIndex: boolean;
    geoSpatial: boolean;
    isolatableIndices: boolean;
  }): Promise<Response> {
    const properties: Record<string, string> = {
      'com.bigdata.rdf.sail.namespace': name,
      'com.bigdata.rdf.store.AbstractTripleStore.quads': String(options.mode === 'quads'),
      'com.bigdata.rdf.store.AbstractTripleStore.statementIdentifiers': String(options.mode === 'rdr'),
      'com.bigdata.rdf.store.AbstractTripleStore.textIndex': String(options.textIndex),
      'com.bigdata.rdf.store.AbstractTripleStore.geoSpatial': String(options.geoSpatial),
      'com.bigdata.rdf.sail.isolatableIndices': String(options.isolatableIndices),
    };
    if (options.inference) {
      properties['com.bigdata.rdf.store.AbstractTripleStore.axiomsClass'] =
        'com.bigdata.rdf.axioms.OwlAxioms';
    }

    const xml = Object.entries(properties)
      .map(([key, value]) => `<entry key="${this.escapeXml(key)}">${this.escapeXml(value)}</entry>`)
      .join('');
    return this.request('namespace', {
      method: 'POST',
      headers: { 'Content-Type': 'application/xml; charset=UTF-8' },
      body: `<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n<properties>${xml}</properties>`,
    }, true);
  }

  async deleteNamespace(name: string): Promise<Response> {
    return this.request(`namespace/${encodeURIComponent(name)}`, { method: 'DELETE' }, true);
  }

  async update(namespace: string, update: string): Promise<Response> {
    const params = new URLSearchParams({ update });
    return this.request(`namespace/${encodeURIComponent(namespace)}/sparql`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: params,
    }, true);
  }

  async upload(namespace: string, file: File, format: string): Promise<Response> {
    return this.request(`namespace/${encodeURIComponent(namespace)}/sparql`, {
      method: 'POST',
      headers: { 'Content-Type': format },
      body: file,
    }, true);
  }

  async status(): Promise<unknown> {
    const response = await this.request('status?showQueries=details&output=json');
    return this.readResponse(response);
  }

  async health(): Promise<unknown> {
    const response = await this.request('status?health&output=json');
    return this.readResponse(response);
  }

  async performance(): Promise<string> {
    const response = await this.request('counters', {
      headers: { Accept: 'application/xml, text/xml, text/html' },
    });
    return response.text();
  }

  private async request(path: string, init: RequestInit = {}, write = false): Promise<Response> {
    const prefix = this.loadBalanced ? (write ? 'LBS/leader/' : 'LBS/read/') : '';
    const url = `${this.contextPath}/${prefix}${path}`;
    const response = await fetch(url, { credentials: 'same-origin', ...init });
    if (!response.ok) {
      const detail = (await response.text()).trim();
      throw new Error(detail || `Request failed with HTTP ${response.status}`);
    }
    return response;
  }

  private async readResponse(response: Response): Promise<unknown> {
    const text = await response.text();
    if (!text) {
      return null;
    }
    try {
      return JSON.parse(text) as unknown;
    } catch {
      return text;
    }
  }

  private parseXml(text: string): Document {
    const xml = new DOMParser().parseFromString(text, 'application/xml');
    if (xml.querySelector('parsererror')) {
      throw new Error('The server returned invalid XML.');
    }
    return xml;
  }

  private findContextPath(): string {
    const basePath = new URL(document.querySelector('base')?.href ?? './', window.location.href).pathname;
    return basePath.replace(/\/$/, '');
  }

  private escapeXml(value: string): string {
    return value.replace(/[<>&'"]/g, (character) => ({
      '<': '&lt;',
      '>': '&gt;',
      '&': '&amp;',
      "'": '&apos;',
      '"': '&quot;',
    })[character] ?? character);
  }
}
