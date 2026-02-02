FROM node:24-alpine

RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser

WORKDIR /app

COPY --chown=appuser:appgroup dist/customer ./dist/customer

USER appuser

ENV PORT=4000

EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4000 || exit 1

CMD ["node", "dist/customer/server/server.mjs"]
