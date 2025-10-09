# Sassycrab

The frontend for kicherkrabbe.com made in Angular.

## Package manager

We use yarn (https://yarnpkg.com/). Install dependencies with `yarn install --frozen-lockfile`.

## Formatting and linting

We use Biome: https://biomejs.dev

## UI Library

We use PrimeNG: https://primeng.org

## Installing deployment on server

1. Install Node.js
2. Install `pm2` globally: `npm install pm2 -g`
3. Build the application via `yarn build`
4. Upload the contents of the `dist/sassycrab` folder to your server
5. Add a file ecosystm.config.js with the following content:

```js
module.exports = {
	apps: [
		{
			name: "Sassycrab",
			script: "./server.mjs", // Path to your server file
			env: {
				PM2: "true",
			},
		},
	],
};
```
6. Start the server with `pm2 start ecosystm.config.js`
