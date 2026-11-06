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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediGigRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
  onNavigateToSearch: () -> Unit,
  onNavigateToBookings: () -> Unit,
  onNavigateToWallet: () -> Unit,
  onNavigateToRateSettings: () -> Unit,
  onOpenBookingDetail: (Booking) -> Unit,
  onNavigateToVault: () -> Unit = {},
  onNavigateToRequisitions: () -> Unit = {},
  onNavigateToDisputes: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val activeRole by MediGigRepository.activeRole.collectAsState()
  val bookings by MediGigRepository.bookings.collectAsState()
  val wallet by MediGigRepository.walletState.collectAsState()
  val myFreelancer by MediGigRepository.myFreelancerProfile.collectAsState()
  val credentials by MediGigRepository.credentialsVault.collectAsState()
  val requisitions by MediGigRepository.hospitalRequisitions.collectAsState()
  val disputes by MediGigRepository.disputeCases.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
  ) {
    // 0. QUICK ACCESS: HIGH-PRIORITY HEALTHCARE PORTALS
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Clinical Operations & Portals",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MediTealLight
          ) {
            Text(
              text = "PRO FEATURES",
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MediTealPrimary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Vault Card
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(12.dp))
              .clickable { onNavigateToVault() },
            color = Color.White
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MediTealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = MediTealPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
              Text("Credential Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("HPCSA & SANC (${credentials.size})", fontSize = 10.sp, color = MediTextMuted)
            }
          }

          // Requisition Card
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(12.dp))
              .clickable { onNavigateToRequisitions() },
            color = Color.White
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Campaign,
                  contentDescription = null,
                  tint = Color(0xFF2563EB),
                  modifier = Modifier.size(16.dp)
                )
              }
              Text("Hospital Shifts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Requisitions (${requisitions.size})", fontSize = 10.sp, color = MediTextMuted)
            }
          }

          // Dispute Card
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(12.dp))
              .clickable { onNavigateToDisputes() },
            color = Color.White
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Gavel,
                  contentDescription = null,
                  tint = MediDangerRed,
                  modifier = Modifier.size(16.dp)
                )
              }
              Text("Dispute Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Escrow Review (${disputes.size})", fontSize = 10.sp, color = MediTextMuted)
            }
          }
        }
      }
    }
    if (activeRole == ActiveRole.HIRER) {
      // HIRER DASHBOARD (Hospital / Clinic View - Screen 6 in PDF)
      item {
        HirerHeaderCard(
          practiceName = MediGigRepository.myPracticeProfile.name,
          location = MediGigRepository.myPracticeProfile.generalLocation,
          activeGigsCount = bookings.count { it.status == BookingStatus.ACCEPTED_ESCROW || it.status == BookingStatus.IN_PROGRESS },
          onPostShift = onNavigateToSearch
        )
      }

      // Wallet & Escrow Snapshot for Hirer
      item {
        HirerWalletSnapshot(
          availableBalanceZar = wallet.availableBalanceZar,
          escrowHeldZar = wallet.escrowPendingZar,
          onManageWallet = onNavigateToWallet
        )
      }

      // Action: Discover / Book
      item {
        PrimaryAffectButton(
          text = "+ Find & Book Healthcare Pro",
          onClick = onNavigateToSearch,
          icon = Icons.Outlined.PersonSearch
        )
      }

      // Active / Recent Bookings
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Active & Pending Gigs",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          TextButton(onClick = onNavigateToBookings) {
            Text("View All (${bookings.size})", color = MediTealPrimary, fontSize = 12.sp)
          }
        }
      }

      items(bookings.take(3)) { booking ->
        BookingSummaryCard(
          booking = booking,
          isHirer = true,
          onClick = { onOpenBookingDetail(booking) },
          onCancelBeforeAccept = { MediGigRepository.cancelBookingBeforeAcceptance(booking.id) },
          onReleaseEscrow = { MediGigRepository.releaseEscrowToWorker(booking.id) }
        )
      }

    } else {
      // FREELANCER DASHBOARD (Get Hired View - Screen 1 & 9 in PDF)
      item {
        FreelancerHeaderCard(
          profile = myFreelancer,
          onManageRate = onNavigateToRateSettings
        )
      }

      // Earnings Snapshot Card (Wallet, Escrow Pending, Paid History)
      item {
        FreelancerEarningsCard(
          wallet = wallet,
          onManageWallet = onNavigateToWallet
        )
      }

      // Quick Setup: Availability & Rates Shortcut
      item {
        AffectCard(onClick = onNavigateToRateSettings) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(MediTealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.CalendarMonth,
                  contentDescription = null,
                  tint = MediTealPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text(
                  text = "Rate: R${"%,d".format(myFreelancer.dailyRateZar)}/day • ${myFreelancer.maxDistanceKm}km Radius",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Tap to update calendar & daily rate",
                  fontSize = 11.sp,
                  color = MediTextSecondary
                )
              }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MediTextMuted)
          }
        }
      }

      // Incoming Gig Requests / Gigs list
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "My Gigs & Shift Requests",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          TextButton(onClick = onNavigateToBookings) {
            Text("See All", color = MediTealPrimary, fontSize = 12.sp)
          }
        }
      }

      items(bookings.take(3)) { booking ->
        BookingSummaryCard(
          booking = booking,
          isHirer = false,
          onClick = { onOpenBookingDetail(booking) },
          onCancelBeforeAccept = { MediGigRepository.declineBooking(booking.id) },
          onReleaseEscrow = { MediGigRepository.markJobCompleteByWorker(booking.id) }
        )
      }
    }
  }
}

@Composable
fun HirerHeaderCard(
  practiceName: String,
  location: String,
  activeGigsCount: Int,
  onPostShift: () -> Unit
) {
  AffectCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Outlined.LocalHospital, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(16.dp))
          Text(
            text = "Hirer Practice Portal",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MediTealPrimary
          )
        }
        Text(
          text = practiceName,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = location,
          fontSize = 12.sp,
          color = MediTextSecondary
        )
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MediMintLight
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(MediMint)
          )
          Text(
            text = "$activeGigsCount Active",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MediMint
          )
        }
      }
    }
  }
}

@Composable
fun FreelancerHeaderCard(
  profile: FreelancerProfile,
  onManageRate: () -> Unit
) {
  var isAvailable by remember { mutableStateOf(profile.isAvailableToday) }

  AffectCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      InitialsAvatar(
        initials = profile.initials,
        size = 54.dp,
        backgroundColor = MediTealPrimary,
        showOnlineDot = true,
        isOnline = isAvailable
      )

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = profile.fullName,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          VerifiedBadge(text = "HPCSA Verified")
        }
        Text(
          text = profile.title,
          fontSize = 12.sp,
          color = MediTealPrimary,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = profile.generalLocation,
          fontSize = 11.sp,
          color = MediTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Divider(color = MediCardBorder)
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isAvailable) MediMint else MediTextMuted)
        )
        Text(
          text = if (isAvailable) "Available for Locum Calls" else "Off Duty / Unavailable",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Switch(
        checked = isAvailable,
        onCheckedChange = { isAvailable = it },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = MediTealPrimary
        )
      )
    }
  }
}

@Composable
fun HirerWalletSnapshot(
  availableBalanceZar: Double,
  escrowHeldZar: Double,
  onManageWallet: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MediCardBorder, RoundedCornerShape(16.dp)),
    color = MaterialTheme.colorScheme.surface
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
          Text("Practice Wallet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(
          text = "Paystack Ready",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MediTealPrimary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Available
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp)),
          color = MediTealLight
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Available Balance", fontSize = 11.sp, color = MediTealOnContainer)
            Text(
              "R${"%,.0f".format(availableBalanceZar)}",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary
            )
          }
        }

        // In Escrow
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp)),
          color = MediEscrowLight
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Held in Escrow", fontSize = 11.sp, color = Color(0xFF92400E))
            Text(
              "R${"%,.0f".format(escrowHeldZar)}",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MediEscrowAmber
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(
          onClick = onManageWallet,
          contentPadding = PaddingValues(0.dp)
        ) {
          Text("Manage Wallet & Funds →", fontSize = 12.sp, color = MediTealPrimary, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
fun FreelancerEarningsCard(
  wallet: WalletState,
  onManageWallet: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MediCardBorder, RoundedCornerShape(16.dp)),
    color = MaterialTheme.colorScheme.surface
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("My Earnings & Escrow", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onManageWallet, contentPadding = PaddingValues(0.dp)) {
          Text("Withdraw Funds →", fontSize = 12.sp, color = MediTealPrimary, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Balance
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp)),
          color = MediTealLight
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Ready to Payout", fontSize = 10.sp, color = MediTealOnContainer)
            Text(
              "R${"%,.0f".format(wallet.availableBalanceZar)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary
            )
          }
        }

        // Escrow
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp)),
          color = MediEscrowLight
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Escrow Pending", fontSize = 10.sp, color = Color(0xFF92400E))
            Text(
              "R${"%,.0f".format(wallet.escrowPendingZar)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MediEscrowAmber
            )
          }
        }

        // Total Earned
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp)),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Total Earned", fontSize = 10.sp, color = MediTextMuted)
            Text(
              "R${"%,.0f".format(wallet.totalEarnedZar)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
fun BookingSummaryCard(
  booking: Booking,
  isHirer: Boolean,
  onClick: () -> Unit,
  onCancelBeforeAccept: () -> Unit,
  onReleaseEscrow: () -> Unit
) {
  AffectCard(onClick = onClick) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isHirer) booking.freelancerName else booking.hirerName,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isHirer) booking.freelancerTitle else booking.timeSlot,
          fontSize = 12.sp,
          color = MediTealPrimary,
          fontWeight = FontWeight.Medium
        )
      }
      StatusBadge(status = booking.status)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Location / Distance rule: KM only, unless accepted!
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MediTextMuted, modifier = Modifier.size(14.dp))
        Text(
          text = if (booking.status == BookingStatus.ACCEPTED_ESCROW || booking.status == BookingStatus.IN_PROGRESS) {
            "${booking.hirerFullAddress} (${"%.1f".format(booking.distanceKm)}km)"
          } else {
            "${"%.1f".format(booking.distanceKm)}km away (${booking.hirerGeneralLocation})"
          },
          fontSize = 11.sp,
          color = MediTextSecondary
        )
      }

      Text(
        text = "${booking.startDate} - ${booking.endDate}",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = MediDivider)
    Spacer(modifier = Modifier.height(8.dp))

    // Escrow Payout amount & Quick action button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "R${"%,d".format(booking.totalAmountZar)} ZAR",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MediTealPrimary
        )
        Text(
          text = if (booking.status == BookingStatus.PENDING_RELEASE) "Releases in ${booking.escrowDaysRemaining} days" else "Protected in Escrow",
          fontSize = 10.sp,
          color = MediTextMuted
        )
      }

      when {
        booking.status == BookingStatus.PENDING_RESPONSE && isHirer -> {
          OutlinedButton(
            onClick = onCancelBeforeAccept,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed)
          ) {
            Text("Cancel (Refund)", fontSize = 11.sp)
          }
        }

        booking.status == BookingStatus.PENDING_RESPONSE && !isHirer -> {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
              onClick = onCancelBeforeAccept, // decline
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed)
            ) {
              Text("Decline", fontSize = 11.sp)
            }
            Button(
              onClick = { MediGigRepository.acceptBooking(booking.id) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
            ) {
              Text("Accept", fontSize = 11.sp)
            }
          }
        }

        booking.status == BookingStatus.ACCEPTED_ESCROW && !isHirer -> {
          Button(
            onClick = onReleaseEscrow, // mark complete
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediMint)
          ) {
            Text("Mark Complete", fontSize = 11.sp)
          }
        }

        booking.status == BookingStatus.PENDING_RELEASE && isHirer -> {
          Button(
            onClick = onReleaseEscrow, // confirm completion & release early
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
          ) {
            Text("Release Escrow", fontSize = 11.sp)
          }
        }

        else -> {
          TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text("Details →", fontSize = 12.sp, color = MediTealPrimary)
          }
        }
      }
    }
  }
}
