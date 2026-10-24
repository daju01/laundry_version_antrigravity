package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.SimulationEngine
import com.example.sim.formatMoneyCompact
import com.example.ui.theme.*

/**
 * Clean, Compact, Single-Line-Safe Zero-Late Streak & Customer Satisfaction Banner Card.
 * Fixes the narrow phone width wrapping bug where "+15% Bonus" wrapped vertically.
 */
@Composable
fun ZeroLateStreakCard(
    engine: SimulationEngine,
    onOpenDetails: () -> Unit,
    onClaimSatisfactionBonus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = engine.getZeroLateStreakInfo()
    val isFlawless = info.todayLateOrders == 0
    val bonusPct = ((info.visualSatisfactionBonusMultiplier - 1.0) * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isFlawless) listOf(MintEmerald, OceanBluePrimary)
                else listOf(CoralRose, SunnyAmber)
            ),
            width = 1.5.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A10B981))
            .clickable { onOpenDetails() }
            .testTag("zero_late_streak_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (isFlawless) listOf(Color(0xFFECFDF5), Color(0xFFF0F9FF))
                        else listOf(Color(0xFFFFF1F2), Color(0xFFFFFBEB))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFlawless) Brush.linearGradient(listOf(MintEmerald, TealAccent))
                            else Brush.linearGradient(listOf(CoralRose, SunnyAmber))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFlawless) "🛡️" else "⚠️",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "STREAK TEPAT WAKTU: ${info.currentStreakDays} HARI",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFlawless) MintEmeraldDark else CoralRoseDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SunnyTintBg)
                                .border(1.dp, SunnyTintBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "+$bonusPct%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SunnyAmberDark,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isFlawless) {
                            "0 Telat Hari Ini • Hadiah +${formatMoneyCompact(info.bonusCashAccumulated)}"
                        } else {
                            "${info.todayLateOrders} Cucian Telat • Ketuk ruang 3D agar lebih cepat!"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    com.example.audio.GameSoundManager.playStreakBonusChime()
                    onClaimSatisfactionBonus()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!engine.satisfactionBonusClaimedToday) MintEmerald else Slate300
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("claim_satisfaction_bonus_btn")
            ) {
                Text(
                    text = if (!engine.satisfactionBonusClaimedToday) "🎁 Klaim" else "✅ Klaim",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (!engine.satisfactionBonusClaimedToday) Color.White else Slate700,
                    maxLines = 1
                )
            }
        }
    }
}
