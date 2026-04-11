export const environment = {
  production: true,
  // Injected at runtime via Docker / Kubernetes env var API_URL
  apiUrl: '${API_URL}',
};
