package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MediGigRepository
import com.example.model.ActiveRole
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun BookingsScreen(
  onSelectBooking: (Booking) -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by MediGigRepository.bookings.collectAsState()
  val activeRole by MediGigRepository.activeRole.collectAsState()
  var selectedTab by remember { mutableStateOf(0) }
  val tabTitles = listOf("All Gigs", "Escrow / Active", "Pending", "Completed")

  val filteredBookings = remember(bookings, selectedTab) {
    when (selectedTab) {
      1 -> bookings.filter { it.status == BookingStatus.ACCEPTED_ESCROW || it.status == BookingStatus.PENDING_RELEASE || it.status == BookingStatus.IN_PROGRESS || it.status == BookingStatus.DISPUTED }
      2 -> bookings.filter { it.status == BookingStatus.PENDING_RESPONSE }
      3 -> bookings.filter { it.status == BookingStatus.FUNDS_RELEASED || it.status == BookingStatus.DECLINED || it.status == BookingStatus.CANCELLED }
      else -> bookings
    }
  }

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
          text = if (activeRole == ActiveRole.HIRER) "Practice Bookings & Escrow Gigs" else "My Healthcare Shifts",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Protected with South African ZAR Escrow & POPIA Compliance",
          fontSize = 12.sp,
          color = MediTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          edgePadding = 0.dp,
          indicator = {},
          divider = {},
          containerColor = Color.Transparent
        ) {
          tabTitles.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Surface(
              modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(20.dp)
                )
                .clickable { selectedTab = index },
              color = if (isSelected) MediTealPrimary else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = title,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    if (filteredBookings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.EventNote,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MediTextMuted
          )
          Text(
            text = "No gigs in this category",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MediTextSecondary
          )
          Text(
            text = "New bookings and shifts will appear here.",
            fontSize = 12.sp,
            color = MediTextMuted,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
      ) {
        items(filteredBookings) { booking ->
          BookingSummaryCard(
            booking = booking,
            isHirer = activeRole == ActiveRole.HIRER,
            onClick = { onSelectBooking(booking) },
            onCancelBeforeAccept = {
              if (activeRole == ActiveRole.HIRER) {
                MediGigRepository.cancelBookingBeforeAcceptance(booking.id)
              } else {
                MediGigRepository.declineBooking(booking.id)
              }
            },
            onReleaseEscrow = {
              if (activeRole == ActiveRole.HIRER) {
                MediGigRepository.releaseEscrowToWorker(booking.id)
              } else {
                MediGigRepository.markJobCompleteByWorker(booking.id)
              }
            }
          )
        }
      }
    }
  }
}

// Full Job Completion, Escrow Release & Dispute Dialog
@Composable
fun BookingDetailDialog(
  booking: Booking,
  isHirer: Boolean,
  onDismiss: () -> Unit,
  onConfirmCompletion: (rating: Float) -> Unit,
  onMarkCompleteByWorker: () -> Unit,
  onRaiseDispute: (reason: String) -> Unit,
  onResolveDisputeRefund: () -> Unit,
  onResolveDisputeRelease: () -> Unit
) {
  var rating by remember { mutableStateOf(5f) }
  var showDisputeModal by remember { mutableStateOf(false) }
  var disputeReason by remember { mutableStateOf("Shift hours or attendance discrepancy") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.90f)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Top bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Gig #${booking.id}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "South African Healthcare Locum Booking",
                fontSize = 11.sp,
                color = MediTealPrimary
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Filled.Close, contentDescription = "Close")
            }
          }

          Divider(color = MediCardBorder)

          // Status Banner
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            color = when (booking.status) {
              BookingStatus.FUNDS_RELEASED -> MediMintLight
              BookingStatus.PENDING_RELEASE -> MediEscrowLight
              BookingStatus.ACCEPTED_ESCROW -> MediTealLight
              BookingStatus.DISPUTED -> MediDangerLight
              else -> MaterialTheme.colorScheme.surfaceVariant
            }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = when (booking.status) {
                  BookingStatus.FUNDS_RELEASED -> Icons.Filled.CheckCircle
                  BookingStatus.DISPUTED -> Icons.Filled.ReportProblem
                  else -> Icons.Filled.Lock
                },
                contentDescription = null,
                tint = when (booking.status) {
                  BookingStatus.FUNDS_RELEASED -> MediMint
                  BookingStatus.DISPUTED -> MediDangerRed
                  else -> MediTealPrimary
                }
              )
              Column {
                Text(
                  text = booking.status.label,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${booking.startDate} - ${booking.endDate} • ${booking.timeSlot}",
                  fontSize = 11.sp,
                  color = MediTextSecondary
                )
              }
            }
          }

          // Counterparty Info (Doctor or Hospital)
          AffectCard {
            Text(
              text = if (isHirer) "Freelance Healthcare Professional" else "Hiring Practice",
              fontSize = 12.sp,
              color = MediTextMuted,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              InitialsAvatar(
                initials = if (isHirer) booking.freelancerInitials else "NC",
                size = 44.dp,
                backgroundColor = MediTealPrimary
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isHirer) booking.freelancerName else booking.hirerName,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isHirer) booking.freelancerTitle else booking.hirerGeneralLocation,
                  fontSize = 12.sp,
                  color = MediTealPrimary
                )
              }
            }

            // Core rule #2 & #3 check: full address revealed ONLY if accepted!
            if (booking.status == BookingStatus.ACCEPTED_ESCROW ||
              booking.status == BookingStatus.IN_PROGRESS ||
              booking.status == BookingStatus.PENDING_RELEASE ||
              booking.status == BookingStatus.DISPUTED ||
              booking.status == BookingStatus.FUNDS_RELEASED
            ) {
              Spacer(modifier = Modifier.height(8.dp))
              Divider(color = MediCardBorder)
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(16.dp))
                Column {
                  Text(
                    text = "Full Address (Unlocked upon acceptance):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediTealPrimary
                  )
                  Text(
                    text = booking.hirerFullAddress,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          // Rating section (if Hirer and completed/pending release)
          if (isHirer && (booking.status == BookingStatus.PENDING_RELEASE || booking.status == BookingStatus.FUNDS_RELEASED)) {
            AffectCard {
              Text("Rate the Freelancer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                (1..5).forEach { star ->
                  IconButton(
                    onClick = { rating = star.toFloat() },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                      contentDescription = "$star stars",
                      tint = if (star <= rating) MediEscrowAmber else MediTextMuted,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
                Text("${rating.toInt()}.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            }
          }

          // Escrow Protection Card
          AffectCard(backgroundColor = MediTealLight.copy(alpha = 0.4f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Filled.Security, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(20.dp))
              Text("Protected ZAR Escrow", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "R${"%,d".format(booking.totalAmountZar)} held safely in escrow. Funds release after 5-10 business days unless a dispute is flagged.",
              fontSize = 12.sp,
              color = MediTextSecondary,
              lineHeight = 16.sp
            )
            if (booking.status == BookingStatus.DISPUTED) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Reason: ${booking.disputeReason ?: "Pending administrative audit"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MediDangerRed
              )
            }
          }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (booking.status == BookingStatus.DISPUTED) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  onResolveDisputeRefund()
                  onDismiss()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MediDangerRed),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Refund Hirer", fontSize = 12.sp)
              }
              Button(
                onClick = {
                  onResolveDisputeRelease()
                  onDismiss()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MediMint),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Release to Pro", fontSize = 12.sp)
              }
            }
          } else if (isHirer && booking.status == BookingStatus.PENDING_RELEASE) {
            PrimaryAffectButton(
              text = "Confirm Completion & Release R${"%,d".format(booking.totalAmountZar)}",
              onClick = {
                onConfirmCompletion(rating)
                onDismiss()
              },
              icon = Icons.Filled.CheckCircle
            )
            OutlinedButton(
              onClick = { showDisputeModal = true },
              modifier = Modifier.fillMaxWidth().height(42.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed)
            ) {
              Icon(Icons.Outlined.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Raise Shift Dispute / Freeze Escrow", fontSize = 12.sp)
            }
          } else if (!isHirer && booking.status == BookingStatus.ACCEPTED_ESCROW) {
            PrimaryAffectButton(
              text = "Mark Job as Complete",
              onClick = {
                onMarkCompleteByWorker()
                onDismiss()
              },
              icon = Icons.Filled.CheckCircle
            )
          }

          SecondaryAffectButton(
            text = "Close",
            onClick = onDismiss
          )
        }
      }
    }
  }

  // Raise Dispute Dialog
  if (showDisputeModal) {
    Dialog(onDismissRequest = { showDisputeModal = false }) {
      Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Flag Shift Dispute", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MediDangerRed)
          Text("Escrow payment will be frozen pending review by MediGig compliance.", fontSize = 12.sp, color = MediTextSecondary)

          val reasons = listOf(
            "Shift hours / attendance discrepancy",
            "Healthcare worker no-show or late arrival",
            "Clinical specialty task mismatch",
            "Emergency hospital cancellation"
          )

          reasons.forEach { r ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { disputeReason = r }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(selected = disputeReason == r, onClick = { disputeReason = r }, colors = RadioButtonDefaults.colors(selectedColor = MediDangerRed))
              Text(r, fontSize = 12.sp)
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showDisputeModal = false }, modifier = Modifier.weight(1f)) {
              Text("Cancel")
            }
            Button(
              onClick = {
                onRaiseDispute(disputeReason)
                showDisputeModal = false
                onDismiss()
              },
              modifier = Modifier.weight(1.3f),
              colors = ButtonDefaults.buttonColors(containerColor = MediDangerRed)
            ) {
              Text("Freeze Escrow")
            }
          }
        }
      }
    }
  }
}
