FROM node:24-alpine

WORKDIR /app

COPY --chown=node:node dist/management ./dist/management
COPY --chown=node:node server.mjs ./server.mjs
COPY --chown=node:node package.json ./package.json

RUN npm install --omit=dev --ignore-scripts && chown -R node:node node_modules

USER node

ENV PORT=4200

EXPOSE 4200

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4200 || exit 1

CMD ["node", "server.mjs"]
