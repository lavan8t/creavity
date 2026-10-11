using System;
using System.Security.Cryptography;
using System.Text;
using CreaVITy.Windows.Domain.Models;

namespace CreaVITy.Windows.Data.Mesh
{
    /// <summary>
    /// Static helper that generates, validates, and evaluates mesh permission tickets.
    /// Corresponds to the Kotlin <c>object MeshAccessController</c>.
    /// </summary>
    public static class MeshAccessController
    {
        // -------------------------------------------------------------------
        // Signature / ticket creation
        // -------------------------------------------------------------------

        /// <summary>
        /// Computes an HMAC-SHA256 signature over the canonical string
        /// <c>{boardId}:{deviceId}:{role}:{expiresAt}</c> using <paramref name="secretKey"/>.
        /// Returns a lowercase hex string.
        /// </summary>
        public static string GenerateSignature(
            string   secretKey,
            string   boardId,
            string   deviceId,
            MeshRole role,
            long     expiresAt)
        {
            byte[] keyBytes  = Encoding.UTF8.GetBytes(secretKey);
            byte[] dataBytes = Encoding.UTF8.GetBytes($"{boardId}:{deviceId}:{role}:{expiresAt}");

            using var hmac = new HMACSHA256(keyBytes);
            byte[] hash    = hmac.ComputeHash(dataBytes);
            return Convert.ToHexString(hash).ToLowerInvariant();
        }

        /// <summary>
        /// Creates a fully-signed <see cref="PermissionTicket"/> valid for
        /// <paramref name="durationMs"/> milliseconds from now (default 24 h).
        /// </summary>
        public static PermissionTicket CreateTicket(
            string   secretKey,
            string   boardId,
            string   deviceId,
            MeshRole role,
            long     durationMs = 86_400_000L)
        {
            long expiresAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() + durationMs;
            string sig     = GenerateSignature(secretKey, boardId, deviceId, role, expiresAt);

            return new PermissionTicket(
                BoardId:   boardId,
                DeviceId:  deviceId,
                Role:      role,
                ExpiresAt: expiresAt,
                Signature: sig
            );
        }

        // -------------------------------------------------------------------
        // Validation
        // -------------------------------------------------------------------

        /// <summary>
        /// Returns <see langword="true"/> when the ticket's signature is non-blank,
        /// not expired, and matches the expected HMAC.
        /// </summary>
        public static bool ValidateTicket(string secretKey, PermissionTicket ticket)
        {
            if (string.IsNullOrEmpty(ticket.Signature))
                return false;

            if (ticket.ExpiresAt > 0 &&
                DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() > ticket.ExpiresAt)
                return false;

            string expected = GenerateSignature(
                secretKey,
                ticket.BoardId,
                ticket.DeviceId,
                ticket.Role,
                ticket.ExpiresAt);

            return string.Equals(expected, ticket.Signature, StringComparison.OrdinalIgnoreCase);
        }

        // -------------------------------------------------------------------
        // Role checks
        // -------------------------------------------------------------------

        /// <summary>Returns <see langword="true"/> if the ticket's role grants write access.</summary>
        public static bool CanWrite(PermissionTicket ticket) =>
            ticket.Role == MeshRole.Host || ticket.Role == MeshRole.Editor;

        /// <summary>
        /// Returns <see langword="true"/> if the ticket's role grants write access
        /// <em>and</em> the ticket passes HMAC validation.
        /// </summary>
        public static bool CanWrite(string secretKey, PermissionTicket ticket) =>
            CanWrite(ticket) && ValidateTicket(secretKey, ticket);
    }
}
