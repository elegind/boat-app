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
  delete: {
    confirm: {
      title: 'Delete {{name}}',
      message: 'Are you sure you want to delete {{name}}? This action cannot be undone.',
      cancel: 'Cancel',
      ok: 'Ok',
    },
    error: 'An error occurred while deleting.',
  },
  boats: {
    list: {
      title: 'All Boats',
    },
    card: {
      viewDetail: 'View detail',
      edit: 'Edit',
      delete: 'Delete',
    },
    detail: {
      title: 'Boat details',
      name: 'Name',
      description: 'Description',
      createdAt: 'Created at',
      close: 'Close',
    },
    delete: {
      success: 'Boat successfully deleted',
    },
    empty: 'No boats found',
    loading: 'Loading boats...',
  },
};

/** Shape contract — every locale file must satisfy this type. */
export type Translations = typeof EN_EN;
