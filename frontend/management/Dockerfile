FROM node:22-alpine

WORKDIR /app

COPY dist/management ./dist/management
COPY server.mjs ./server.mjs
COPY package.json ./package.json

RUN npm install --omit=dev --ignore-scripts

ENV PORT=4200

EXPOSE 4200

CMD ["node", "server.mjs"]
