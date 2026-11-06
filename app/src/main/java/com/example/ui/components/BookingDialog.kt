package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FreelancerProfile
import com.example.ui.theme.*

@Composable
fun BookingDialog(
  freelancer: FreelancerProfile,
  walletBalanceZar: Double,
  onConfirmBooking: (startDate: String, endDate: String, days: Int, timeSlot: String) -> Unit,
  onNeedTopUp: () -> Unit,
  onDismiss: () -> Unit
) {
  var selectedDays by remember { mutableStateOf(1) }
  var selectedSlot by remember { mutableStateOf(freelancer.preferredTimeSlots.firstOrNull() ?: "08:00 - 16:00 (Standard)") }
  var startDateText by remember { mutableStateOf("15 Nov 2026") }
  var endDateText by remember { mutableStateOf("15 Nov 2026") }

  val totalZar = freelancer.dailyRateZar * selectedDays
  val hasEnoughBalance = walletBalanceZar >= totalZar

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          InitialsAvatar(
            initials = freelancer.initials,
            size = 46.dp,
            backgroundColor = MediTealPrimary
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Hire ${freelancer.fullName}",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${freelancer.title} • ${"%.1f".format(freelancer.distanceKm)}km away",
              fontSize = 12.sp,
              color = MediTextSecondary
            )
          }
        }

        Divider(color = MediCardBorder)

        // Duration selector (1 to 5 days)
        Text(
          text = "Booking Duration",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(1, 2, 3, 5).forEach { days ->
            val isSelected = selectedDays == days
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(8.dp)
                )
                .clickable {
                  selectedDays = days
                  endDateText = if (days == 1) "15 Nov 2026" else "${14 + days} Nov 2026"
                },
              color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "$days ${if (days == 1) "Day" else "Days"}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSelected) MediTealPrimary else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        // Shift Time Slot
        Text(
          text = "Shift Time Slot",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          freelancer.preferredTimeSlots.forEach { slot ->
            val isSelected = selectedSlot == slot
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(8.dp)
                )
                .background(if (isSelected) MediTealLight.copy(alpha = 0.5f) else Color.Transparent)
                .clickable { selectedSlot = slot }
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { selectedSlot = slot },
                colors = RadioButtonDefaults.colors(selectedColor = MediTealPrimary)
              )
              Text(
                text = slot,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Cost Breakdown & Escrow Card
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MediCardBorder, RoundedCornerShape(12.dp)),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Daily Rate:", fontSize = 12.sp, color = MediTextSecondary)
              Text("R${"%,d".format(freelancer.dailyRateZar)} / day", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Days:", fontSize = 12.sp, color = MediTextSecondary)
              Text("$selectedDays ${if (selectedDays == 1) "day" else "days"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Divider(color = MediCardBorder, modifier = Modifier.padding(vertical = 2.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total Amount (Escrow):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
              Text("R${"%,d".format(totalZar)} ZAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
            }
          }
        }

        // Wallet Balance Check Alert
        if (hasEnoughBalance) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MediTealLight)
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(16.dp))
            Text(
              text = "Wallet balance: R${"%,.0f".format(walletBalanceZar)}. Amount will be locked into Escrow.",
              fontSize = 11.sp,
              color = MediTealOnContainer
            )
          }
        } else {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MediDangerLight)
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MediDangerRed, modifier = Modifier.size(16.dp))
            Column {
              Text(
                text = "Insufficient Wallet Funds!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MediDangerRed
              )
              Text(
                text = "Balance: R${"%,.0f".format(walletBalanceZar)}. Need R${"%,.0f".format(totalZar - walletBalanceZar)} more.",
                fontSize = 11.sp,
                color = MediDangerRed
              )
            }
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }

          if (hasEnoughBalance) {
            Button(
              onClick = {
                onConfirmBooking(startDateText, endDateText, selectedDays, selectedSlot)
              },
              modifier = Modifier
                .weight(1.5f)
                .height(46.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
            ) {
              Text("Lock & Send Request")
            }
          } else {
            Button(
              onClick = onNeedTopUp,
              modifier = Modifier
                .weight(1.5f)
                .height(46.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MediCyanAccent)
            ) {
              Text("Add Funds via Paystack")
            }
          }
        }
      }
    }
  }
}
