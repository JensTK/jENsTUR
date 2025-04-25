package com.example.avgangstider

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.avgangstider.entur.Quay
import com.example.avgangstider.ui.theme.AvgangsTiderTheme
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant

class MainActivity : ComponentActivity() {

    private val quayIds = mapOf("T-Bane mot byen fra CBP" to "NSR:Quay:11027", "Buss hjem fra CBP" to "NSR:Quay:11090")
    private lateinit var mainHandler: Handler
    //private var lastCheckedTime: Instant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvgangsTiderTheme {
                Text(
                    text = "Laster...",
                    //modifier = Modifier.padding(innerPadding)
                )
            }
        }
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        updateDepartures()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateDepartures() {
        val now = Instant.now()
        val stops = ArrayList<Quay>()
        quayIds.forEach { name, id ->
            runBlocking {
                launch {
                    stops.add(getAvgangsTid(name, id))
                }
            }
        }
        setContent {
            AvgangsTiderTheme {
                Column {
                    TopAppBar(
                        title = { Text(getInstantString(now)) },
                        actions = {
                            Button({ updateDepartures() }) {
                                Text("Hent")
                            }
                        }
                    )
                    stops.forEach { stop ->
                        var isExpanded by remember { mutableStateOf(false) }
                        // surfaceColor will be updated gradually from one color to the other
                        val surfaceColor by animateColorAsState(
                            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp,
                            // surfaceColor color will be changing gradually from primary to surface
                            color = surfaceColor,
                            // animateContentSize will change the Surface size gradually
                            modifier = Modifier.animateContentSize().padding(1.dp)
                        ) {
                            Column(modifier = Modifier.clickable {
                                isExpanded = !isExpanded
                            }.padding(10.dp)) {
                                Text(
                                    text = stop.customName?:stop.getNameString(),
                                    textDecoration = TextDecoration.Underline,
                                    //modifier = Modifier.size(30.dp)
                                )
                                if (isExpanded) {
                                    LazyColumn {
                                        items(stop.estimatedCalls) { call ->
                                            Text(call.getDepartureString(now))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}



suspend fun getAvgangsTid(customName: String, quayId: String): Quay {
    val query = """
    {
        "query": "query StopPlace {
            quay(id: \"$quayId\") {
                id
                name
                description
                publicCode
                stopType
                flexibleArea
                situations {
                    summary {
                        value
                        language
                    }
                    description {
                        value
                        language
                    }
                    advice {
                        value
                        language
                    }
                    validityPeriod {
                        startTime
                        endTime
                    }
                }
                estimatedCalls(numberOfDepartures: 10) {
                    expectedArrivalTime
                    expectedDepartureTime
                    realtime
                    predictionInaccurate
                    realtimeState
                    occupancyStatus
                    cancellation
                    date
                    destinationDisplay {
                        frontText
                        via
                    }
                }
            }
        }"
    }
    """.trimMargin().replace("\n", "")
    val httpClient = HttpClient(CIO)
    val response = httpClient.request("https://api.entur.io/journey-planner/v3/graphql") {
        method = HttpMethod.Post
        headers {
            append(HttpHeaders.ContentType, "application/json")
        }
        setBody(query)
    }
    val objectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule()).configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val quayJson = objectMapper.readValue(response.body() as String) as Map<String, Map<String, Quay>>
    val quay = quayJson["data"]?.get("quay")!!
    quay.customName = customName
    return quay
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AvgangsTiderTheme {
        Greeting("Android")
    }
}