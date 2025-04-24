import java.net.URI

object DomainUtils {
    private val DOMAIN_REGEX =  Regex("""^(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,}$""")

    fun normalize(input: String): String {
        val trimmed = input.trim()
        val url = if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) trimmed else "http://$trimmed"

        val host = try {
            URI(url).host ?: throw IllegalArgumentException("Cannot extract host from URL: $url")
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: $url", e)
        }

        if (!DOMAIN_REGEX.matches(host)) throw IllegalArgumentException("Invalid domain name: $host")
        return host.lowercase()
    }
}