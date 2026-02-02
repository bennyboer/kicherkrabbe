# Deployment Guide

This guide explains how to deploy the Kicherkrabbe server using Docker Swarm with automatic Let's Encrypt certificates.

## Architecture

The deployment consists of:
- **Traefik**: Reverse proxy with automatic Let's Encrypt certificate management (configured via `traefik.yml`)
- **MongoDB**: Database with replica set (required for transactions)
- **RabbitMQ**: Message broker for event-driven communication
- **App**: The Kicherkrabbe server application
- **Customer**: The customer-facing Angular SSR frontend
- **Management**: The admin/management Angular frontend

## Prerequisites

- A V-Server with Docker installed
- A domain pointing to your server (e.g., `kicherkrabbe.example.com`)
- Ports 80 and 443 open on your firewall

## Initial Setup

### 1. Initialize Docker Swarm

```bash
docker swarm init
```

### 2. Create Required Directories

```bash
mkdir -p /opt/kicherkrabbe
cd /opt/kicherkrabbe
```

### 3. Copy Deployment Files

Copy the `deploy/` folder contents to your server:

```bash
scp -r deploy/* user@your-server:/opt/kicherkrabbe/
```

This includes:
- `docker-swarm.yml` - Docker Swarm stack definition
- `traefik.yml` - Traefik reverse proxy configuration
- `.env.example` - Environment variable template

### 4. Create Environment File

```bash
cp .env.example .env
nano .env
```

Edit the `.env` file with your actual values:

```bash
# Your domain
DOMAIN=kicherkrabbe.example.com
ACME_EMAIL=your-email@example.com

# MongoDB - use strong passwords!
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=<generate-strong-password>
MONGO_APP_USERNAME=kicherkrabbe
MONGO_APP_PASSWORD=<generate-strong-password>

# RabbitMQ
RABBITMQ_USER=kicherkrabbe
RABBITMQ_PASSWORD=<generate-strong-password>

# Your Docker registry image
APP_IMAGE=your-registry/kicherkrabbe-server:latest
```

Generate strong passwords:
```bash
openssl rand -base64 32
```

### 5. Create Docker Secrets

**MongoDB Keyfile** (required for replica set authentication):
```bash
openssl rand -base64 756 > mongo-keyfile
docker secret create mongo-keyfile mongo-keyfile
rm mongo-keyfile
```

**JWT Keypair** (for authentication tokens):
```bash
openssl ecparam -genkey -name secp521r1 -noout -out key_pair.pem
docker secret create jwt-keypair key_pair.pem
rm key_pair.pem
```

### 6. Build and Push Application Image

On your development machine:

```bash
# Build the application
cd backend
./gradlew :apps:api:bootJar

# Build Docker image
docker build -t your-registry/kicherkrabbe-server:latest .

# Push to registry
docker push your-registry/kicherkrabbe-server:latest
```

If you don't have a registry, you can build directly on the server:
```bash
# On the server
docker build -t kicherkrabbe-server:latest backend/

# Update .env
APP_IMAGE=kicherkrabbe-server:latest
```

## Deploying the Stack

```bash
cd /opt/kicherkrabbe

# Load environment variables and deploy
set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

## Verify Deployment

```bash
# Check all services are running
docker service ls

# Check service logs
docker service logs kicherkrabbe_app
docker service logs kicherkrabbe_customer
docker service logs kicherkrabbe_management
docker service logs kicherkrabbe_mongo
docker service logs kicherkrabbe_rabbitmq
docker service logs kicherkrabbe_traefik

# Check MongoDB initialization
docker service logs kicherkrabbe_mongo-init
```

## Accessing Services

After deployment, the following URLs will be available (replace with your domain):

| Service | URL |
|---------|-----|
| Management Frontend | `https://kicherkrabbe.example.com` |
| Customer Frontend | `https://customer.kicherkrabbe.example.com` |
| API | `https://api.kicherkrabbe.example.com` |

## DNS Configuration

Create DNS A records pointing to your server:
- `kicherkrabbe.example.com` → `<your-server-ip>` (Management Frontend)
- `www.kicherkrabbe.example.com` → `<your-server-ip>` (redirects to non-www)
- `api.kicherkrabbe.example.com` → `<your-server-ip>` (API)
- `customer.kicherkrabbe.example.com` → `<your-server-ip>` (Customer Frontend)

## Updating the Application

```bash
# Build and push new image
cd backend
./gradlew :apps:api:bootJar
docker build -t your-registry/kicherkrabbe-server:v1.2.3 .
docker push your-registry/kicherkrabbe-server:v1.2.3

# On server: update image and redeploy
cd /opt/kicherkrabbe
sed -i 's|APP_IMAGE=.*|APP_IMAGE=your-registry/kicherkrabbe-server:v1.2.3|' .env
set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

The rolling update will deploy the new version without downtime.

## Let's Encrypt Certificates

Certificates are automatically:
- Requested on first deployment
- Renewed before expiration (Traefik handles this automatically)
- Stored in the `traefik-certificates` volume

To check certificate status:
```bash
docker exec $(docker ps -q -f name=kicherkrabbe_traefik) cat /letsencrypt/acme.json | jq '.letsencrypt.Certificates'
```

## Backup and Restore

### Backup

Backups can be performed without stopping services using MongoDB's `mongodump` with oplog for point-in-time consistency.

```bash
cd /opt/kicherkrabbe
mkdir -p backups/$(date +%Y%m%d)
cd backups/$(date +%Y%m%d)

# Hot backup MongoDB with oplog (point-in-time consistent, no downtime)
docker exec $(docker ps -q -f name=kicherkrabbe_mongo) mongodump \
  --uri="mongodb://admin:${MONGO_ROOT_PASSWORD}@localhost:27017/?authSource=admin&replicaSet=rs0" \
  --oplog \
  --archive \
  --gzip > mongo-backup.gz

# Backup RabbitMQ definitions (queues, exchanges, bindings)
docker exec $(docker ps -q -f name=kicherkrabbe_rabbitmq) rabbitmqctl export_definitions - > rabbitmq-definitions.json

# Backup Traefik certificates
docker run --rm -v kicherkrabbe_traefik-certificates:/data -v $(pwd):/backup alpine tar czf /backup/traefik-certs.tar.gz -C /data .

echo "Backup completed: $(pwd)"
```

### Restore

```bash
# Stop services
docker stack rm kicherkrabbe

# Wait for services to stop
sleep 10

# Recreate volumes
docker volume rm kicherkrabbe_mongo-data kicherkrabbe_mongo-config 2>/dev/null || true
docker volume create kicherkrabbe_mongo-data
docker volume create kicherkrabbe_mongo-config

# Start only MongoDB first
set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe

# Wait for MongoDB to be ready
sleep 30

# Restore MongoDB from backup
cat mongo-backup.gz | docker exec -i $(docker ps -q -f name=kicherkrabbe_mongo) mongorestore \
  --uri="mongodb://admin:${MONGO_ROOT_PASSWORD}@localhost:27017/?authSource=admin" \
  --oplogReplay \
  --archive \
  --gzip \
  --drop

# Restore RabbitMQ definitions
docker exec -i $(docker ps -q -f name=kicherkrabbe_rabbitmq) rabbitmqctl import_definitions < rabbitmq-definitions.json

# Restore Traefik certificates
docker run --rm -v kicherkrabbe_traefik-certificates:/data -v $(pwd):/backup alpine tar xzf /backup/traefik-certs.tar.gz -C /data

# Redeploy to ensure all services are running
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

## Connecting to MongoDB

To connect to MongoDB using `mongosh` when logged into the server:

```bash
cd /opt/kicherkrabbe
source .env

docker exec -it $(docker ps -q -f name=kicherkrabbe_mongo) mongosh \
  -u "$MONGO_ROOT_USERNAME" \
  -p "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin
```

To connect as the application user (limited permissions):

```bash
docker exec -it $(docker ps -q -f name=kicherkrabbe_mongo) mongosh \
  -u "$MONGO_APP_USERNAME" \
  -p "$MONGO_APP_PASSWORD" \
  --authenticationDatabase kicherkrabbe \
  kicherkrabbe
```

## Accessing RabbitMQ Management

RabbitMQ management UI is not exposed publicly for security reasons. Access it via SSH tunnel:

```bash
ssh -L 15672:localhost:15672 user@your-server \
  "docker exec -it \$(docker ps -q -f name=kicherkrabbe_rabbitmq) cat /dev/null && \
   socat TCP-LISTEN:15672,fork TCP:\$(docker inspect --format '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' \$(docker ps -q -f name=kicherkrabbe_rabbitmq)):15672"
```

Alternatively, use Docker's network directly from the server:

```bash
ssh user@your-server

RABBITMQ_IP=$(docker inspect --format '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q -f name=kicherkrabbe_rabbitmq))
curl http://$RABBITMQ_IP:15672
```

For a simpler approach, temporarily expose the port for administration:

```bash
ssh -L 15672:localhost:15672 user@your-server \
  "docker run --rm --network kicherkrabbe_internal alpine/socat TCP-LISTEN:15672,fork TCP:rabbitmq:15672"
```

Then open http://localhost:15672 in your browser and log in with your RabbitMQ credentials from `.env`.

## Accessing Traefik Dashboard

Traefik dashboard is not exposed publicly for security reasons. Access it via SSH tunnel:

```bash
ssh -L 8080:localhost:8080 user@your-server \
  "docker run --rm --network kicherkrabbe_traefik-public alpine/socat TCP-LISTEN:8080,fork TCP:traefik:8080"
```

Then open http://localhost:8080/dashboard/ in your browser.

## Troubleshooting

### Services not starting

```bash
# Check service status
docker service ps kicherkrabbe_app --no-trunc

# Check logs
docker service logs kicherkrabbe_app -f
```

### MongoDB replica set issues

```bash
# Connect to MongoDB container
docker exec -it $(docker ps -q -f name=kicherkrabbe_mongo) mongosh -u admin -p

# Check replica set status
rs.status()
```

### Certificate issues

```bash
# Check Traefik logs
docker service logs kicherkrabbe_traefik -f

# Verify ACME storage
docker exec $(docker ps -q -f name=kicherkrabbe_traefik) cat /letsencrypt/acme.json
```

### Network connectivity

```bash
# Test internal network
docker exec $(docker ps -q -f name=kicherkrabbe_app) ping mongo
docker exec $(docker ps -q -f name=kicherkrabbe_app) ping rabbitmq
```

## Important Notes

### CORS Configuration

The application has CORS configured for `https://www.kicherkrabbe.com` in production mode. If you're using a different domain, update `ServerSecurityConfig.java` before building.

## Security Considerations

1. **Firewall**: Only expose ports 80 and 443. All internal services communicate over Docker overlay networks.

2. **Secrets**: Never commit `.env` files. Consider using Docker secrets for sensitive values. Set proper file permissions:
   ```bash
   chmod 600 .env
   ```

3. **Admin Interfaces**: Both Traefik dashboard and RabbitMQ management are only accessible via SSH tunnel (see respective sections above). This prevents exposure of administrative interfaces to the internet.

4. **Updates**: Regularly update base images:
   ```bash
   docker pull traefik:v3.6
   docker pull mongo:8.2
   docker pull rabbitmq:4.2-management
   docker pull eclipse-temurin:25-jre-alpine
   docker pull node:24-alpine
   ```

5. **Backup**: Implement automated backups (see Backup section below).

## Pre-Production Security Checklist

Before going live, verify the following:

- [ ] Generated unique JWT keypair for production (not copied from development)
- [ ] All passwords in `.env` are strong (32+ characters, generated with `openssl rand -base64 32`)
- [ ] `.env` file permissions set to `600`
- [ ] SSH access properly secured (key-based authentication, no root login)
- [ ] DNS records configured correctly
- [ ] Firewall allows only ports 80 and 443
- [ ] SSL certificates are being issued correctly by Let's Encrypt
- [ ] Automated backup schedule configured
- [ ] Monitoring and alerting configured for service health
- [ ] Log aggregation configured for security audit trail

## Secret Rotation Procedures

Periodically rotate credentials to limit exposure from potential breaches.

### Rotate JWT Keypair

```bash
cd /opt/kicherkrabbe

openssl ecparam -genkey -name secp521r1 -noout -out key_pair_new.pem

docker secret rm jwt-keypair
docker secret create jwt-keypair key_pair_new.pem
rm key_pair_new.pem

set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

Note: Rotating the JWT keypair will invalidate all existing user sessions.

### Rotate MongoDB Passwords

```bash
docker exec -it $(docker ps -q -f name=kicherkrabbe_mongo) mongosh -u admin -p

use admin
db.changeUserPassword("admin", "<new-root-password>")
db.changeUserPassword("kicherkrabbe", "<new-app-password>")

nano .env

set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

### Rotate RabbitMQ Password

```bash
docker exec -it $(docker ps -q -f name=kicherkrabbe_rabbitmq) rabbitmqctl change_password kicherkrabbe <new-password>

nano .env

set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

## Removing the Stack

```bash
# Remove all services
docker stack rm kicherkrabbe

# Remove volumes (WARNING: deletes all data!)
docker volume rm kicherkrabbe_mongo-data kicherkrabbe_mongo-config kicherkrabbe_rabbitmq-data kicherkrabbe_traefik-certificates

# Remove secrets
docker secret rm mongo-keyfile jwt-keypair

# Leave swarm (if no longer needed)
docker swarm leave --force
```
