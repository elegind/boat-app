/**
 * Matches the uniform JSON error envelope returned by GlobalExceptionHandler
 * on the Spring Boot backend.
 */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}
