# wimmelbilder

A busy picture full of dogs, with one special dog hidden somewhere in it.

## Development

### Requirements

* Node.js with npm

### Running it locally

```bash
npm install
npm run dev
```

The dev server prints the URL it serves on, which is
`http://localhost:5173/wimmelbilder/`.

### Building it

```bash
npm run build
npm run preview
```

`npm run build` type-checks the sources and writes a plain static bundle to
`dist/`, which any static file server can serve.

### Running the tests

```bash
npm test
```

## Images

Backgrounds live in `src/img/bg` and sprites in `src/img/sprite`. Both
directories are read at build time, so a new image dropped into either one is
picked up without any other change. The special dog is `doge-scarf.png`.
