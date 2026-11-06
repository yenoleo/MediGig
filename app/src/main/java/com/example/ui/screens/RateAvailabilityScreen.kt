package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediGigRepository
import com.example.ui.components.AffectCard
import com.example.ui.components.PrimaryAffectButton
import com.example.ui.theme.*

@Composable
fun RateAvailabilityScreen(
  onSaved: () -> Unit,
  modifier: Modifier = Modifier
) {
  val myProfile by MediGigRepository.myFreelancerProfile.collectAsState()

  var dailyRate by remember { mutableStateOf(myProfile.dailyRateZar.toFloat()) }
  var travelRadiusKm by remember { mutableStateOf(myProfile.maxDistanceKm.toFloat()) }
  var availableDays by remember { mutableStateOf(myProfile.availableDays) }
  var selectedSlots by remember { mutableStateOf(myProfile.preferredTimeSlots.toSet()) }

  val allTimeSlots = listOf(
    "08:00 - 16:00 (Standard Shift)",
    "07:00 - 19:00 (12h Day Call)",
    "19:00 - 07:00 (Night Duty)",
    "Weekend On-Call (24h)"
  )

  val daysInMonth = (1..30).toList()
  val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Set Rate & Availability",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Control your earnings and working schedule in South Africa",
          fontSize = 12.sp,
          color = MediTextSecondary
        )
      }
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Daily Rate Card (Matching Screen 3 in mockup)
      AffectCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MediTealLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Outlined.Payments, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
            }
            Text("Daily Locum Rate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Text(
            text = "ZAR R${"%,d".format(dailyRate.toInt())} / day",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MediTealPrimary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Slider(
          value = dailyRate,
          onValueChange = { dailyRate = it },
          valueRange = 800f..6000f,
          steps = 25,
          colors = SliderDefaults.colors(
            thumbColor = MediTealPrimary,
            activeTrackColor = MediTealPrimary
          )
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Min: R800", fontSize = 11.sp, color = MediTextMuted)
          Text("SA Market Avg: R1,800 - R3,500", fontSize = 11.sp, color = MediTealPrimary, fontWeight = FontWeight.Medium)
          Text("Max: R6,000", fontSize = 11.sp, color = MediTextMuted)
        }
      }

      // Interactive Availability Calendar (Screen 3 in mockup)
      AffectCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
            Text("Availability Calendar • Nov 2026", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "${availableDays.size} days active",
            fontSize = 11.sp,
            color = MediMint,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday headers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          weekDays.forEach { day ->
            Text(
              text = day,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MediTextMuted,
              modifier = Modifier.width(36.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 30 Days Grid
        val rows = (daysInMonth.size + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          for (r in 0 until rows) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              for (c in 0 until 7) {
                val dayNum = r * 7 + c + 1
                if (dayNum <= daysInMonth.size) {
                  val isAvailable = availableDays.contains(dayNum)
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(if (isAvailable) MediMint else MaterialTheme.colorScheme.surfaceVariant)
                      .clickable {
                        availableDays = if (isAvailable) {
                          availableDays - dayNum
                        } else {
                          availableDays + dayNum
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$dayNum",
                      fontSize = 12.sp,
                      fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Normal,
                      color = if (isAvailable) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                  }
                } else {
                  Spacer(modifier = Modifier.size(36.dp))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MediMint))
            Text("Available for booking", fontSize = 11.sp, color = MediTextSecondary)
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MediCardBorder))
            Text("Unavailable", fontSize = 11.sp, color = MediTextSecondary)
          }
        }
      }

      // Shift Time Slots
      AffectCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
          Text("Preferred Shift Slots", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          allTimeSlots.forEach { slot ->
            val isChecked = selectedSlots.contains(slot)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                  1.dp,
                  if (isChecked) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(8.dp)
                )
                .background(if (isChecked) MediTealLight.copy(alpha = 0.5f) else Color.Transparent)
                .clickable {
                  selectedSlots = if (isChecked) selectedSlots - slot else selectedSlots + slot
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Checkbox(
                checked = isChecked,
                onCheckedChange = {
                  selectedSlots = if (it) selectedSlots + slot else selectedSlots - slot
                },
                colors = CheckboxDefaults.colors(checkedColor = MediTealPrimary)
              )
              Text(slot, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }
      }

      // Max Distance Willing to Travel Slider (Screen 3 in mockup)
      AffectCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Outlined.NearMe, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
            Text("Max Travel Distance", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Text(
            text = "${travelRadiusKm.toInt()} km",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MediTealPrimary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Slider(
          value = travelRadiusKm,
          onValueChange = { travelRadiusKm = it },
          valueRange = 5f..60f,
          steps = 10,
          colors = SliderDefaults.colors(
            thumbColor = MediTealPrimary,
            activeTrackColor = MediTealPrimary
          )
        )

        Text(
          text = "You will be shown to hospital clinics within ${travelRadiusKm.toInt()}km of your location.",
          fontSize = 11.sp,
          color = MediTextMuted
        )
      }

      // Save Button
      PrimaryAffectButton(
        text = "Save & Update Profile",
        onClick = {
          MediGigRepository.updateFreelancerSettings(
            dailyRate = dailyRate.toInt(),
            maxDistance = travelRadiusKm.toInt(),
            timeSlots = selectedSlots.toList(),
            availableDays = availableDays
          )
          onSaved()
        },
        icon = Icons.Filled.Check
      )
    }
  }
}
