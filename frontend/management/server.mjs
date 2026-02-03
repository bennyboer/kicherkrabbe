import express from "express";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const app = express();
const browserDistFolder = join(__dirname, "browser");

app.use(
	express.static(browserDistFolder, {
		maxAge: "1y",
		index: false,
	}),
);

app.get("/{*path}", (req, res) => {
	res.sendFile(join(browserDistFolder, "index.html"));
});

const port = process.env.PORT || 4200;
app.listen(port, () => {
	console.log(`Management app listening on http://localhost:${port}`);
});
