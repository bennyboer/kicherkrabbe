# Management

The management frontend for kicherkrabbe.com made in Angular.

## Package Manager

We use yarn (https://yarnpkg.com/).

## Install

```bash
yarn install
```

## Development

```bash
yarn start
```

The app will be available at http://localhost:4200.

## Build

```bash
yarn build
```

## Production Server

The app can be served via Express for production deployments.

```bash
yarn build
yarn serve:express
```

The Express server:
- Serves static files from `dist/management/browser`
- Handles SPA routing (all routes serve `index.html`)
- Uses 1-year cache for static assets
- Listens on port 4200 (configurable via `PORT` env variable)

## Docker

Build and run with Docker:

```bash
yarn build
docker build -t kicherkrabbe-management .
docker run -p 4200:4200 kicherkrabbe-management
```

The Docker image is automatically built and pushed to `ghcr.io/bennyboer/kicherkrabbe-management` by the CI/CD pipeline.

## Deployment

The management frontend is deployed as part of the Docker Swarm stack. See [DEPLOY.md](../../DEPLOY.md) for the full deployment guide.

To update the management frontend:

1. Push changes to `main` branch
2. GitHub Actions will build and push the Docker image
3. The deploy job pulls and deploys the new image

You'll need to add the management service to your `docker-swarm.yml` on the server:

```yaml
management:
  image: ghcr.io/bennyboer/kicherkrabbe-management:latest
  environment:
    - PORT=4200
  networks:
    - internal
  deploy:
    replicas: 1
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.management.rule=Host(`management.${DOMAIN}`)"
      - "traefik.http.routers.management.entrypoints=websecure"
      - "traefik.http.routers.management.tls.certresolver=letsencrypt"
      - "traefik.http.services.management.loadbalancer.server.port=4200"
```
