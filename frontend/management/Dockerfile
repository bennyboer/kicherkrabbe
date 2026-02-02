FROM node:24-alpine

RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser

WORKDIR /app

COPY --chown=appuser:appgroup dist/management ./dist/management
COPY --chown=appuser:appgroup server.mjs ./server.mjs
COPY --chown=appuser:appgroup package.json ./package.json

RUN npm install --omit=dev --ignore-scripts && chown -R appuser:appgroup node_modules

USER appuser

ENV PORT=4200

EXPOSE 4200

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4200 || exit 1

CMD ["node", "server.mjs"]
