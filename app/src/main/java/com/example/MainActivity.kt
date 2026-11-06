package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.AuthManager
import com.example.data.MediGigRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class MediGigNavDestination(val label: String) {
  HOME("Home"),
  SEARCH("Find Pro"),
  BOOKINGS("Gigs"),
  WALLET("Wallet"),
  RATE_SETTINGS("Schedule"),
  PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Silent background auto-sign-in on startup
    AuthManager.attemptAutoSignIn(this, lifecycleScope)

    setContent {
      MediGigTheme {
        val currentUser by AuthManager.currentUser.collectAsState()
        if (currentUser == null) {
          AuthScreen(
            onAuthSuccess = {
              // User signed in successfully
            }
          )
        } else {
          MediGigMainApp()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediGigMainApp() {
  var currentDestination by remember { mutableStateOf(MediGigNavDestination.HOME) }
  val activeRole by MediGigRepository.activeRole.collectAsState()
  val wallet by MediGigRepository.walletState.collectAsState()
  val pendingNotification by MediGigRepository.pendingNotification.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  // Modal Dialog States
  var detailFreelancer by remember { mutableStateOf<FreelancerProfile?>(null) }
  var bookingFreelancer by remember { mutableStateOf<FreelancerProfile?>(null) }
  var detailBooking by remember { mutableStateOf<Booking?>(null) }
  var showPaystackAddFunds by remember { mutableStateOf(false) }
  var showWithdrawal by remember { mutableStateOf(false) }

  // Handle system back navigation if a sub-screen or modal is active
  BackHandler(enabled = currentDestination != MediGigNavDestination.HOME) {
    currentDestination = MediGigNavDestination.HOME
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Brand Logo + Title
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MediTealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = "MediGig Logo",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "MediGig",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "ZA",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediTealPrimary
                )
              }
              Text(
                text = "Medical Locum & Gig Escrow",
                fontSize = 10.sp,
                color = MediTextMuted
              )
            }
          }

          // Top Action Controls: Mode Switcher & Notification Bell
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Mode Switcher Pill
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                  MediGigRepository.toggleRole()
                  coroutineScope.launch {
                    val roleLabel = if (activeRole == ActiveRole.HIRER) "Freelance Healthcare Pro" else "Hirer Practice"
                    snackbarHostState.showSnackbar("Switched to $roleLabel mode")
                  }
                }
                .border(1.dp, MediTealPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
              color = MediTealLight
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = if (activeRole == ActiveRole.HIRER) Icons.Filled.Business else Icons.Filled.MedicalServices,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp),
                  tint = MediTealPrimary
                )
                Text(
                  text = if (activeRole == ActiveRole.HIRER) "Hirer ⇄" else "Doctor ⇄",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediTealPrimary
                )
              }
            }

            // Notification Bell (opens incoming gig request if available)
            Box {
              IconButton(
                onClick = {
                  if (pendingNotification != null) {
                    // Open notification
                  } else {
                    MediGigRepository.triggerSampleNotification()
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Loaded pending gig request")
                    }
                  }
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Notifications,
                  contentDescription = "Notifications",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }

              if (pendingNotification != null) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MediDangerRed)
                )
              }
            }
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.navigationBarsPadding()
      ) {
        NavigationBarItem(
          selected = currentDestination == MediGigNavDestination.HOME,
          onClick = { currentDestination = MediGigNavDestination.HOME },
          icon = {
            Icon(
              imageVector = if (currentDestination == MediGigNavDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
              contentDescription = "Home"
            )
          },
          label = { Text("Home", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MediTealPrimary,
            selectedTextColor = MediTealPrimary,
            indicatorColor = MediTealLight
          )
        )

        NavigationBarItem(
          selected = currentDestination == MediGigNavDestination.SEARCH,
          onClick = { currentDestination = MediGigNavDestination.SEARCH },
          icon = {
            Icon(
              imageVector = if (currentDestination == MediGigNavDestination.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
              contentDescription = "Find Doctors"
            )
          },
          label = { Text("Discover", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MediTealPrimary,
            selectedTextColor = MediTealPrimary,
            indicatorColor = MediTealLight
          )
        )

        NavigationBarItem(
          selected = currentDestination == MediGigNavDestination.BOOKINGS,
          onClick = { currentDestination = MediGigNavDestination.BOOKINGS },
          icon = {
            Icon(
              imageVector = if (currentDestination == MediGigNavDestination.BOOKINGS) Icons.Filled.Assignment else Icons.Outlined.Assignment,
              contentDescription = "Gigs"
            )
          },
          label = { Text("My Gigs", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MediTealPrimary,
            selectedTextColor = MediTealPrimary,
            indicatorColor = MediTealLight
          )
        )

        NavigationBarItem(
          selected = currentDestination == MediGigNavDestination.WALLET,
          onClick = { currentDestination = MediGigNavDestination.WALLET },
          icon = {
            Icon(
              imageVector = if (currentDestination == MediGigNavDestination.WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
              contentDescription = "Wallet"
            )
          },
          label = { Text("Wallet", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MediTealPrimary,
            selectedTextColor = MediTealPrimary,
            indicatorColor = MediTealLight
          )
        )

        NavigationBarItem(
          selected = currentDestination == MediGigNavDestination.PROFILE,
          onClick = { currentDestination = MediGigNavDestination.PROFILE },
          icon = {
            Icon(
              imageVector = if (currentDestination == MediGigNavDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
              contentDescription = "Profile"
            )
          },
          label = { Text("Profile", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MediTealPrimary,
            selectedTextColor = MediTealPrimary,
            indicatorColor = MediTealLight
          )
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentDestination) {
        MediGigNavDestination.HOME -> {
          HomeScreen(
            onNavigateToSearch = { currentDestination = MediGigNavDestination.SEARCH },
            onNavigateToBookings = { currentDestination = MediGigNavDestination.BOOKINGS },
            onNavigateToWallet = { currentDestination = MediGigNavDestination.WALLET },
            onNavigateToRateSettings = { currentDestination = MediGigNavDestination.RATE_SETTINGS },
            onOpenBookingDetail = { booking -> detailBooking = booking }
          )
        }

        MediGigNavDestination.SEARCH -> {
          SearchScreen(
            onSelectFreelancer = { pro -> detailFreelancer = pro }
          )
        }

        MediGigNavDestination.BOOKINGS -> {
          BookingsScreen(
            onSelectBooking = { booking -> detailBooking = booking }
          )
        }

        MediGigNavDestination.WALLET -> {
          WalletScreen(
            onOpenAddFunds = { showPaystackAddFunds = true },
            onOpenWithdrawal = { showWithdrawal = true }
          )
        }

        MediGigNavDestination.RATE_SETTINGS -> {
          RateAvailabilityScreen(
            onSaved = {
              currentDestination = MediGigNavDestination.HOME
              coroutineScope.launch {
                snackbarHostState.showSnackbar("Rate and availability calendar updated!")
              }
            }
          )
        }

        MediGigNavDestination.PROFILE -> {
          ProfileScreen(
            onNavigateToRateSettings = { currentDestination = MediGigNavDestination.RATE_SETTINGS },
            onNavigateToWallet = { currentDestination = MediGigNavDestination.WALLET }
          )
        }
      }
    }
  }

  // --- MODALS & DIALOGS ---

  // 1. Worker Detail Sheet
  detailFreelancer?.let { pro ->
    WorkerDetailDialog(
      freelancer = pro,
      onHireClicked = {
        bookingFreelancer = pro
      },
      onDismiss = { detailFreelancer = null }
    )
  }

  // 2. Booking / Hire Flow Dialog (with Escrow Calculation)
  bookingFreelancer?.let { pro ->
    BookingDialog(
      freelancer = pro,
      walletBalanceZar = wallet.availableBalanceZar,
      onConfirmBooking = { startDate, endDate, days, timeSlot ->
        when (val result = MediGigRepository.createBooking(pro, startDate, endDate, days, timeSlot)) {
          is MediGigRepository.BookingResult.Success -> {
            bookingFreelancer = null
            coroutineScope.launch {
              val total = pro.dailyRateZar * days
              snackbarHostState.showSnackbar("Booking sent! R${"%,d".format(total)} locked in Escrow.")
            }
            currentDestination = MediGigNavDestination.BOOKINGS
          }
          is MediGigRepository.BookingResult.InsufficientFunds -> {
            bookingFreelancer = null
            showPaystackAddFunds = true
          }
          is MediGigRepository.BookingResult.DateConflict -> {
            coroutineScope.launch {
              snackbarHostState.showSnackbar("Conflict: ${result.message}")
            }
          }
        }
      },
      onNeedTopUp = {
        bookingFreelancer = null
        showPaystackAddFunds = true
      },
      onDismiss = { bookingFreelancer = null }
    )
  }

  // 3. Gig Detail & Escrow Release Dialog (Screen 9 in mockups)
  detailBooking?.let { booking ->
    BookingDetailDialog(
      booking = booking,
      isHirer = activeRole == ActiveRole.HIRER,
      onDismiss = { detailBooking = null },
      onConfirmCompletion = { rating ->
        MediGigRepository.releaseEscrowToWorker(booking.id, rating)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Completion confirmed! Escrow funds released to ${booking.freelancerName}.")
        }
      },
      onMarkCompleteByWorker = {
        MediGigRepository.markJobCompleteByWorker(booking.id)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Job marked complete! Escrow enters 5-10 day release period.")
        }
      },
      onRaiseDispute = { reason ->
        MediGigRepository.raiseDispute(booking.id, reason)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Dispute raised. Escrow frozen for compliance review.")
        }
      },
      onResolveDisputeRefund = {
        MediGigRepository.resolveDisputeRefundHirer(booking.id)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Dispute resolved: R${"%,d".format(booking.totalAmountZar)} refunded to Hirer.")
        }
      },
      onResolveDisputeRelease = {
        MediGigRepository.releaseEscrowToWorker(booking.id, 5.0f)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Dispute resolved: Escrow released to ${booking.freelancerName}.")
        }
      }
    )
  }

  // 4. Paystack Add Funds Modal
  if (showPaystackAddFunds) {
    PaystackAddFundsModal(
      currentBalanceZar = wallet.availableBalanceZar,
      onAddFunds = { amount, method ->
        MediGigRepository.topUpWalletPaystack(amount, method)
        showPaystackAddFunds = false
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Added R${"%,.0f".format(amount)} to wallet via Paystack!")
        }
      },
      onDismiss = { showPaystackAddFunds = false }
    )
  }

  // 5. Withdrawal Modal to South African Bank
  if (showWithdrawal) {
    WithdrawalModal(
      availableBalanceZar = wallet.availableBalanceZar,
      onWithdraw = { amount, bank, account ->
        val success = MediGigRepository.withdrawToBank(amount, bank, account)
        showWithdrawal = false
        if (success) {
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Withdrawal of R${"%,.0f".format(amount)} initiated to $bank.")
          }
        }
      },
      onDismiss = { showWithdrawal = false }
    )
  }

  // 6. Hire Notification Modal (Strict rule: Two predefined buttons ONLY: Accept / Decline, No typing)
  pendingNotification?.let { notif ->
    GigNotificationModal(
      booking = notif,
      onAccept = {
        MediGigRepository.acceptBooking(notif.id)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Gig Accepted! Full address for ${notif.hirerName} unlocked.")
        }
      },
      onDecline = {
        MediGigRepository.declineBooking(notif.id)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Gig Declined. Hirer escrow refunded.")
        }
      },
      onDismiss = {
        MediGigRepository.dismissNotification()
      }
    )
  }
}
