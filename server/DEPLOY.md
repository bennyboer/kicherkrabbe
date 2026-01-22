# Deployment Guide

This guide explains how to deploy the Kicherkrabbe server using Docker Swarm with automatic Let's Encrypt certificates.

## Architecture

The deployment consists of:
- **Traefik**: Reverse proxy with automatic Let's Encrypt certificate management
- **MongoDB**: Database with replica set (required for transactions)
- **RabbitMQ**: Message broker for event-driven communication
- **App**: The Kicherkrabbe server application

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

# Generate Traefik dashboard password:
# htpasswd -nb admin yourpassword
TRAEFIK_DASHBOARD_AUTH=admin:$apr1$xyz...

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

### 5. Create Docker Configs

**MongoDB Keyfile** (required for replica set authentication):
```bash
openssl rand -base64 756 > mongo-keyfile
docker config create mongo-keyfile mongo-keyfile
rm mongo-keyfile
```

**JWT Keypair** (for authentication tokens):
```bash
# Generate new keypair
openssl ecparam -genkey -name secp521r1 -noout -out key_pair.pem

# Or copy existing one from development
scp app/src/main/resources/keys/key_pair.pem user@your-server:/opt/kicherkrabbe/

# Create Docker secret
docker secret create jwt-keypair key_pair.pem
rm key_pair.pem
```

### 6. Build and Push Application Image

On your development machine:

```bash
# Build the application
./gradlew :app:bootJar

# Build Docker image
docker build -t your-registry/kicherkrabbe-server:latest .

# Push to registry
docker push your-registry/kicherkrabbe-server:latest
```

If you don't have a registry, you can build directly on the server:
```bash
# On the server
docker build -t kicherkrabbe-server:latest .

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
| Application | `https://kicherkrabbe.example.com` |
| Traefik Dashboard | `https://traefik.kicherkrabbe.example.com/dashboard/` |
| RabbitMQ Management | `https://rabbitmq.kicherkrabbe.example.com` |

## DNS Configuration

Create DNS A records pointing to your server:
- `kicherkrabbe.example.com` → `<your-server-ip>`
- `traefik.kicherkrabbe.example.com` → `<your-server-ip>`
- `rabbitmq.kicherkrabbe.example.com` → `<your-server-ip>`

## Updating the Application

```bash
# Build and push new image
./gradlew :app:bootJar
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

```bash
# Stop services (optional, for consistent backup)
docker stack rm kicherkrabbe

# Backup volumes
docker run --rm -v kicherkrabbe_mongo-data:/data -v $(pwd):/backup alpine tar czf /backup/mongo-data.tar.gz -C /data .
docker run --rm -v kicherkrabbe_rabbitmq-data:/data -v $(pwd):/backup alpine tar czf /backup/rabbitmq-data.tar.gz -C /data .
docker run --rm -v kicherkrabbe_traefik-certificates:/data -v $(pwd):/backup alpine tar czf /backup/traefik-certs.tar.gz -C /data .

# Restart services
set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

### Restore

```bash
# Stop services
docker stack rm kicherkrabbe

# Restore volumes
docker volume create kicherkrabbe_mongo-data
docker volume create kicherkrabbe_rabbitmq-data
docker volume create kicherkrabbe_traefik-certificates

docker run --rm -v kicherkrabbe_mongo-data:/data -v $(pwd):/backup alpine tar xzf /backup/mongo-data.tar.gz -C /data
docker run --rm -v kicherkrabbe_rabbitmq-data:/data -v $(pwd):/backup alpine tar xzf /backup/rabbitmq-data.tar.gz -C /data
docker run --rm -v kicherkrabbe_traefik-certificates:/data -v $(pwd):/backup alpine tar xzf /backup/traefik-certs.tar.gz -C /data

# Restart services
set -a && source .env && set +a
docker stack deploy -c docker-swarm.yml kicherkrabbe
```

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

2. **Secrets**: Never commit `.env` files. Consider using Docker secrets for sensitive values.

3. **Updates**: Regularly update base images:
   ```bash
   docker pull traefik:v3.6
   docker pull mongo:8.2
   docker pull rabbitmq:4.2-management
   docker pull eclipse-temurin:25-jre-alpine
   ```

4. **Backup**: Implement automated backups of the volumes.

## Removing the Stack

```bash
# Remove all services
docker stack rm kicherkrabbe

# Remove volumes (WARNING: deletes all data!)
docker volume rm kicherkrabbe_mongo-data kicherkrabbe_mongo-config kicherkrabbe_rabbitmq-data kicherkrabbe_traefik-certificates

# Remove configs and secrets
docker config rm mongo-keyfile
docker secret rm jwt-keypair

# Leave swarm (if no longer needed)
docker swarm leave --force
```
