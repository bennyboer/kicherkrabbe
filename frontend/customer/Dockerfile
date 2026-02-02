FROM node:24-alpine

WORKDIR /app

COPY --chown=node:node dist/customer ./dist/customer

USER node

ENV PORT=4000

EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4000 || exit 1

CMD ["node", "dist/customer/server/server.mjs"]
