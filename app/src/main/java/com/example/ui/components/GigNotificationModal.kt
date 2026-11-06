package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Booking
import com.example.ui.theme.*

@Composable
fun GigNotificationModal(
  booking: Booking,
  onAccept: () -> Unit,
  onDecline: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, MediTealPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // App header pill
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MediTealLight)
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(MediTealPrimary)
          )
          Text(
            text = "MediGig ZA • Booking Request",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MediTealPrimary
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Medical Symbol Badge
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MediTealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Payments,
            contentDescription = null,
            tint = MediTealPrimary,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "New Gig Request",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = booking.hirerName,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = MediTealPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shift Details Card
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MediCardBorder, RoundedCornerShape(16.dp)),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Date
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MediTealPrimary,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Dates",
                  fontSize = 11.sp,
                  color = MediTextMuted
                )
                Text(
                  text = "${booking.startDate} - ${booking.endDate} (${booking.totalDays} ${if (booking.totalDays == 1) "day" else "days"})",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Time Slot
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MediTealPrimary,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Shift Time",
                  fontSize = 11.sp,
                  color = MediTextMuted
                )
                Text(
                  text = booking.timeSlot,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Location (Rule 2 & 3: distance in km, not full address until hired!)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MediTealPrimary,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Distance",
                  fontSize = 11.sp,
                  color = MediTextMuted
                )
                Text(
                  text = "${"%.1f".format(booking.distanceKm)}km away (${booking.hirerGeneralLocation})",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Exact street address revealed upon acceptance",
                  fontSize = 10.sp,
                  color = MediTextSecondary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Payout Banner
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
          color = MediTealLight
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Guaranteed Payout",
                fontSize = 11.sp,
                color = MediTealOnContainer
              )
              Text(
                text = "Held in ZAR Escrow",
                fontSize = 10.sp,
                color = MediTextSecondary
              )
            }
            Text(
              text = "R${"%,d".format(booking.totalAmountZar)} ZAR",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Funds released securely 5-10 business days after shift completion.",
          fontSize = 10.sp,
          color = MediTextMuted,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SPECIFICATION RULE: "Two predefined reply buttons ONLY: [Available - Accept] / [Not Available - Decline]. No typing."
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Decline Button
          OutlinedButton(
            onClick = onDecline,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MediDangerRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MediDangerRed.copy(alpha = 0.5f))
          ) {
            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Decline",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // Accept Button
          Button(
            onClick = onAccept,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MediTealPrimary,
              contentColor = Color.White
            )
          ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Accept",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
