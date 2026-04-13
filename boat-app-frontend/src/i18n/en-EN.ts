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
    create: {
      button: 'Create boat',
      dialog: {
        title: 'Create a new boat',
      },
      success: 'Boat successfully created',
      error: 'An error occurred while creating the boat',
    },
    form: {
      name: {
        label: 'Name',
        hint: 'Letters, numbers and hyphens only. Max 30 characters.',
        error: {
          required: 'Name is required.',
          maxlength: 'Name cannot exceed 30 characters.',
          pattern: 'Only letters, numbers and hyphens allowed.',
        },
      },
      description: {
        label: 'Description',
        hint: 'Max 500 characters.',
        error: {
          maxlength: 'Description cannot exceed 500 characters.',
        },
      },
      cancel: 'Cancel',
      create: 'Create',
      save: 'Save',
    },
    empty: 'No boats found',
    loading: 'Loading boats...',
  },
};

/** Shape contract — every locale file must satisfy this type. */
export type Translations = typeof EN_EN;
