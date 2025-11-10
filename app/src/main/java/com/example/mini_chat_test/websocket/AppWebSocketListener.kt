import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class AppWebSocketListener(
    private val onMessage: (String) -> Unit,
    private val onStatus: (String) -> Unit
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        onStatus("Connected")
        onMessage("--- Connection established ---")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Handle byte messages if needed
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        onStatus("Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onStatus("Failed: ${t.message}")
        t.printStackTrace()
    }
}
