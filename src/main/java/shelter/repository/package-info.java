/**
 * Repository interfaces that abstract persistent storage for each aggregate root.
 * Service-layer code depends only on these interfaces, keeping business logic
 * independent of the underlying storage format (CSV, in-memory, or otherwise).
 */
package shelter.repository;
