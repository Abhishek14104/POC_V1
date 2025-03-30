package com.example.wear.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material3.Typography
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private var activityContext: Context? = null

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private var mobileDeviceConnected: Boolean = false

    // Payload string items
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private var messageContent by mutableStateOf("")
    private var messageLog by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityContext = this

        // Enables Always-on
        ambientController = AmbientModeSupport.attach(this as FragmentActivity)

        setContent {
            WearTheme {
                // Applying padding to the Scaffold content area
                Box(modifier = Modifier.padding(8.dp)) {
                    MainScreen(
                        messageContent = messageContent,
                        onMessageContentChange = { messageContent = it },
                        messageLog = messageLog,
                        onSendMessageClick = { sendMessage() },
                        mobileDeviceConnected = mobileDeviceConnected
                    )
                }
            }
        }
    }

    private fun sendMessage() {
        if (mobileDeviceConnected) {
            if (messageContent.isNotEmpty()) {

                val nodeId: String = messageEvent?.sourceNodeId ?: return
                // Set the data of the message to be the bytes of the Uri.
                val payload: ByteArray = messageContent.toByteArray()

                // Send the message
                val sendMessageTask = Wearable.getMessageClient(activityContext!!).sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("send1", "Message sent successfully")
                        messageLog += "\n$messageContent (Sent to mobile)"
                    } else {
                        Log.d("send1", "Message failed.")
                    }
                }
            } else {
                Toast.makeText(activityContext, "Message content is empty. Please enter some message and proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        // Handle data changes (empty for now)
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        // Handle capability changes (empty for now)
    }

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:" +
                        p0.requestId + " " + messageEventPath + " " + s1
            )

            // Send back a message to acknowledge that the receiver activity is open
            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                try {
                    val nodeId: String = p0.sourceNodeId
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    val sendMessageTask = Wearable.getMessageClient(activityContext!!).sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                    Log.d(TAG_MESSAGE_RECEIVED, "Acknowledgement message successfully with payload : $returnPayloadAck")

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")
                            mobileDeviceConnected = true
                            messageLog += "\nMobile device connected." // Update message log here
                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                try {
                    Log.d(TAG_MESSAGE_RECEIVED, "Received message: $s1")
                    // Append received message to the message log
                    messageLog += "\n$s1 - (Received from mobile)" // Update message log here
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAmbientCallback(): AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }
}

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color.Blue,
            secondary = Color.Green
        ),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun MainScreen(
    messageContent: String,
    onMessageContentChange: (String) -> Unit,
    messageLog: String,
    onSendMessageClick: () -> Unit,
    mobileDeviceConnected: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mobileDeviceConnected) {
            TextField(
                value = messageContent,
                onValueChange = onMessageContentChange,
                label = { Text("Message") },
                textStyle = TextStyle(fontSize = 6.sp),
                modifier = Modifier
                    .padding(horizontal = 10.dp , vertical = 2.dp)
                    .height(20.dp)
            )
            Button(
                onClick = onSendMessageClick,
                modifier = Modifier.padding(1.dp)
            ) {
                Text("Send", fontSize = 8.sp)
            }

            Text(
                text = "Mobile device is connected",
                color = Color.Green,
                fontSize = 8.sp,
//                modifier = Modifier.padding(0.dp)
            )

            Text(
                text = "Message Log:",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 6.sp),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp)
            )

            BasicTextField(
                value = messageLog,
                onValueChange = {},
                readOnly = true,
                textStyle = TextStyle(fontSize = 6.sp, color = Color.White),
                modifier = Modifier
                    .heightIn(60.dp)
                    .padding(horizontal = 5.dp, vertical = 0.dp)
            )
        } else {
            Text(
                text = "No mobile device connected.",
                color = Color.Red,
                fontSize = 8.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WearTheme {
        MainScreen(
            messageContent = "Sample Message",
            onMessageContentChange = {},
            messageLog = "Sample Log",
            onSendMessageClick = {},
            mobileDeviceConnected = true
        )
    }
}
