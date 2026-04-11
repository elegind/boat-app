/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  // Disable Tailwind's CSS reset (Preflight) to avoid conflicts with Angular Material.
  // Angular Material ships its own normalisation layer; having two resets causes
  // buttons, lists, typography and layout primitives to break.
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      // Breakpoints aligned with Angular Material breakpoints
      screens: {
        'sm':  '600px',   // Material small
        'md':  '960px',   // Material medium
        'lg':  '1280px',  // Material large
        'xl':  '1920px',  // Material xlarge
      },
    },
  },
  plugins: [],
};
