# Blazegraph Workbench

The Angular 22 standalone Workbench uses Angular Material 3. It is built into
the WAR by the `bigdata-war-html` Maven module; the classic jQuery Workbench is
still available at `/legacy.html`.

## Local development

Use Node.js 24.15 or later and run:

```sh
npm ci
npm start
```

The development server proxies API requests to a local Blazegraph instance at
`http://localhost:9999/blazegraph`.

Run the frontend tests with `npm test`. Maven runs the production Angular build
and packages its output in the WAR.
