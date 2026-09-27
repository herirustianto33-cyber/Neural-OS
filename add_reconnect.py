import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

reconnect_ui = """
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

                    MainCard"""

content = re.sub(r'                    MainCard', reconnect_ui, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

