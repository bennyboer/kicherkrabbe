import { bootstrapApplication } from "@angular/platform-browser";
import { Carousel } from "primeng/carousel";
import { App } from "./app/app";
import { appConfig } from "./app/app.config";

// Fix: PrimeNG carousel blocks vertical page scrolling on mobile
// https://github.com/primefaces/primeng/issues/13266
// https://github.com/primefaces/primeng/issues/11607
const originalOnTouchMove = Carousel.prototype.onTouchMove;
Carousel.prototype.onTouchMove = function (event: TouchEvent) {
	if (typeof this.startPos?.x === "number" && typeof this.startPos?.y === "number") {
		const touch = event.changedTouches[0] || event.touches[0];
		const deltaX = Math.abs(touch.clientX - this.startPos.x);
		const deltaY = Math.abs(touch.clientY - this.startPos.y);
		if (deltaY > deltaX) {
			return;
		}
	}
	originalOnTouchMove.call(this, event);
};

bootstrapApplication(App, appConfig).catch((err) => console.error(err));
