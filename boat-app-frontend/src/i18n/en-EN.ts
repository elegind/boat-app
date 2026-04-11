/**
 * English (en-EN) translations — single source of truth for all UI labels.
 */
export const EN_EN = {
  app: {
    title: 'Boat App',
    tagline: 'Fleet Management',
    nav: {
      menuAriaLabel: 'Toggle navigation menu',
      sectionLabel: 'Navigation',
      home: 'Home',
    },
    footer: {
      version: 'v0.0.1',
    },
  },
  home: {
    header: {
      title: 'Welcome to Boat App',
      subtitle: 'Manage your fleet of boats.',
    },
    card: {
      title: 'Hello World',
      body: 'This is the home page of the Boat App.',
      button: {
        idle: 'Click me!',
      },
    },
  },
};

/** Shape contract — every locale file must satisfy this type. */
export type Translations = typeof EN_EN;

