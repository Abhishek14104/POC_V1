package com.example.mobile

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private var wearableDeviceConnected: Boolean by mutableStateOf(false)
    private var messageEvent: MessageEvent? = null
    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private val MESSAGE_ITEM_RECEIVED_PATH = "/message-item-received"

    private val _messageLog = mutableStateOf("")
    val messageLog: String get() = _messageLog.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(this)
        }
    }

    @Composable
    fun MainScreen(mainActivity: MainActivity) {
        val context = LocalContext.current
        var message by remember { mutableStateOf("") }

        val messageLog by remember { mainActivity._messageLog }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Wear OS Communication", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { mainActivity.initialiseDevicePairing(context as Activity) }) {
                Text("Check Wearables")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mainActivity.wearableDeviceConnected) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Enter Message") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    mainActivity.sendMessage(context, message)
                    mainActivity._messageLog.value += "\nSent: $message" // Log Sent Message
                }) {
                    Text("Send Message to Wearable")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Message Log:", style = MaterialTheme.typography.headlineSmall)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(messageLog.lines().size) { index ->
                        Text(text = messageLog.lines()[index], style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }


    private fun getConnectedNodes(context: Context): List<String> {
        val nodeListTask = Wearable.getNodeClient(context).connectedNodes
        return try {
            val nodes = Tasks.await(nodeListTask)
            nodes.map { it.id }
        } catch (e: Exception) {
            Log.e("getConnectedNodes", "Failed to get connected nodes", e)
            emptyList()
        }
    }


    private fun sendMessage(context: Context, message: String) {
        if (message.isEmpty()) {
            Toast.makeText(context, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        launch(Dispatchers.IO) {
            val nodes = getConnectedNodes(context)
            if (nodes.isNotEmpty()) {
                val nodeId = nodes.first()
                val payload: ByteArray = message.toByteArray(StandardCharsets.UTF_8)
                val sendMessageTask = Wearable.getMessageClient(context).sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
                try {
                    Tasks.await(sendMessageTask)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("sendMessage", "Message sent successfully")
                } catch (e: Exception) {
                    Log.e("sendMessage", "Message failed to send", e)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No connected wearable devices found", Toast.LENGTH_SHORT).show()
                }
                Log.e("sendMessage", "No connected nodes found")
            }
        }
}

    private fun initialiseDevicePairing(tempAct: Activity) {
        launch(Dispatchers.Default) {
            val getNodesResBool = getNodes(tempAct.applicationContext)
            withContext(Dispatchers.Main) {
                wearableDeviceConnected = getNodesResBool[0] && getNodesResBool[1]
            }
        }
    }

    private fun getNodes(context: Context): BooleanArray {
        val resBool = BooleanArray(2)
        val nodeListTask = Wearable.getNodeClient(context).connectedNodes
        try {
            val nodes = Tasks.await(nodeListTask)
            if (nodes.isNotEmpty()) {
                val nodeId = nodes.first().id
                val payload: ByteArray = wearableAppCheckPayload.toByteArray()
                val sendMessageTask = Wearable.getMessageClient(context)
                    .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                Tasks.await(sendMessageTask)
                resBool[0] = true
                if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                    resBool[1] = true
                    return resBool
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return resBool
    }

    override fun onMessageReceived(p0: MessageEvent) {
        val receivedMessage = String(p0.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Received message: $receivedMessage")

        if (p0.path == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
            if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                Log.d("onMessageReceived", "Duplicate ACK ignored")
                return
            }
            currentAckFromWearForAppOpenCheck = receivedMessage
        }

        MainScope().launch(Dispatchers.Main) {
            _messageLog.value += "\nReceived: $receivedMessage"
        }
    }


    override fun onDataChanged(p0: DataEventBuffer) {}
    override fun onCapabilityChanged(p0: CapabilityInfo) {}

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }
}
