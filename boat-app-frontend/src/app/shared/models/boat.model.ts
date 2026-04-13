/**
 * Represents a Boat resource as returned by the API.
 * createdAt is an ISO 8601 UTC string (serialized from Java Instant).
 */
export interface Boat {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

