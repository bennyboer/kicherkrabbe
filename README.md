# Kicherkrabbe Shop

Resources for the kicherkrabbe.com shop.

## Project Structure

```
kicherkrabbe/
├── frontend/                    # Frontend applications
│   ├── management/              # Angular 21 admin/management frontend
│   └── customer/                # Angular 21 customer-facing frontend (SSR)
│
├── backend/                     # Backend services
│   ├── apps/
│   │   └── api/                 # Main API application (Spring Boot)
│   ├── features/                # Feature modules
│   │   ├── assets/
│   │   ├── categories/
│   │   ├── colors/
│   │   ├── credentials/
│   │   ├── fabric-types/
│   │   ├── fabrics/
│   │   ├── inquiries/
│   │   ├── mailbox/
│   │   ├── mailing/
│   │   ├── notifications/
│   │   ├── patterns/
│   │   ├── products/
│   │   ├── telegram/
│   │   ├── topics/
│   │   └── users/
│   ├── libs/                    # Shared libraries
│   │   ├── auth/
│   │   ├── changes/
│   │   ├── commons/
│   │   ├── eventsourcing/
│   │   ├── messaging/
│   │   ├── money/
│   │   ├── permissions/
│   │   ├── persistence/
│   │   └── testing/
│   └── build-logic/             # Gradle build configuration
│
├── deploy/                      # Deployment configuration
│   ├── docker-swarm.yml         # Docker Swarm stack definition
│   └── traefik.yml              # Traefik reverse proxy config
│
└── .github/
    └── workflows/               # CI/CD pipelines
```

## Getting Started

See individual READMEs for setup instructions:
- [Backend](backend/README.md)
- [Management Frontend](frontend/management/README.md)
- [Customer Frontend](frontend/customer/README.md)

## Deployment

See [DEPLOY.md](DEPLOY.md) for the full deployment guide using Docker Swarm.
