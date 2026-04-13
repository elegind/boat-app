/**
 * Represents a Boat resource as returned by the API.
 * createdAt is an ISO 8601 UTC string (serialized from Java Instant).
 * updatedAt is optional — null for records that have never been updated via the API.
 */
export interface Boat {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt?: string;
}

