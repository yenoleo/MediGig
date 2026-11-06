package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.MediGigRepository
import com.example.model.ActiveRole
import com.example.model.UserRole
import com.example.model.VerificationStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  onNavigateToRateSettings: () -> Unit,
  onNavigateToWallet: () -> Unit,
  onNavigateToVault: () -> Unit = {},
  onNavigateToRequisitions: () -> Unit = {},
  onNavigateToDisputes: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val activeRole by MediGigRepository.activeRole.collectAsState()
  val myProfile by MediGigRepository.myFreelancerProfile.collectAsState()
  val practice = MediGigRepository.myPracticeProfile
  val auditLogs by MediGigRepository.auditLogs.collectAsState()
  val allFreelancers by MediGigRepository.freelancers.collectAsState()

  var userRoleSelection by remember { mutableStateOf(UserRole.BOTH) }
  var showUploadSuccess by remember { mutableStateOf(false) }
  var showAuditModal by remember { mutableStateOf(false) }
  var showPopiaModal by remember { mutableStateOf(false) }

  val pendingCandidates = remember(allFreelancers) {
    allFreelancers.filter { it.verificationStatus == VerificationStatus.PENDING_REVIEW }
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
          text = "Profile & Medical Credentials",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "South African Medical Council compliance (HPCSA & SANC)",
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
      // Current User Initials Header (Rule #1: Initials Avatar Only)
      AffectCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          InitialsAvatar(
            initials = if (activeRole == ActiveRole.HIRER) "NC" else myProfile.initials,
            size = 60.dp,
            backgroundColor = MediTealPrimary
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (activeRole == ActiveRole.HIRER) practice.name else myProfile.fullName,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (activeRole == ActiveRole.HIRER) practice.practiceType else myProfile.title,
              fontSize = 13.sp,
              color = MediTealPrimary,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            VerifiedBadge(text = if (activeRole == ActiveRole.HIRER) "PR-Registered" else "HPCSA Active 2026")
          }
        }
      }

      // Role Switcher Card (Toggle between Freelancer and Hirer)
      AffectCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Active App Perspective",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Currently acting as: ${if (activeRole == ActiveRole.HIRER) "Practice Hirer (Netcare)" else "Healthcare Pro (Doctor)"}",
              fontSize = 12.sp,
              color = MediTealPrimary
            )
          }

          Button(
            onClick = { MediGigRepository.toggleRole() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealLight, contentColor = MediTealPrimary)
          ) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Switch Perspective", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Clinical Operations & Specialized Portals Card
      AffectCard {
        Text(
          text = "Clinical Operations & Portals",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // 1. HPCSA & SANC Credential Vault
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(10.dp))
              .clickable { onNavigateToVault() },
            color = Color(0xFFF8FAFC)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MediTealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text("HPCSA & SANC Credential Vault", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Verified practitioner licenses, CPD points & Form 7", fontSize = 11.sp, color = MediTextMuted)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediTextMuted)
            }
          }

          // 2. Hospital Manager Requisition Dashboard
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(10.dp))
              .clickable { onNavigateToRequisitions() },
            color = Color(0xFFF8FAFC)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text("Hospital Requisition Dashboard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Shift roster management & emergency locum broadcasts", fontSize = 11.sp, color = MediTextMuted)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediTextMuted)
            }
          }

          // 3. Admin Dispute Portal
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(10.dp))
              .clickable { onNavigateToDisputes() },
            color = Color(0xFFF8FAFC)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = MediDangerRed, modifier = Modifier.size(18.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text("Admin Dispute Portal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Escrow freeze tribunal & clinical mediation determinations", fontSize = 11.sp, color = MediTextMuted)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediTextMuted)
            }
          }
        }
      }

      // POPIA & Healthcare Staffing Compliance Card (Section 6)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, MediTealPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
          .clickable { showPopiaModal = true },
        color = MediTealLight.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Filled.Gavel, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(22.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("POPIA & Clinical Privacy Notice", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
            Text("Staffing marketplace only. Patient records are never stored on MediGig.", fontSize = 11.sp, color = MediTextSecondary)
          }
          Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MediTealPrimary)
        }
      }

      // Admin Compliance Review Queue (Section 2 - 04 & Section 3)
      if (pendingCandidates.isNotEmpty()) {
        AffectCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MediEscrowAmber, modifier = Modifier.size(18.dp))
              Text("Verification Review Queue", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = MediEscrowLight) {
              Text("${pendingCandidates.size} Pending", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MediEscrowAmber, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          pendingCandidates.forEach { candidate ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, MediCardBorder, RoundedCornerShape(10.dp)),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(candidate.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${candidate.title} • ${candidate.hpcsaOrSancNumber}", fontSize = 11.sp, color = MediTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = { MediGigRepository.approveFreelancerVerification(candidate.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text("Approve HPCSA", fontSize = 11.sp)
                  }
                  OutlinedButton(
                    onClick = { MediGigRepository.rejectFreelancerVerification(candidate.id, "Invalid certificate") },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text("Reject", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }

      // Account Roles
      AffectCard {
        Text("Account Role Type", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val roleOptions = listOf(
          Triple(UserRole.FREELANCER, "I want to get hired", "Find freelance medical shifts & set daily rate"),
          Triple(UserRole.HIRER, "I want to hire", "Hire vetted medical professionals for your clinic"),
          Triple(UserRole.BOTH, "Both (Recommended)", "Work shifts and hire on MediGig ZA")
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          roleOptions.forEach { (role, title, subtitle) ->
            val isSelected = userRoleSelection == role
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, if (isSelected) MediTealPrimary else MediCardBorder, RoundedCornerShape(10.dp))
                .background(if (isSelected) MediTealLight.copy(alpha = 0.5f) else Color.Transparent)
                .clickable { userRoleSelection = role }
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              RadioButton(selected = isSelected, onClick = { userRoleSelection = role }, colors = RadioButtonDefaults.colors(selectedColor = MediTealPrimary))
              Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 10.sp, color = MediTextMuted)
              }
            }
          }
        }
      }

      // Professional Qualifications (Form)
      if (userRoleSelection == UserRole.FREELANCER || userRoleSelection == UserRole.BOTH) {
        AffectCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Professional Qualifications", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MediMint, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = myProfile.hpcsaOrSancNumber,
            onValueChange = {},
            label = { Text("HPCSA / SANC Registration Number") },
            readOnly = true,
            trailingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MediMint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = myProfile.degrees.firstOrNull() ?: "MBChB - University of Cape Town",
            onValueChange = {},
            label = { Text("Degree & University") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text("Certifications & PDF Proof", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MediCardBorder, RoundedCornerShape(10.dp))
              .clickable { showUploadSuccess = true },
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(20.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text("acls_board_cert_2026.pdf", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("1.4 MB • Verified under South African standards", fontSize = 10.sp, color = MediMint)
              }
              Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MediMint, modifier = Modifier.size(16.dp))
            }
          }

          if (showUploadSuccess) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Certificate verified by MediGig Compliance.", fontSize = 11.sp, color = MediMint)
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = onNavigateToRateSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Outlined.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Daily Rate & Schedule Calendar", fontSize = 12.sp)
          }
        }
      }

      // Hospital / Clinic Information
      if (userRoleSelection == UserRole.HIRER || userRoleSelection == UserRole.BOTH) {
        AffectCard {
          Text("Hospital & Practice Details", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = practice.name, onValueChange = {}, label = { Text("Hospital / Practice Name") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = practice.registrationNumber, onValueChange = {}, label = { Text("Practice Registration Number") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = practice.contactPerson, onValueChange = {}, label = { Text("Contact Person") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = practice.fullAddress,
            onValueChange = {},
            label = { Text("Street Address (Protected)") },
            readOnly = true,
            supportingText = { Text("Protected: Address is shared only upon shift acceptance.") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      // Audit Logs & Transparency (Section 3)
      SecondaryAffectButton(
        text = "View Immutable Audit Log (${auditLogs.size})",
        onClick = { showAuditModal = true },
        icon = Icons.Outlined.History
      )

      val context = androidx.compose.ui.platform.LocalContext.current
      val coroutineScope = rememberCoroutineScope()
      OutlinedButton(
        onClick = {
          com.example.data.AuthManager.signOut(context, coroutineScope)
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MediDangerRed)
      ) {
        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Sign out of MediGig", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }

  // POPIA Dialog
  if (showPopiaModal) {
    Dialog(onDismissRequest = { showPopiaModal = false }) {
      Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("POPIA Compliance & Privacy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
          Text(
            "MediGig ZA operates strictly in accordance with the Protection of Personal Information Act (POPIA):\n\n" +
              "• We only collect credential data required for professional council verification (HPCSA & SANC).\n" +
              "• Exact workplace and personal addresses are never displayed on public search.\n" +
              "• Patient health records are NOT processed or stored on this platform.",
            fontSize = 12.sp,
            color = MediTextSecondary,
            lineHeight = 16.sp
          )
          Button(onClick = { showPopiaModal = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)) {
            Text("Understood")
          }
        }
      }
    }
  }

  // Audit Logs Dialog
  if (showAuditModal) {
    Dialog(onDismissRequest = { showAuditModal = false }) {
      Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f).clip(RoundedCornerShape(16.dp)), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Immutable Audit Trail", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAuditModal = false }) {
              Icon(Icons.Filled.Close, contentDescription = "Close")
            }
          }
          Divider(color = MediCardBorder)
          Spacer(modifier = Modifier.height(8.dp))
          Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            auditLogs.forEach { log ->
              Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(log.action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
                    Text(log.timestamp, fontSize = 9.sp, color = MediTextMuted)
                  }
                  Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                  Text("By: ${log.performedBy}", fontSize = 9.sp, color = MediTextSecondary)
                }
              }
            }
          }
        }
      }
    }
  }
}
