package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.StreakCelebration
import com.example.sim.formatMoney
import com.example.ui.theme.*

@Composable
fun StreakCelebrationDialog(
    streak: StreakCelebration,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.3f) }
    LaunchedEffect(streak) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(SunnyAmber, CoralRose)),
                width = 2.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .testTag("streak_celebration_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(SunnyAmber, CoralRose))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "STREAK HARI EFISIEN!",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = CoralRose,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "${streak.streakDays} Hari Berturut-turut 🔥",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Operasi cepat, tanpa backlog parah, dan laba positif konsisten!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SunnyTintBg),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SunnyTintBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus Efisiensi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text("+${streak.multiplierPercent}% Multiplier", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = CoralRoseDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus Kas Hari Ini", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text("+${formatMoney(streak.bonusCash)} 🪙", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus XP Tycoon", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text("+${streak.bonusXp} XP ⭐", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = OceanBlueDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_dismiss_streak")
                ) {
                    Text("Pertahankan Rentetan Kemenangan! 🔥", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}
