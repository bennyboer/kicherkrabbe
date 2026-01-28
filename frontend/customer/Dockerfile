FROM node:22-alpine

WORKDIR /app

COPY dist/sassycrab ./dist/sassycrab

ENV PORT=4000

EXPOSE 4000

CMD ["node", "dist/sassycrab/server/server.mjs"]
