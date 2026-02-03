# Frontend

This directory contains the frontend applications for Kicherkrabbe, organized as a Yarn workspace.

## Structure

```
frontend/
├── .yarn/
│   └── releases/            # Yarn 4 binary (committed)
├── packages/
│   └── shared/              # Shared TypeScript library
│       └── src/
│           ├── option.ts    # Option monad (Some/None)
│           ├── equals.ts    # Eq interface
│           └── index.ts     # Barrel export
├── customer/                # Customer-facing Angular app (SSR)
├── management/              # Admin/management Angular app
├── .yarnrc.yml              # Yarn 4 configuration
├── package.json             # Workspace root
└── yarn.lock                # Single lockfile for all workspaces
```

## Shared Package

The `@kicherkrabbe/shared` package contains utilities shared between the `customer` and `management` apps.

### Usage

Import directly in any app:

```typescript
import { Option, some, none, someOrNone } from '@kicherkrabbe/shared';
```

The shared source is compiled on-the-fly by Angular during build/serve via tsconfig paths - no separate build step required.

### Adding New Shared Code

1. Add your code to `packages/shared/src/`
2. Export it from `packages/shared/src/index.ts`
3. Import from `@kicherkrabbe/shared` in the apps

## Development

This project uses Yarn 4 (Berry) with workspaces.

### Install Dependencies

From the `frontend/` directory:

```bash
yarn install
```

### Run Apps

From the `frontend/` directory:

```bash
yarn workspace customer start
yarn workspace management start
```

### Build Apps

From the `frontend/` directory:

```bash
yarn workspace customer build
yarn workspace management build
```

### List Workspaces

```bash
yarn workspaces list
```

### Run Command in All Workspaces

```bash
yarn workspaces foreach run <command>
```

## Notes

- Uses Yarn 4 with `nodeLinker: node-modules` for compatibility
- Both apps use Angular 21
- The `customer` app uses Server-Side Rendering (SSR)
- Dependencies are hoisted to the root `node_modules/` via Yarn workspaces
- The `.yarn/releases/` directory contains the Yarn 4 binary and should be committed
- Only one `yarn.lock` should exist at the workspace root - never add lockfiles in sub-workspaces
