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
				overlay: {
					select: {
						background: "{zinc.900}",
						borderColor: "{zinc.700}",
						color: "{zinc.50}",
					},
				},
				list: {
					option: {
						focusBackground: "{zinc.800}",
						selectedBackground: "{zinc.700}",
						selectedFocusBackground: "{zinc.700}",
						color: "{zinc.50}",
						focusColor: "{zinc.50}",
						selectedColor: "{zinc.50}",
						selectedFocusColor: "{zinc.50}",
					},
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
		breadcrumb: {
			root: {
				background: "transparent",
				padding: "0",
			},
			item: {
				color: "{text.muted.color}",
				hoverColor: "{primary.color}",
			},
			separator: {
				color: "{text.muted.color}",
			},
			colorScheme: {
				light: {
					item: {
						color: "{surface.600}",
						hoverColor: "{primary.color}",
					},
					separator: {
						color: "{surface.600}",
					},
				},
				dark: {
					item: {
						color: "{surface.600}",
						hoverColor: "{primary.color}",
					},
					separator: {
						color: "{surface.600}",
					},
				},
			},
		},
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
		inputtext: {
			root: {
				background: "{surface.0}",
				color: "{surface.700}",
				borderColor: "{surface.300}",
			},
			colorScheme: {
				light: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.300}",
						hoverBorderColor: "{surface.400}",
					},
				},
				dark: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.300}",
						hoverBorderColor: "{surface.400}",
					},
				},
			},
		},
		select: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.300}",
						hoverBorderColor: "{surface.400}",
					},
					dropdown: {
						color: "{surface.900}",
					},
				},
			},
		},
		multiselect: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.300}",
						hoverBorderColor: "{surface.400}",
					},
					dropdown: {
						color: "{surface.900}",
					},
				},
			},
		},
		checkbox: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.0}",
						checkedBackground: "{primary.color}",
						borderColor: "{surface.300}",
						hoverBorderColor: "{surface.400}",
					},
				},
			},
		},
		drawer: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.0}",
						borderColor: "{surface.200}",
						color: "{surface.900}",
					},
				},
			},
		},
		toggleswitch: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.300}",
						hoverBackground: "{surface.400}",
						checkedBackground: "{primary.color}",
						checkedHoverBackground: "{primary.hoverColor}",
						borderColor: "{surface.400}",
					},
					handle: {
						background: "{surface.0}",
						hoverBackground: "{surface.0}",
						checkedBackground: "{surface.0}",
						checkedHoverBackground: "{surface.0}",
					},
				},
			},
		},
		panel: {
			colorScheme: {
				dark: {
					root: {
						background: "{surface.100}",
						borderColor: "{surface.200}",
						color: "{surface.900}",
					},
					header: {
						background: "{surface.100}",
						color: "{surface.900}",
					},
				},
			},
		},
		slider: {
			colorScheme: {
				dark: {
					track: {
						background: "{surface.300}",
					},
					range: {
						background: "{primary.color}",
					},
					handle: {
						background: "{surface.0}",
						hoverBackground: "{surface.0}",
						content: {
							background: "{primary.color}",
						},
					},
				},
			},
		},
		popover: {
			colorScheme: {
				light: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.200}",
					},
				},
				dark: {
					root: {
						background: "{surface.0}",
						color: "{surface.900}",
						borderColor: "{surface.200}",
					},
				},
			},
		},
	},
});
