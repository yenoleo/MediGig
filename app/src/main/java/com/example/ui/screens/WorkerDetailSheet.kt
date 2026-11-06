package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FreelancerProfile
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WorkerDetailDialog(
  freelancer: FreelancerProfile,
  onHireClicked: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Professional Profile",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
          }
        }

        Divider(color = MediCardBorder)

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Profile Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            InitialsAvatar(
              initials = freelancer.initials,
              size = 64.dp,
              backgroundColor = MediTealPrimary,
              showOnlineDot = true,
              isOnline = freelancer.isAvailableToday
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = freelancer.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = freelancer.title,
                fontSize = 13.sp,
                color = MediTealPrimary,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                DistanceChip(distanceKm = freelancer.distanceKm)
                VerifiedBadge(text = freelancer.hpcsaOrSancNumber)
              }
            }
          }

          // Rate & Escrow Summary Box
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp)),
            color = MediTealLight
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Freelancer Daily Rate",
                  fontSize = 11.sp,
                  color = MediTealOnContainer
                )
                Text(
                  text = "R${"%,d".format(freelancer.dailyRateZar)} ZAR / day",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediTealPrimary
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MediMintLight,
                modifier = Modifier.padding(2.dp)
              ) {
                Text(
                  text = if (freelancer.isAvailableToday) "Available Today" else "Book in Advance",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MediMint,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          // Experience & Council Info
          AffectCard {
            Text(
              text = "Registration & Experience",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Council Number:", fontSize = 12.sp, color = MediTextSecondary)
              Text(freelancer.hpcsaOrSancNumber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Years Practicing:", fontSize = 12.sp, color = MediTextSecondary)
              Text("${freelancer.yearsExperience} Years Experience", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Max Travel Radius:", fontSize = 12.sp, color = MediTextSecondary)
              Text("Up to ${freelancer.maxDistanceKm}km", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          // Education Degrees
          AffectCard {
            Text(
              text = "Education & Degrees",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            freelancer.degrees.forEach { degree ->
              Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Outlined.School, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(16.dp))
                Text(degree, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }

          // Certifications
          AffectCard {
            Text(
              text = "Verified Certifications",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            freelancer.certifications.forEach { cert ->
              Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MediMint, modifier = Modifier.size(16.dp))
                Text(cert, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }

          // Standard Shift Availability
          AffectCard {
            Text(
              text = "Shift Options",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            freelancer.preferredTimeSlots.forEach { slot ->
              Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MediCyanAccent, modifier = Modifier.size(16.dp))
                Text(slot, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }

          // Security & Escrow Note
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Filled.Lock, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(16.dp))
              Text(
                text = "Protected by MediGig Escrow: Practice address is shared upon acceptance. Payment released 5-10 days after completion.",
                fontSize = 10.sp,
                color = MediTextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Book Button
        PrimaryAffectButton(
          text = "Book Locum Shift",
          onClick = {
            onDismiss()
            onHireClicked()
          },
          icon = Icons.Filled.Payments
        )
      }
    }
  }
}
