/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        red: {
          50: "#fef2f3",
          100: "#fde8eb",
          600: "#ec1c24",
          700: "#d60c14",
        },
        primary: {
          50: "#e8f0fe",
          100: "#d1e0fd",
          500: "#1a4fa0",
          600: "#0f3275",
          700: "#0a2552",
        },
        secondary: {
          400: "#ff6b00",
          500: "#ff5500",
          600: "#e54d00",
        },
        success: {
          500: "#2e7d32",
          600: "#1b5e20",
        },
        error: {
          500: "#ec1c24",
          600: "#d60c14",
        },
        neutral: {
          50: "#f9fafb",
          100: "#f3f4f6",
          200: "#e5e7eb",
          300: "#d1d5db",
          400: "#9ca3af",
          500: "#6b7280",
          600: "#4b5563",
          700: "#374151",
          800: "#1f2937",
          900: "#111827",
        },
        gray: {
          50: "#fafafa",
          100: "#f4f4f4",
          200: "#e0e0e0",
          300: "#d1d5db",
          400: "#9ca3af",
          500: "#6b7280",
          600: "#4b5563",
          700: "#374151",
          800: "#1f2937",
          900: "#121f3f",
        },
      },
      fontFamily: {
        sans: ["system-ui", "-apple-system", "sans-serif"],
        poppins: ["Poppins", "sans-serif"],
      },
      spacing: {
        nav: "80px",
        sidebar: "220px",
      },
      animation: {
        pulse: "pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite",
        "fade-in": "fadeIn 0.3s ease-out",
        "slide-in": "slideIn 0.4s ease-out",
      },
      keyframes: {
        fadeIn: {
          "0%": { opacity: "0", transform: "translateY(10px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        slideIn: {
          "0%": { transform: "translateX(400px)", opacity: "0" },
          "100%": { transform: "translateX(0)", opacity: "1" },
        },
      },
      zIndex: {
        9999: "9999",
      },
    },
  },
  plugins: [
    require("@tailwindcss/forms"),
    require("@tailwindcss/typography"),
  ],
};

