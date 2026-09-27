package com.example

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration


import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.api.parseVoiceCommand
import kotlinx.coroutines.launch
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import com.example.ui.AiChatScreen
import com.example.ui.ConnectivityScreen
import com.example.ui.HardwareScreen
import com.example.ui.InsightsScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDark
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import com.example.data.VoiceCommandLog

import com.example.ui.RobotViewModel

enum class Screen {
  DASHBOARD,
  CHAT,
  INSIGHTS,
  CONNECTIVITY,
  HARDWARE
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
        var dashboardMode by remember { mutableStateOf("TELEMETRY") }
        val robotViewModel: RobotViewModel = viewModel()
        val robotStatus by robotViewModel.uiState.collectAsStateWithLifecycle()
        val voiceLogs by robotViewModel.voiceLogs.collectAsStateWithLifecycle()
        var showVoiceLogs by remember { mutableStateOf(false) }

                val batteryLevel = robotStatus.batteryLevel
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(robotStatus.signalStrength, robotStatus.heartbeatBpm) {
            if (robotStatus.signalStrength < 20 || robotStatus.heartbeatBpm < 30) {
                snackbarHostState.showSnackbar(
                    message = "CRITICAL: Robot Heartbeat Lost! Signal: ${robotStatus.signalStrength}%, BPM: ${robotStatus.heartbeatBpm}",
                    actionLabel = "DISMISS",
                    duration = SnackbarDuration.Long
                )
            }
        }

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val speechRecognizerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results?.get(0)
                
                if (!spokenText.isNullOrEmpty()) {
                    Toast.makeText(context, "Command: \"$spokenText\"", Toast.LENGTH_SHORT).show()
                    
                    scope.launch {
                        val voiceAction = parseVoiceCommand(spokenText)
                        if (voiceAction != null) {
                            when (voiceAction.action) {
                                "SHOW_LATENCY", "SHOW_CONNECTIVITY" -> {
                                    currentScreen = Screen.CONNECTIVITY
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_INSIGHTS" -> {
                                    currentScreen = Screen.INSIGHTS
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_DASHBOARD" -> {
                                    currentScreen = Screen.DASHBOARD
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "SHOW_HARDWARE" -> {
                                    currentScreen = Screen.HARDWARE
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "OPEN_CHAT" -> {
                                    currentScreen = Screen.CHAT
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                "CALIBRATE_SENSORS" -> {
                                    Toast.makeText(context, "Calibrating sensors via Uplink...", Toast.LENGTH_LONG).show()
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "SUCCESS")
                                }
                                else -> {
                                    Toast.makeText(context, "Unrecognized action: ${voiceAction.action}", Toast.LENGTH_SHORT).show()
                                    robotViewModel.logVoiceCommand(spokenText, voiceAction.action, "UNRECOGNIZED")
                                }
                            }
                        } else {
                            Toast.makeText(context, "Failed to parse command", Toast.LENGTH_SHORT).show()
                            robotViewModel.logVoiceCommand(spokenText, "UNKNOWN", "FAILED")
                        }
                    }
                }
            }
        }

        val startVoiceRecognition = {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                }
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Speech recognition not available.", Toast.LENGTH_SHORT).show()
            }
        }

        if (currentScreen == Screen.CHAT) {
            AiChatScreen(onBack = { currentScreen = Screen.DASHBOARD })
        } else {
            Scaffold(
              modifier = Modifier.fillMaxSize(),
                            containerColor = BackgroundDark,
              snackbarHost = { 
                  SnackbarHost(snackbarHostState) { data ->
                      Snackbar(
                          snackbarData = data,
                          containerColor = Color(0xFFB3261E), // M3 Error Red
                          contentColor = Color.White,
                          actionColor = Color(0xFFFFB4AB) // Light red/pink for action
                      )
                  }
              },
              floatingActionButton = {
                  Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                      FloatingActionButton(
                          onClick = { showVoiceLogs = true },
                          containerColor = SurfaceCard,
                          contentColor = TextPrimary,
                          modifier = Modifier.size(48.dp)
                      ) {
                          Icon(imageVector = Icons.Default.History, contentDescription = "Voice Logs")
                      }
                      FloatingActionButton(
                          onClick = startVoiceRecognition,
                          containerColor = AccentPurple,
                          contentColor = Color(0xFF1D192B)
                      ) {
                          Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Command")
                      }
                  }
              },
              bottomBar = { 
                  BottomNav(
                      currentScreen = currentScreen,
                      onScreenSelected = { currentScreen = it }
                  ) 
              }
            ) { innerPadding ->

              if (showVoiceLogs) {
                  VoiceLogsSheet(logs = voiceLogs, onDismiss = { showVoiceLogs = false })
              }
              if (currentScreen == Screen.DASHBOARD) {
                  Column(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(innerPadding)
                      .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                  ) {
                    Header(batteryLevel = batteryLevel, modifier = Modifier.padding(top = 16.dp))
                    
                    AnimatedVisibility(
                        visible = batteryLevel <= 20,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Surface(
                            color = Color(0xFF331111),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2B8B5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFF2B8B5))
                                Spacer(modifier = Modifier.size(12.dp))
                                Column {
                                    Text("LOW BATTERY ALERT", color = Color(0xFFF2B8B5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Power reserves critical. Please return to charging station.", color = Color(0xFFF2B8B5), fontSize = 10.sp)
                                }
                            }
                        }
                    }


                    AnimatedVisibility(
                        visible = robotStatus.signalStrength < 20 || robotStatus.heartbeatBpm < 30,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Surface(
                            color = Color(0xFF331111),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2B8B5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Disconnected", tint = Color(0xFFF2B8B5))
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Column {
                                        Text("SIGNAL LOST", color = Color(0xFFF2B8B5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Uplink disconnected.", color = Color(0xFFF2B8B5), fontSize = 10.sp)
                                    }
                                }
                                androidx.compose.material3.Button(
                                    onClick = { robotViewModel.reconnect() },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B8B5), contentColor = Color(0xFF331111))
                                ) {
                                    Text("RECONNECT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    DashboardModeToggle(
                        currentMode = dashboardMode,
                        onModeSelected = { dashboardMode = it }
                    )

                    AnimatedContent(
                        targetState = dashboardMode,
                        transitionSpec = {
                            if (targetState == "PREDICTIVE") {
                                (slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn(animationSpec = tween(400))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut(animationSpec = tween(400)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(400)) { width -> -width } + fadeIn(animationSpec = tween(400))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(400)) { width -> width } + fadeOut(animationSpec = tween(400)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "DashboardModeTransition",
                        modifier = Modifier.weight(1f)
                    ) { mode ->
                        if (mode == "TELEMETRY") {
                            Column(modifier = Modifier.fillMaxSize()) {
                                MainCard(modifier = Modifier.weight(1f))
                                GridCards(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    onChatClicked = { currentScreen = Screen.CHAT }
                                )
                            }
                        } else {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = WebViewClient()
                                        loadUrl("file:///android_asset/dashboard.html")
                                    }
                                }
                            )
                        }
                    }
                  }
              } else if (currentScreen == Screen.INSIGHTS) {
                  InsightsScreen(
                      modifier = Modifier
                          .padding(innerPadding)
                          .padding(horizontal = 16.dp)
                  )
              } else if (currentScreen == Screen.CONNECTIVITY) {
                  ConnectivityScreen(
                      modifier = Modifier
                          .padding(innerPadding)
                          .padding(horizontal = 16.dp)
                  )
              } else if (currentScreen == Screen.HARDWARE) {
                  HardwareScreen(
                      modifier = Modifier
                          .padding(innerPadding)
                          .padding(horizontal = 16.dp)
                  )
              }
            }
        }
      }
    }
  }
}

@Composable
fun Header(batteryLevel: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "System Active".uppercase(),
        fontSize = 10.sp,
        letterSpacing = 2.sp,
        color = AccentPurple,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Neural Core v.4",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
      )
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (batteryLevel <= 20) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
          contentDescription = "Battery",
          tint = if (batteryLevel <= 20) Color(0xFFF2B8B5) else AccentPurple,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
          text = "$batteryLevel%",
          color = if (batteryLevel <= 20) Color(0xFFF2B8B5) else TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(SurfaceVariantDark)
          .border(1.dp, Color(0xFF49454F), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.SettingsInputComponent,
          contentDescription = "Settings",
          tint = AccentPurple,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun MainCard(modifier: Modifier = Modifier) {
  val pagerState = rememberPagerState(pageCount = { 2 })

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(32.dp),
    color = SurfaceDark,
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Radial Gradient Background Effect
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.radialGradient(
              colors = listOf(AccentPurple.copy(alpha = 0.15f), Color.Transparent),
              center = Offset.Unspecified,
              radius = 800f
            )
          )
      )

      Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.weight(1f)
        ) { page ->
          when (page) {
            0 -> RealTimeTelemetryView()
            1 -> PredictiveTrendView()
          }
        }

        // Pager Indicators
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
          horizontalArrangement = Arrangement.Center
        ) {
          repeat(2) { iteration ->
            val color = if (pagerState.currentPage == iteration) AccentPurple else SurfaceVariantDark
            Box(
              modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .background(color)
                .size(6.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun RealTimeTelemetryView() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(modifier = Modifier.weight(1f))

    // Center Processing Circle
    Box(
      modifier = Modifier.size(192.dp),
      contentAlignment = Alignment.Center
    ) {
      // Dashed border effect
      Box(
        modifier = Modifier
          .fillMaxSize()
          .drawBehind {
            drawCircle(
              color = AccentPurple.copy(alpha = 0.2f),
              style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
              )
            )
          }
      )

      Box(
        modifier = Modifier
          .size(128.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(AccentPurple, AccentPurpleDark)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Processing".uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF1D192B)
          )
          Text(
            text = "98.4%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D192B)
          )
        }
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    // Stats rows
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      StatRow(
        label = "Data Throughput",
        value = "1.2 GB/s",
        status = "+0.4% ▲"
      )
      StatRow(
        label = "Neural Nodes",
        value = "14,022 Active",
        status = "STABLE"
      )
    }
  }
}

@Composable
fun PredictiveTrendView() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(modifier = Modifier.weight(1f))
    
    Box(
      modifier = Modifier.size(192.dp),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .drawBehind {
             val path = androidx.compose.ui.graphics.Path()
             path.moveTo(0f, size.height * 0.8f)
             path.lineTo(size.width * 0.3f, size.height * 0.5f)
             path.lineTo(size.width * 0.7f, size.height * 0.6f)
             path.lineTo(size.width, size.height * 0.2f)
             
             drawPath(
               path = path,
               color = AccentPurple,
               style = Stroke(
                 width = 4.dp.toPx(),
                 pathEffect = PathEffect.cornerPathEffect(30f)
               )
             )
             
             drawLine(color = SurfaceVariantDark, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 2f)
             drawLine(color = SurfaceVariantDark, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 2f)
          }
      )
      
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(SurfaceDark.copy(alpha = 0.9f))
          .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
         Text("ETA: 14m", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
    
    Spacer(modifier = Modifier.weight(1f))
    
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      StatRow(
        label = "Prediction Confidence",
        value = "99.2%",
        status = "HIGH"
      )
      StatRow(
        label = "Next Anomaly",
        value = "ETA 14m 20s",
        status = "ALERT"
      )
    }
  }
}

@Composable
fun StatRow(label: String, value: String, status: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .drawBehind {
        drawLine(
          color = SurfaceVariantDark,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1.dp.toPx()
        )
      }
      .padding(bottom = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    Column {
      Text(text = label, fontSize = 12.sp, color = TextSecondary)
      Text(
        text = value,
        fontSize = 18.sp,
        fontFamily = FontFamily.Monospace,
        color = TextPrimary
      )
    }
    Text(
      text = status,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      color = AccentPurple,
      modifier = Modifier.padding(bottom = 4.dp)
    )
  }
}

@Composable
fun GridCards(modifier: Modifier = Modifier, onChatClicked: () -> Unit = {}) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Surface(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(24.dp),
      color = SurfaceCard
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Analytics,
          contentDescription = "Analysis",
          tint = AccentPurple
        )
        Text(
          text = "Real-time Analysis",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = TextPrimary
        )
        Text(
          text = "Predictive modeling enabled",
          fontSize = 10.sp,
          color = TextMuted
        )
      }
    }
    
    Surface(
      modifier = Modifier
          .weight(1f)
          .clickable { onChatClicked() },
      shape = RoundedCornerShape(24.dp),
      color = AccentPurple
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.SmartToy,
          contentDescription = "AI Command",
          tint = AccentPurpleDark
        )
        Text(
          text = "AI Command",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = AccentPurpleDark
        )
        Text(
          text = "Ready for voice or text",
          fontSize = 10.sp,
          color = AccentPurpleDark.copy(alpha = 0.7f)
        )
      }
    }
  }
}

@Composable
fun BottomNav(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
  Surface(
    color = SurfaceDark,
    modifier = Modifier.drawBehind {
      drawLine(
        color = SurfaceVariantDark,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1.dp.toPx()
      )
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        icon = Icons.Default.Dashboard,
        label = "Home",
        isSelected = currentScreen == Screen.DASHBOARD,
        onClick = { onScreenSelected(Screen.DASHBOARD) }
      )
      NavItem(
        icon = Icons.Default.Timeline,
        label = "Insights",
        isSelected = currentScreen == Screen.INSIGHTS,
        onClick = { onScreenSelected(Screen.INSIGHTS) }
      )
      NavItem(
        icon = Icons.Default.Memory,
        label = "Uplink",
        isSelected = currentScreen == Screen.CONNECTIVITY,
        onClick = { onScreenSelected(Screen.CONNECTIVITY) }
      )
      NavItem(
        icon = Icons.Default.Build,
        label = "Hardware",
        isSelected = currentScreen == Screen.HARDWARE,
        onClick = { onScreenSelected(Screen.HARDWARE) }
      )
    }
  }
}

@Composable
fun NavItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier.clickable { onClick() },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    if (isSelected) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(AccentPurpleMuted)
          .padding(horizontal = 20.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = Color(0xFFE8DEF8)
        )
      }
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFE8DEF8)
      )
    } else {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = TextMuted,
        modifier = Modifier.padding(vertical = 4.dp)
      )
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = TextMuted
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLogsSheet(logs: List<VoiceCommandLog>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        contentColor = TextPrimary
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Command Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            if (logs.isEmpty()) {
                Text("No voice commands recorded yet.", color = TextMuted)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log ->
                        Surface(color = SurfaceCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val statusIcon = when (log.status) {
                                    "SUCCESS" -> Icons.Default.CheckCircle
                                    "UNRECOGNIZED" -> Icons.Default.HelpOutline
                                    else -> Icons.Default.Error
                                }
                                val statusColor = when (log.status) {
                                    "SUCCESS" -> Color(0xFF4CAF50)
                                    "UNRECOGNIZED" -> Color(0xFFFFEB3B)
                                    else -> Color(0xFFF44336)
                                }
                                
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = log.status,
                                    tint = statusColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("\"${log.commandText}\"", fontStyle = FontStyle.Italic, color = TextPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Action: ${log.actionParsed}", fontSize = 12.sp, color = AccentPurple)
                                }
                                Text(log.status, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardModeToggle(currentMode: String, onModeSelected: (String) -> Unit) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val modes = listOf("TELEMETRY", "PREDICTIVE")
            val labels = listOf("Real-time Telemetry", "Predictive Trend")
            
            modes.forEachIndexed { index, mode ->
                val isSelected = currentMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentPurple else Color.Transparent)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labels[index],
                        color = if (isSelected) Color(0xFF1D192B) else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
