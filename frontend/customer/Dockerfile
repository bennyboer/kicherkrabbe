FROM node:22-alpine

WORKDIR /app

COPY dist/customer ./dist/customer

ENV PORT=4000

EXPOSE 4000

CMD ["node", "dist/customer/server/server.mjs"]
