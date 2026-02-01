import { definePreset } from "@primeuix/themes";
import Aura from "@primeuix/themes/aura";

export const customerPreset = definePreset(Aura, {
	semantic: {
		primary: {
			50: "{zinc.50}",
			100: "{zinc.100}",
			200: "{zinc.200}",
			300: "{zinc.300}",
			400: "{zinc.400}",
			500: "{zinc.500}",
			600: "{zinc.600}",
			700: "{zinc.700}",
			800: "{zinc.800}",
			900: "{zinc.900}",
			950: "{zinc.950}",
		},
		id: {
			50: "#fef6f5",
			100: "#fde8e6",
			200: "#fbcfcc",
			300: "#f8a9a3",
			400: "#f37972",
			500: "#F05048",
			600: "#dd3129",
			700: "#ba2520",
			800: "#9a221e",
			900: "#4E1D18",
			950: "#2a0d0b",
		},
		colorScheme: {
			light: {
				primary: {
					color: "{zinc.950}",
					inverseColor: "#ffffff",
					hoverColor: "{zinc.900}",
					activeColor: "{zinc.800}",
					contrastColor: "{zinc.100}",
				},
				surface: {
					0: "#ffffff",
					50: "{zinc.50}",
					100: "{zinc.100}",
					200: "{zinc.200}",
					300: "{zinc.300}",
					400: "{zinc.400}",
					500: "{zinc.500}",
					600: "{zinc.600}",
					700: "{zinc.700}",
					800: "{zinc.800}",
					900: "{zinc.900}",
					950: "{zinc.950}",
				},
			},
			dark: {
				primary: {
					color: "{zinc.50}",
					inverseColor: "{zinc.950}",
					hoverColor: "{zinc.100}",
					activeColor: "{zinc.200}",
					contrastColor: "{zinc.900}",
				},
				surface: {
					0: "{zinc.950}",
					50: "{zinc.900}",
					100: "{zinc.800}",
					200: "{zinc.700}",
					300: "{zinc.600}",
					400: "{zinc.500}",
					500: "{zinc.400}",
					600: "{zinc.300}",
					700: "{zinc.200}",
					800: "{zinc.100}",
					900: "{zinc.50}",
					950: "#ffffff",
				},
			},
		},
		focusRing: {
			width: "2px",
			style: "solid",
			color: "{primary.color}",
			offset: "-2px",
		},
	},
	components: {
		card: {
			root: {
				background: "{surface.0}",
				color: "{surface.700}",
			},
			subtitle: {
				color: "{surface.500}",
			},
		},
		carousel: {
			colorScheme: {
				light: {
					indicator: {
						background: "{surface.200}",
						hoverBackground: "{surface.300}",
						activeBackground: "{surface.900}",
					},
				},
				dark: {
					indicator: {
						background: "{zinc.700}",
						hoverBackground: "{zinc.600}",
						activeBackground: "#ffffff",
					},
				},
			},
		},
	},
});
