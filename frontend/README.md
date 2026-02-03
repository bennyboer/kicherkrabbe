# Frontend

This directory contains the frontend applications for Kicherkrabbe, organized as a Yarn workspace.

## Structure

```
frontend/
├── packages/
│   └── shared/              # Shared TypeScript library
│       └── src/
│           ├── option.ts    # Option monad (Some/None)
│           ├── equals.ts    # Eq interface
│           └── index.ts     # Barrel export
│
├── customer/                # Customer-facing Angular app (SSR)
├── management/              # Admin/management Angular app
├── package.json             # Workspace root
└── yarn.lock
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

### Install Dependencies

From the `frontend/` directory:

```bash
yarn install
```

### Run Apps

```bash
cd customer && yarn start
cd management && yarn start
```

### Build Apps

```bash
cd customer && yarn build
cd management && yarn build
```

## Notes

- Both apps use Angular 21
- The `customer` app uses Server-Side Rendering (SSR)
- Dependencies are hoisted to the root `node_modules/` via Yarn workspaces
