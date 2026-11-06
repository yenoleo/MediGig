package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MediGigRepository
import com.example.model.*
import com.example.ui.components.AffectCard
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDisputePortalScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val disputeCases by MediGigRepository.disputeCases.collectAsState()
  var selectedStatusFilter by remember { mutableStateOf<DisputeStatus?>(null) }
  var selectedCaseDetail by remember { mutableStateOf<DisputeCase?>(null) }

  // Action resolution dialog state
  var resolutionTargetCase by remember { mutableStateOf<DisputeCase?>(null) }
  var resolutionType by remember { mutableStateOf<ResolutionAction?>(null) }

  val context = LocalContext.current

  val activeCount = disputeCases.count { it.status == DisputeStatus.ACTIVE_INVESTIGATION }
  val mediationCount = disputeCases.count { it.status == DisputeStatus.UNDER_MEDIATION }
  val resolvedCount = disputeCases.count {
    it.status == DisputeStatus.RESOLVED_RELEASED ||
    it.status == DisputeStatus.RESOLVED_REFUNDED ||
    it.status == DisputeStatus.RESOLVED_SPLIT
  }
  val totalFrozenEscrow = disputeCases
    .filter { it.status == DisputeStatus.ACTIVE_INVESTIGATION || it.status == DisputeStatus.UNDER_MEDIATION }
    .sumOf { it.disputedAmountZar }

  val filteredCases = remember(disputeCases, selectedStatusFilter) {
    if (selectedStatusFilter == null) disputeCases else disputeCases.filter { it.status == selectedStatusFilter }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag("dispute_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Admin Dispute Portal",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFFEF2F2)
                ) {
                  Text(
                    text = "ESCROW MEDIATION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MediDangerRed,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "Independent Clinical Shift & Escrow Tribunal",
                fontSize = 12.sp,
                color = MediTextMuted
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Mediation Trust Metrics Card
      item {
        AffectCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color(0xFFFFFBEB) // Light warm amber
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Gavel,
                  contentDescription = null,
                  tint = Color(0xFFD97706),
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Escrow Freeze & Tribunal Status",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFF92400E)
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7)
              ) {
                Text(
                  text = "ZAR Escrow Protected",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB45309),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Active Disputes", fontSize = 11.sp, color = MediTextMuted)
                Text("$activeCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MediDangerRed)
              }
              Column {
                Text("Under Mediation", fontSize = 11.sp, color = MediTextMuted)
                Text("$mediationCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
              }
              Column {
                Text("Resolved", fontSize = 11.sp, color = MediTextMuted)
                Text("$resolvedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MediSuccessGreen)
              }
              Column {
                Text("Frozen Escrow", fontSize = 11.sp, color = MediTextMuted)
                Text("R${"%,d".format(totalFrozenEscrow)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
            }

            Text(
              text = "MediGig SA escrow is automatically locked when a dispute is filed. Neither party receives funds until administrative mediation is finalized. Decisions are binding under the MediGig ZA Marketplace Agreement.",
              fontSize = 11.sp,
              color = Color(0xFF78350F),
              lineHeight = 15.sp
            )
          }
        }
      }

      // 2. Status Filter Tabs
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterChip(
              selected = selectedStatusFilter == null,
              onClick = { selectedStatusFilter = null },
              label = { Text("All Cases (${disputeCases.size})") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == DisputeStatus.ACTIVE_INVESTIGATION,
              onClick = {
                selectedStatusFilter = if (selectedStatusFilter == DisputeStatus.ACTIVE_INVESTIGATION) null else DisputeStatus.ACTIVE_INVESTIGATION
              },
              label = { Text("Active ($activeCount)") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == DisputeStatus.UNDER_MEDIATION,
              onClick = {
                selectedStatusFilter = if (selectedStatusFilter == DisputeStatus.UNDER_MEDIATION) null else DisputeStatus.UNDER_MEDIATION
              },
              label = { Text("Mediation ($mediationCount)") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == DisputeStatus.RESOLVED_RELEASED || selectedStatusFilter == DisputeStatus.RESOLVED_REFUNDED || selectedStatusFilter == DisputeStatus.RESOLVED_SPLIT,
              onClick = {
                selectedStatusFilter = if (selectedStatusFilter == DisputeStatus.RESOLVED_SPLIT) null else DisputeStatus.RESOLVED_SPLIT
              },
              label = { Text("Resolved ($resolvedCount)") },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
            )
          }
        }
      }

      // 3. Dispute List Header
      item {
        Text(
          text = "DISPUTE DOCKET (${filteredCases.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MediTextMuted,
          letterSpacing = 0.5.sp
        )
      }

      // 4. Dispute Cards
      if (filteredCases.isEmpty()) {
        item {
          AffectCard(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = MediSuccessGreen, modifier = Modifier.size(40.dp))
              Text("No disputes under selected filter", fontWeight = FontWeight.Bold)
              Text("All shifts completed peacefully with funds released", fontSize = 12.sp, color = MediTextMuted)
            }
          }
        }
      } else {
        items(filteredCases, key = { it.id }) { dispute ->
          DisputeCaseCard(
            dispute = dispute,
            onCardClick = { selectedCaseDetail = dispute },
            onResolveAction = { action ->
              resolutionTargetCase = dispute
              resolutionType = action
            }
          )
        }
      }
    }
  }

  // Action Dialog
  if (resolutionTargetCase != null && resolutionType != null) {
    ResolveDisputeDialog(
      dispute = resolutionTargetCase!!,
      action = resolutionType!!,
      onDismiss = {
        resolutionTargetCase = null
        resolutionType = null
      },
      onConfirm = { note ->
        when (resolutionType!!) {
          ResolutionAction.RELEASE_DOCTOR -> {
            MediGigRepository.resolveDisputeReleaseToDoctor(resolutionTargetCase!!.id, note)
            Toast.makeText(context, "Resolved: 100% Escrow released to ${resolutionTargetCase!!.practitionerName}", Toast.LENGTH_LONG).show()
          }
          ResolutionAction.REFUND_HOSPITAL -> {
            MediGigRepository.resolveDisputeRefundHospital(resolutionTargetCase!!.id, note)
            Toast.makeText(context, "Resolved: 100% Escrow refunded to ${resolutionTargetCase!!.hospitalName}", Toast.LENGTH_LONG).show()
          }
          ResolutionAction.SPLIT_5050 -> {
            MediGigRepository.resolveDisputeSplit5050(resolutionTargetCase!!.id, note)
            Toast.makeText(context, "Resolved: 50/50 Settlement Executed", Toast.LENGTH_LONG).show()
          }
        }
        resolutionTargetCase = null
        resolutionType = null
      }
    )
  }

  // Dossier Detail Dialog
  selectedCaseDetail?.let { dispute ->
    DisputeDossierDialog(
      dispute = dispute,
      onDismiss = { selectedCaseDetail = null }
    )
  }
}

enum class ResolutionAction {
  RELEASE_DOCTOR,
  REFUND_HOSPITAL,
  SPLIT_5050
}

@Composable
fun DisputeCaseCard(
  dispute: DisputeCase,
  onCardClick: () -> Unit,
  onResolveAction: (ResolutionAction) -> Unit,
  modifier: Modifier = Modifier
) {
  val isResolved = dispute.status == DisputeStatus.RESOLVED_RELEASED ||
    dispute.status == DisputeStatus.RESOLVED_REFUNDED ||
    dispute.status == DisputeStatus.RESOLVED_SPLIT

  AffectCard(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("dispute_card_${dispute.caseNumber}")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Header: Case Number, Escrow Amount, Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "#${dispute.caseNumber}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF1F5F9)
          ) {
            Text(
              text = dispute.bookingId,
              fontSize = 10.sp,
              color = MediTextMuted,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isResolved) MediSuccessGreen.copy(alpha = 0.15f) else Color(0xFFFEF2F2)
        ) {
          Text(
            text = dispute.status.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isResolved) MediSuccessGreen else MediDangerRed,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // Reason & Locked Amount
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = dispute.reasonCategory,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Opened ${dispute.createdAt}",
            fontSize = 11.sp,
            color = MediTextMuted
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text("Frozen Escrow", fontSize = 10.sp, color = MediTextMuted)
          Text(
            text = "R${"%,d".format(dispute.disputedAmountZar)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isResolved) MediTextMuted else MediDangerRed
          )
        }
      }

      HorizontalDivider(color = MediCardBorder)

      // Parties involved: Facility & Freelancer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Hospital
        Column(modifier = Modifier.weight(1f)) {
          Text("Complainant Facility", fontSize = 10.sp, color = MediTextMuted)
          Text(
            text = dispute.hospitalName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Icon(
          imageVector = Icons.Default.CompareArrows,
          contentDescription = null,
          tint = MediTextMuted,
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Doctor
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text("Respondent Pro", fontSize = 10.sp, color = MediTextMuted)
            Text(
              text = dispute.practitionerName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          InitialsAvatar(
            initials = dispute.practitionerInitials,
            size = 28.dp,
            backgroundColor = MediTealLight,
            textColor = MediTealPrimary
          )
        }
      }

      // Statements Preview Box
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, MediCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Facility: \"${dispute.hospitalStatement}\"",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
          )
          Text(
            text = "Doctor: \"${dispute.practitionerStatement}\"",
            fontSize = 11.sp,
            color = MediTealDark,
            maxLines = 2
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("In: ${dispute.checkInTimestamp}", fontSize = 10.sp, color = MediTextMuted)
            Text("Out: ${dispute.checkOutTimestamp}", fontSize = 10.sp, color = MediTextMuted)
          }
        }
      }

      if (dispute.resolutionSummary != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MediSuccessGreen.copy(alpha = 0.1f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Tribunal Determination", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MediSuccessGreen)
            Text(dispute.resolutionSummary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }

      // Administrative Mediation Actions (If not yet resolved)
      if (!isResolved) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Admin Mediation Determination", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MediTextMuted)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = { onResolveAction(ResolutionAction.SPLIT_5050) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
              Text("50/50 Split", fontSize = 11.sp, color = Color.White)
            }

            Button(
              onClick = { onResolveAction(ResolutionAction.RELEASE_DOCTOR) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
            ) {
              Text("Pay Doctor", fontSize = 11.sp, color = Color.White)
            }

            Button(
              onClick = { onResolveAction(ResolutionAction.REFUND_HOSPITAL) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
            ) {
              Text("Refund Hosp", fontSize = 11.sp, color = Color.White)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ResolveDisputeDialog(
  dispute: DisputeCase,
  action: ResolutionAction,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var note by remember {
    mutableStateOf(
      when (action) {
        ResolutionAction.RELEASE_DOCTOR -> "Traffic delay was substantiated; shift overtime compensated for Casualty coverage. Full escrow released to practitioner."
        ResolutionAction.REFUND_HOSPITAL -> "Delay resulted in cancellation of emergency list; hospital refunded."
        ResolutionAction.SPLIT_5050 -> "Equitable mutual settlement: 50% paid for standby/overtime, 50% refunded to hospital."
      }
    )
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text("Confirm Tribunal Resolution", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
          text = "Case #${dispute.caseNumber} • Escrow: R${"%,d".format(dispute.disputedAmountZar)}",
          fontSize = 12.sp,
          color = MediTextMuted
        )

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFEF3C7),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = when (action) {
              ResolutionAction.RELEASE_DOCTOR -> "Action: Release 100% of R${"%,d".format(dispute.disputedAmountZar)} to ${dispute.practitionerName}."
              ResolutionAction.REFUND_HOSPITAL -> "Action: Refund 100% of R${"%,d".format(dispute.disputedAmountZar)} back to ${dispute.hospitalName}."
              ResolutionAction.SPLIT_5050 -> "Action: 50/50 Split (R${"%,d".format(dispute.disputedAmountZar / 2)} to Doctor / R${"%,d".format(dispute.disputedAmountZar / 2)} to Hospital)."
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF92400E),
            modifier = Modifier.padding(10.dp)
          )
        }

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Binding Resolution Justification") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          maxLines = 3
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
            Text("Cancel")
          }
          Button(
            onClick = { onConfirm(note) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
          ) {
            Text("Execute", color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun DisputeDossierDialog(
  dispute: DisputeCase,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Case Docket #${dispute.caseNumber}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MediTextMuted)
          }
        }
        HorizontalDivider(color = MediCardBorder)

        Text("Booking Reference: ${dispute.bookingId}", fontSize = 12.sp, color = MediTextMuted)
        Text("Category: ${dispute.reasonCategory}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("Frozen Escrow: R${"%,d".format(dispute.disputedAmountZar)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MediDangerRed)

        Text("Hospital: ${dispute.hospitalName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Statement: ${dispute.hospitalStatement}", fontSize = 12.sp, color = MediTextMuted)

        Text("Practitioner: ${dispute.practitionerName} (${dispute.practitionerTitle})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Statement: ${dispute.practitionerStatement}", fontSize = 12.sp, color = MediTextMuted)

        Text("Verified Check-In: ${dispute.checkInTimestamp}", fontSize = 11.sp, color = MediTealPrimary)
        Text("Verified Check-Out: ${dispute.checkOutTimestamp}", fontSize = 11.sp, color = MediTealPrimary)

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
        ) {
          Text("Close Docket", color = Color.White)
        }
      }
    }
  }
}
