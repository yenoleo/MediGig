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
fun HospitalRequisitionScreen(
  onNavigateBack: () -> Unit,
  onNavigateToFindDoctors: () -> Unit,
  modifier: Modifier = Modifier
) {
  val requisitions by MediGigRepository.hospitalRequisitions.collectAsState()
  val wallet by MediGigRepository.walletState.collectAsState()
  val practiceProfile = MediGigRepository.myPracticeProfile

  var selectedFilter by remember { mutableStateOf<RequisitionStatus?>(null) }
  var selectedDepartment by remember { mutableStateOf<HospitalDepartment?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var selectedRequisitionDetail by remember { mutableStateOf<HospitalRequisition?>(null) }

  val context = LocalContext.current

  val filteredRequisitions = remember(requisitions, selectedFilter, selectedDepartment) {
    requisitions.filter { req ->
      val matchesStatus = selectedFilter == null || req.status == selectedFilter
      val matchesDept = selectedDepartment == null || req.department == selectedDepartment
      matchesStatus && matchesDept
    }
  }

  val openCount = requisitions.count { it.status == RequisitionStatus.OPEN_BROADCASTING }
  val filledCount = requisitions.count { it.status == RequisitionStatus.FILLED || it.status == RequisitionStatus.IN_PROGRESS }

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
              modifier = Modifier.testTag("requisition_back_button")
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
                  text = "Hospital Requisitions",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MediTealLight
                ) {
                  Text(
                    text = "MANAGER PORTAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MediTealPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "${practiceProfile.name} • Clinical Staffing Roster",
                fontSize = 12.sp,
                color = MediTextMuted
              )
            }

            IconButton(
              onClick = { showCreateDialog = true },
              modifier = Modifier.testTag("requisition_create_button")
            ) {
              Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Broadcast Requisition",
                tint = MediTealPrimary
              )
            }
          }
        }
      }
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateDialog = true },
        containerColor = MediTealPrimary,
        contentColor = Color.White,
        icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
        text = { Text("Broadcast Shift Callout", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        modifier = Modifier.testTag("requisition_fab")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Staffing Metrics Overview Card
      item {
        AffectCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = MediTealLight
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
                  imageVector = Icons.Default.Business,
                  contentDescription = null,
                  tint = MediTealPrimary,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "Shift Coverage & Roster Health",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MediTealDark
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MediSuccessGreen.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "94% Filled",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediSuccessGreen,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Open Requisitions", fontSize = 11.sp, color = MediTextMuted)
                Text("$openCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (openCount > 0) MediDangerRed else MediSuccessGreen)
              }
              Column {
                Text("Confirmed Locums", fontSize = 11.sp, color = MediTextMuted)
                Text("$filledCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
              }
              Column {
                Text("Escrow Allocated", fontSize = 11.sp, color = MediTextMuted)
                val totalEscrow = requisitions.sumOf { it.rateOfferedZar }
                Text("R${"%,d".format(totalEscrow)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
            }

            // Department Coverage Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Departmental Fill Ratio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MediTealDark)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                DepartmentPill(name = "ICU", count = "1 Open", isUrgent = true, modifier = Modifier.weight(1f))
                DepartmentPill(name = "ER", count = "1 Open", isUrgent = true, modifier = Modifier.weight(1f))
                DepartmentPill(name = "OT", count = "Filled", isUrgent = false, modifier = Modifier.weight(1f))
                DepartmentPill(name = "Ward", count = "Filled", isUrgent = false, modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }

      // 2. Department & Status Filters
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Status Chips
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All Statuses (${requisitions.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
              )
            }
            item {
              FilterChip(
                selected = selectedFilter == RequisitionStatus.OPEN_BROADCASTING,
                onClick = {
                  selectedFilter = if (selectedFilter == RequisitionStatus.OPEN_BROADCASTING) null else RequisitionStatus.OPEN_BROADCASTING
                },
                label = { Text("Open & Broadcasting ($openCount)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
              )
            }
            item {
              FilterChip(
                selected = selectedFilter == RequisitionStatus.FILLED,
                onClick = {
                  selectedFilter = if (selectedFilter == RequisitionStatus.FILLED) null else RequisitionStatus.FILLED
                },
                label = { Text("Filled ($filledCount)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
              )
            }
          }

          // Department Chips
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChip(
                selected = selectedDepartment == null,
                onClick = { selectedDepartment = null },
                label = { Text("All Wards") }
              )
            }
            HospitalDepartment.values().forEach { dept ->
              item {
                FilterChip(
                  selected = selectedDepartment == dept,
                  onClick = { selectedDepartment = if (selectedDepartment == dept) null else dept },
                  label = { Text(dept.code) }
                )
              }
            }
          }
        }
      }

      // 3. Requisition List Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ACTIVE REQUISITIONS (${filteredRequisitions.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MediTextMuted,
            letterSpacing = 0.5.sp
          )
          TextButton(
            onClick = onNavigateToFindDoctors,
            contentPadding = PaddingValues(0.dp)
          ) {
            Text("Browse Available Locums →", fontSize = 11.sp, color = MediTealPrimary, fontWeight = FontWeight.Bold)
          }
        }
      }

      // 4. Requisitions Items
      if (filteredRequisitions.isEmpty()) {
        item {
          AffectCard(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.EventAvailable, contentDescription = null, tint = MediTextMuted, modifier = Modifier.size(40.dp))
              Text("No requisitions under selected filter", fontWeight = FontWeight.Bold)
              Text("Tap 'Broadcast Shift Callout' to request medical locums", fontSize = 12.sp, color = MediTextMuted)
            }
          }
        }
      } else {
        items(filteredRequisitions, key = { it.id }) { req ->
          RequisitionCard(
            requisition = req,
            onCardClick = { selectedRequisitionDetail = req },
            onCancel = {
              MediGigRepository.cancelRequisition(req.id)
              Toast.makeText(context, "Requisition ${req.id} cancelled", Toast.LENGTH_SHORT).show()
            },
            onBroadcastAgain = {
              Toast.makeText(context, "Broadcast dispatched to 22 verified locums within ${req.broadcastRadiusKm}km!", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }

  // Create Requisition Dialog
  if (showCreateDialog) {
    CreateRequisitionDialog(
      onDismiss = { showCreateDialog = false },
      onSave = { dept, role, profession, date, shift, rate, urgency, radius, notes ->
        MediGigRepository.createRequisition(
          department = dept,
          roleNeeded = role,
          profession = profession,
          date = date,
          shiftType = shift,
          rateOfferedZar = rate,
          urgency = urgency,
          broadcastRadiusKm = radius,
          notes = notes
        )
        showCreateDialog = false
        Toast.makeText(context, "Requisition broadcast to verified locums in $radius km!", Toast.LENGTH_LONG).show()
      }
    )
  }

  // Detail Dialog
  selectedRequisitionDetail?.let { req ->
    RequisitionDetailDialog(
      requisition = req,
      onDismiss = { selectedRequisitionDetail = null }
    )
  }
}

@Composable
fun RequisitionCard(
  requisition: HospitalRequisition,
  onCardClick: () -> Unit,
  onCancel: () -> Unit,
  onBroadcastAgain: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isOpen = requisition.status == RequisitionStatus.OPEN_BROADCASTING
  val isUrgent = requisition.urgency.contains("Critical") || requisition.urgency.contains("High")

  AffectCard(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("requisition_card_${requisition.id}")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Header: Department, Urgency, Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MediTealLight
          ) {
            Text(
              text = requisition.department.displayName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          if (isUrgent) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MediDangerRed.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MediDangerRed))
                Text(
                  text = requisition.urgency,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediDangerRed
                )
              }
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isOpen) Color(0xFFFEF3C7) else MediSuccessGreen.copy(alpha = 0.15f)
        ) {
          Text(
            text = requisition.status.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOpen) Color(0xFFD97706) else MediSuccessGreen,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      // Title & Date
      Column {
        Text(
          text = requisition.roleNeeded,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = MediTextMuted)
          Text(
            text = "${requisition.date} • ${requisition.shiftType}",
            fontSize = 12.sp,
            color = MediTextMuted
          )
        }
      }

      HorizontalDivider(color = MediCardBorder)

      // Rate & Locum Assignment Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Rate Offer (Escrow)", fontSize = 10.sp, color = MediTextMuted)
          Text(
            text = "R${"%,d".format(requisition.rateOfferedZar)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MediTealPrimary
          )
        }

        if (requisition.assignedWorkerName != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            InitialsAvatar(
              initials = requisition.assignedWorkerInitials ?: "DR",
              size = 32.dp,
              backgroundColor = MediTealLight,
              textColor = MediTealPrimary
            )
            Column {
              Text("Assigned Locum", fontSize = 10.sp, color = MediTextMuted)
              Text(
                text = requisition.assignedWorkerName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(13.dp), tint = MediTealPrimary)
            Text(
              text = "Broadcasting (${requisition.broadcastRadiusKm}km)",
              fontSize = 11.sp,
              color = MediTealPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      if (isOpen) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onBroadcastAgain,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Re-broadcast", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed)
          ) {
            Text("Cancel Shift", fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
fun CreateRequisitionDialog(
  onDismiss: () -> Unit,
  onSave: (
    department: HospitalDepartment,
    roleNeeded: String,
    profession: ProfessionCategory,
    date: String,
    shiftType: String,
    rateOfferedZar: Int,
    urgency: String,
    broadcastRadiusKm: Int,
    notes: String
  ) -> Unit
) {
  var selectedDept by remember { mutableStateOf(HospitalDepartment.EMERGENCY_CASUALTY) }
  var roleTitle by remember { mutableStateOf("Casualty Locum GP") }
  var profession by remember { mutableStateOf(ProfessionCategory.GENERAL_PRACTITIONER) }
  var shiftType by remember { mutableStateOf("12h Day Shift (07:00 - 19:00)") }
  var rateZar by remember { mutableStateOf("2500") }
  var urgency by remember { mutableStateOf("High") }
  var radiusKm by remember { mutableStateOf(25) }
  var notes by remember { mutableStateOf("") }

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
        Text("Broadcast Locum Requisition", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Notifies verified healthcare practitioners in South Africa within your radius", fontSize = 11.sp, color = MediTextMuted)

        // Department Selection
        Text("Department Ward", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          HospitalDepartment.values().forEach { dept ->
            item {
              FilterChip(
                selected = selectedDept == dept,
                onClick = { selectedDept = dept },
                label = { Text(dept.code) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
              )
            }
          }
        }

        OutlinedTextField(
          value = roleTitle,
          onValueChange = { roleTitle = it },
          label = { Text("Role Needed (e.g. ICU Sister, Casualty GP)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = shiftType,
          onValueChange = { shiftType = it },
          label = { Text("Shift Details & Hours") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = rateZar,
            onValueChange = { rateZar = it },
            label = { Text("Rate Offer (ZAR)") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )

          OutlinedTextField(
            value = "$radiusKm km",
            onValueChange = {},
            readOnly = true,
            label = { Text("Broadcast") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Clinical Requirements (e.g. ACLS/ATLS required)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
            Text("Cancel")
          }
          Button(
            onClick = {
              val rate = rateZar.toIntOrNull() ?: 2500
              onSave(selectedDept, roleTitle, profession, "Next Available Shift", shiftType, rate, urgency, radiusKm, notes)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
          ) {
            Text("Broadcast", color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun RequisitionDetailDialog(
  requisition: HospitalRequisition,
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
          Text("Shift Requisition #${requisition.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MediTextMuted)
          }
        }
        HorizontalDivider(color = MediCardBorder)

        Text(requisition.roleNeeded, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Facility: ${requisition.facilityName}", fontSize = 12.sp, color = MediTextMuted)
        Text("Ward: ${requisition.department.displayName}", fontSize = 12.sp, color = MediTextMuted)
        Text("Date & Shift: ${requisition.date} (${requisition.shiftType})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Rate Offer: R${"%,d".format(requisition.rateOfferedZar)} (Escrow Guaranteed)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)

        if (requisition.notes.isNotBlank()) {
          Text("Clinical Notes: ${requisition.notes}", fontSize = 12.sp, color = MediTextMuted)
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
        ) {
          Text("Done", color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun DepartmentPill(name: String, count: String, isUrgent: Boolean, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isUrgent) Color(0xFFFEF2F2) else Color(0xFFF1F5F9),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isUrgent) MediDangerRed else MaterialTheme.colorScheme.onSurface)
      Text(count, fontSize = 9.sp, color = if (isUrgent) MediDangerRed else MediTextMuted)
    }
  }
}
