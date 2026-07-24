package org.wikipedia.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiModel(
    val id: String,
    val label: String,
    val modelStr: String,
    val badge: String,
    val badgeColor: String,
    val description: String,
    val baseUrl: String,
    val enableDeepThink: Boolean = false
) {
    companion object {
        private const val ULTIMATE_WORKER_URL = "https://ultimate-ai-worker.haruyhari930.workers.dev/v1/chat/completions"
        private const val QWEN_WORKER_URL = "https://qwen.g4f-dev.workers.dev/v1/chat/completions"

        val ALL_MODELS = listOf(
            AiModel(
                id = "updf-gpt-5-6",
                label = "GPT-5.6 (UPDF Flagship)",
                modelStr = "updf/gpt-5.6",
                badge = "NEW",
                badgeColor = "purple",
                description = "UPDF AI Knowledge Talk Stream API",
                baseUrl = ULTIMATE_WORKER_URL
            ),
            AiModel(
                id = "surfsense-gpt5.4-mini",
                label = "GPT-5.4 Mini",
                modelStr = "surfsense/gpt-5.4-mini-no-login",
                badge = "MINI",
                badgeColor = "teal",
                description = "Surfsense Anonymous Chat API",
                baseUrl = ULTIMATE_WORKER_URL
            ),
            AiModel(
                id = "grok-3",
                label = "Grok 4",
                modelStr = "xai/grok-3",
                badge = "NEW",
                badgeColor = "yellow",
                description = "Grok 3 via xAI",
                baseUrl = ULTIMATE_WORKER_URL
            ),
            AiModel(
                id = "perplexity-copilot",
                label = "Perplexity Copilot",
                modelStr = "perplexity-direct/copilot",
                badge = "COPILOT",
                badgeColor = "cyan",
                description = "Perplexity Copilot via direct Cloudflare worker",
                baseUrl = ULTIMATE_WORKER_URL
            ),
            AiModel(
                id = "baidu-ernie-5.1",
                label = "Baidu Ernie 5.1",
                modelStr = "ernie/ERINE-5.1",
                badge = "BAIDU",
                badgeColor = "red",
                description = "Baidu ERNIE-5.1 unauthenticated SSE proxy",
                baseUrl = ULTIMATE_WORKER_URL
            ),
            AiModel(
                id = "qw-qwen3.7-max",
                label = "Qwen 3.7 Max (Worker)",
                modelStr = "qwen_worker/qwen3.7-max",
                badge = "MAX",
                badgeColor = "violet",
                description = "Alibaba Qwen 3.7 Max — dedicated worker",
                baseUrl = QWEN_WORKER_URL,
                enableDeepThink = true
            ),
            AiModel(
                id = "qw-qwen3.7-plus",
                label = "Qwen 3.7 Plus (Worker)",
                modelStr = "qwen_worker/qwen3.7-plus",
                badge = "PLUS",
                badgeColor = "violet",
                description = "Alibaba Qwen 3.7 Plus — dedicated worker",
                baseUrl = QWEN_WORKER_URL,
                enableDeepThink = true
            )
        )

        val DEFAULT_MODEL = ALL_MODELS[0]
    }
}

fun getBadgeColor(colorName: String): androidx.compose.ui.graphics.Color {
    return when (colorName) {
        "purple" -> androidx.compose.ui.graphics.Color(0xFFA855F7)
        "teal" -> androidx.compose.ui.graphics.Color(0xFF14B8A6)
        "yellow" -> androidx.compose.ui.graphics.Color(0xFFEAB308)
        "cyan" -> androidx.compose.ui.graphics.Color(0xFF06B6D4)
        "red" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
        "violet" -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
        "green" -> androidx.compose.ui.graphics.Color(0xFF22C55E)
        "blue" -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        else -> androidx.compose.ui.graphics.Color(0xFF6B7280)
    }
}

