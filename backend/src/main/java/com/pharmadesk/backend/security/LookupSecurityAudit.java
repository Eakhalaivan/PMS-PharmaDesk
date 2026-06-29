package com.pharmadesk.backend.security;

/**
 * =========================================================
 * SECURITY AUDIT: Public Endpoints Review
 * =========================================================
 * 
 * Previous Configuration:
 * - /api/config/** and /api/lookups/** were exposed as permitAll().
 * 
 * Audit Findings:
 * 1. /api/lookups/** endpoints often return master data which might include
 *    medicine names, categories, suppliers, and pricing information.
 *    Exposing these publicly could lead to scraping of internal pharmaceutical 
 *    stock and pricing data, which is a significant business risk.
 * 2. /api/config/** could potentially expose internal application configurations,
 *    feature flags, or system preferences that should only be accessible 
 *    to authenticated staff or administrators.
 * 
 * Resolution:
 * Both /api/lookups/** and /api/config/** have been removed from the 
 * permitAll() list in SecurityConfig.java. They are now secured and require 
 * authentication by default via the `.anyRequest().authenticated()` rule.
 * 
 * Note: If any specific lookup (e.g., public branch locations) genuinely needs 
 * to be public, it should be extracted into a separate controller with a clearly 
 * defined scope and explicitly added to permitAll().
 */
public class LookupSecurityAudit {
    // This class serves as documentation for the security audit of public endpoints.
}
