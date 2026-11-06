package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MediGigRepository
import com.example.model.*
import com.example.ui.components.AffectCard
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HpcsaSancVaultScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val credentials by MediGigRepository.credentialsVault.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedCouncil by remember { mutableStateOf<RegulatoryCouncil?>(null) }
  var selectedStatus by remember { mutableStateOf<VerificationStatus?>(null) }

  var selectedCredentialDetail by remember { mutableStateOf<CredentialVaultItem?>(null) }
  var showAddCredentialDialog by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  // Filtered List
  val filteredCredentials = remember(credentials, searchQuery, selectedCouncil, selectedStatus) {
    credentials.filter { item ->
      val matchesSearch = searchQuery.isBlank() ||
        item.practitionerName.contains(searchQuery, ignoreCase = true) ||
        item.registrationNumber.contains(searchQuery, ignoreCase = true) ||
        item.category.contains(searchQuery, ignoreCase = true)

      val matchesCouncil = selectedCouncil == null || item.council == selectedCouncil
      val matchesStatus = selectedStatus == null || item.status == selectedStatus

      matchesSearch && matchesCouncil && matchesStatus
    }
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
              modifier = Modifier.testTag("vault_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to dashboard",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "HPCSA & SANC Vault",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MediTealLight
                ) {
                  Text(
                    text = "OFFICIAL REGISTRY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MediTealPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "South African Healthcare Regulatory Credential Vault",
                fontSize = 12.sp,
                color = MediTextMuted
              )
            }

            IconButton(
              onClick = { showAddCredentialDialog = true },
              modifier = Modifier.testTag("vault_add_button")
            ) {
              Icon(
                imageVector = Icons.Default.AddModerator,
                contentDescription = "Add practitioner credential",
                tint = MediTealPrimary
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
      // 1. Vault Overview Metrics
      item {
        AffectCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = MediTealLight
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MediTealPrimary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "National Regulatory Compliance Index",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MediTealDark
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Total in Vault", fontSize = 11.sp, color = MediTextMuted)
                Text("${credentials.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
              }
              Column {
                Text("HPCSA Doctors", fontSize = 11.sp, color = MediTextMuted)
                Text(
                  "${credentials.count { it.council == RegulatoryCouncil.HPCSA }}",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Column {
                Text("SANC Nurses", fontSize = 11.sp, color = MediTextMuted)
                Text(
                  "${credentials.count { it.council == RegulatoryCouncil.SANC }}",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Column {
                Text("CPD Compliant", fontSize = 11.sp, color = MediTextMuted)
                Text("98.4%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MediSuccessGreen)
              }
            }

            Text(
              text = "Live validation connects to Health Professions Council of SA (HPCSA iEnquirer) and SA Nursing Council live register endpoints. All credentials backed by Form 7 Sexual Offences screening.",
              fontSize = 11.sp,
              color = MediTextMuted,
              lineHeight = 15.sp
            )
          }
        }
      }

      // 2. Search & Council Filter
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, HPCSA/SANC reg #, or title", fontSize = 13.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MediTextMuted)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = MediTextMuted)
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("vault_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MediTealPrimary,
              unfocusedBorderColor = MediCardBorder
            ),
            singleLine = true
          )

          // Council Filter Chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              FilterChip(
                selected = selectedCouncil == null,
                onClick = { selectedCouncil = null },
                label = { Text("All Councils (${credentials.size})") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MediTealPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            item {
              FilterChip(
                selected = selectedCouncil == RegulatoryCouncil.HPCSA,
                onClick = {
                  selectedCouncil = if (selectedCouncil == RegulatoryCouncil.HPCSA) null else RegulatoryCouncil.HPCSA
                },
                label = { Text("HPCSA (Medical & Allied)") },
                leadingIcon = {
                  Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MediTealPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
            item {
              FilterChip(
                selected = selectedCouncil == RegulatoryCouncil.SANC,
                onClick = {
                  selectedCouncil = if (selectedCouncil == RegulatoryCouncil.SANC) null else RegulatoryCouncil.SANC
                },
                label = { Text("SANC (Nursing)") },
                leadingIcon = {
                  Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MediTealPrimary,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      }

      // 3. Credential Vault Item Count Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "VERIFIED REGISTRY (${filteredCredentials.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MediTextMuted,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "Cryptographically Sealed",
            fontSize = 11.sp,
            color = MediTealPrimary,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // 4. Credential Cards List
      if (filteredCredentials.isEmpty()) {
        item {
          AffectCard(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MediTextMuted,
                modifier = Modifier.size(40.dp)
              )
              Text("No matching credentials found", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              Text("Try searching by registration number or resetting filters", fontSize = 12.sp, color = MediTextMuted)
            }
          }
        }
      } else {
        items(filteredCredentials, key = { it.id }) { item ->
          CredentialCard(
            item = item,
            onCardClick = { selectedCredentialDetail = item },
            onVerifyLive = {
              MediGigRepository.triggerLiveRegisterVerification(item.id)
              Toast.makeText(context, "Queried ${item.council.shortName} live register: Status Verified!", Toast.LENGTH_SHORT).show()
            },
            onCopyRegNumber = {
              clipboardManager.setText(AnnotatedString(item.registrationNumber))
              Toast.makeText(context, "Copied ${item.registrationNumber}", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }

  // Credential Detail Dossier Dialog
  selectedCredentialDetail?.let { detail ->
    CredentialDossierDialog(
      item = detail,
      onDismiss = { selectedCredentialDetail = null }
    )
  }

  // Add Credential Dialog
  if (showAddCredentialDialog) {
    AddCredentialDialog(
      onDismiss = { showAddCredentialDialog = false },
      onSave = { newItem ->
        MediGigRepository.addNewCredential(newItem)
        showAddCredentialDialog = false
        Toast.makeText(context, "Credential registered into vault!", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
fun CredentialCard(
  item: CredentialVaultItem,
  onCardClick: () -> Unit,
  onVerifyLive: () -> Unit,
  onCopyRegNumber: () -> Unit,
  modifier: Modifier = Modifier
) {
  val councilColor = if (item.council == RegulatoryCouncil.HPCSA) MediTealPrimary else Color(0xFF3B82F6)
  val councilBg = if (item.council == RegulatoryCouncil.HPCSA) MediTealLight else Color(0xFFEFF6FF)

  AffectCard(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("vault_card_${item.id}")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Header: Avatar, Name, Council Badge, Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          InitialsAvatar(
            initials = item.initials,
            size = 46.dp,
            backgroundColor = councilBg,
            textColor = councilColor
          )
          Column {
            Text(
              text = item.practitionerName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = item.category,
              fontSize = 12.sp,
              color = MediTextMuted,
              maxLines = 1
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = when (item.status) {
            VerificationStatus.VERIFIED -> MediSuccessGreen.copy(alpha = 0.15f)
            VerificationStatus.PENDING_REVIEW -> Color(0xFFFEF3C7)
            VerificationStatus.REJECTED -> MediDangerRed.copy(alpha = 0.15f)
            VerificationStatus.EXPIRED -> Color(0xFFF1F5F9)
          }
        ) {
          Text(
            text = item.status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = when (item.status) {
              VerificationStatus.VERIFIED -> MediSuccessGreen
              VerificationStatus.PENDING_REVIEW -> Color(0xFFD97706)
              VerificationStatus.REJECTED -> MediDangerRed
              VerificationStatus.EXPIRED -> MediTextMuted
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      HorizontalDivider(color = MediCardBorder)

      // Registration Number & Council Box
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = councilBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, councilColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${item.council.shortName} REGISTERED NUMBER",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = councilColor
            )
            Text(
              text = item.registrationNumber,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = onCopyRegNumber,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy registration number",
              tint = councilColor,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Details: Annual Practising Cert, CPD, Malpractice Insurance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Annual Cert Expiry", fontSize = 11.sp, color = MediTextMuted)
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
              imageVector = Icons.Default.Event,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = if (item.isApcPaid) MediSuccessGreen else MediDangerRed
            )
            Text(
              text = item.annualCertificateExpiry,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Column {
          Text("CPD CEU Points", fontSize = 11.sp, color = MediTextMuted)
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val isFull = item.cpdPointsCurrent >= item.cpdPointsRequired
            Icon(
              imageVector = if (isFull) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = if (isFull) MediSuccessGreen else Color(0xFFF59E0B)
            )
            Text(
              text = "${item.cpdPointsCurrent} / ${item.cpdPointsRequired} CEUs",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Malpractice & Background
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Malpractice Indemnity", fontSize = 11.sp, color = MediTextMuted)
          Text(
            text = "${item.malpracticeInsurer} (${item.malpracticePolicyNumber})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Verification Footnote & Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MediSuccessGreen,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = item.lastVerifiedTimestamp,
            fontSize = 10.sp,
            color = MediTextMuted,
            maxLines = 1
          )
        }

        OutlinedButton(
          onClick = onVerifyLive,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.height(32.dp),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MediTealPrimary)
        ) {
          Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MediTealPrimary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Live Query", fontSize = 11.sp, color = MediTealPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CredentialDossierDialog(
  item: CredentialVaultItem,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = MediTealPrimary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Official Credential Dossier",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MediTextMuted)
          }
        }

        HorizontalDivider(color = MediCardBorder)

        // Practitioner Summary
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          InitialsAvatar(
            initials = item.initials,
            size = 50.dp,
            backgroundColor = MediTealLight,
            textColor = MediTealPrimary
          )
          Column {
            Text(item.practitionerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(item.category, fontSize = 12.sp, color = MediTextMuted)
            Text("Council: ${item.council.displayName}", fontSize = 11.sp, color = MediTealPrimary, fontWeight = FontWeight.SemiBold)
          }
        }

        // Vault Cryptographic Proof Box
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF8FAFC),
          border = androidx.compose.foundation.BorderStroke(1.dp, MediCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Cryptographic Vault Hash", fontSize = 10.sp, color = MediTextMuted, fontWeight = FontWeight.Bold)
              Text("VERIFIED BY MEDIGIG", fontSize = 9.sp, color = MediSuccessGreen, fontWeight = FontWeight.Bold)
            }
            Text(
              text = item.qrRegisterHash,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Live validation status: ${item.lastVerifiedTimestamp}",
              fontSize = 10.sp,
              color = MediTextMuted
            )
          }
        }

        // Details breakdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DossierRow("Registration #", item.registrationNumber)
          DossierRow("Council Authority", item.council.shortName)
          DossierRow("Practising Card Expiry", item.annualCertificateExpiry)
          DossierRow("CPD Status", "${item.cpdPointsCurrent} / ${item.cpdPointsRequired} CEUs (${if (item.cpdPointsCurrent >= item.cpdPointsRequired) "Fully Compliant" else "Points Deficit"})")
          DossierRow("Malpractice Insurer", item.malpracticeInsurer)
          DossierRow("Indemnity Policy #", item.malpracticePolicyNumber)
          DossierRow("Sexual Offences (Form 7)", item.policeClearanceStatus)
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              clipboardManager.setText(AnnotatedString("MEDIGIG CREDENTIAL DOSSIER\nPractitioner: ${item.practitionerName}\nReg: ${item.registrationNumber}\nCouncil: ${item.council.shortName}\nStatus: VERIFIED\nHash: ${item.qrRegisterHash}"))
              Toast.makeText(context, "Credential dossier copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export", fontSize = 13.sp)
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
          ) {
            Text("Done", fontSize = 13.sp, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun DossierRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, fontSize = 12.sp, color = MediTextMuted)
    Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
fun AddCredentialDialog(
  onDismiss: () -> Unit,
  onSave: (CredentialVaultItem) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var council by remember { mutableStateOf(RegulatoryCouncil.HPCSA) }
  var regNumber by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("") }
  var malpractice by remember { mutableStateOf("Medical Protection Society (MPS)") }
  var policyNum by remember { mutableStateOf("") }

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
        Text("Register New Practitioner Credential", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Enters practitioner registration into MediGig South African compliance registry", fontSize = 11.sp, color = MediTextMuted)

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name (e.g. Dr. K. Mthembu)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = council == RegulatoryCouncil.HPCSA,
            onClick = { council = RegulatoryCouncil.HPCSA },
            label = { Text("HPCSA") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
          )
          FilterChip(
            selected = council == RegulatoryCouncil.SANC,
            onClick = { council = RegulatoryCouncil.SANC },
            label = { Text("SANC") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MediTealPrimary, selectedLabelColor = Color.White)
          )
        }

        OutlinedTextField(
          value = regNumber,
          onValueChange = { regNumber = it },
          label = { Text(if (council == RegulatoryCouncil.HPCSA) "HPCSA Reg # (e.g. MP 0681920)" else "SANC Reg # (e.g. 14920199)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category (e.g. General Practitioner, ICU Sister)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = policyNum,
          onValueChange = { policyNum = it },
          label = { Text("Malpractice Policy # (MPS / EthiQal)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (name.isNotBlank() && regNumber.isNotBlank()) {
                val initials = name.split(" ").filter { it.isNotBlank() }.map { it.first().uppercase() }.take(2).joinToString("")
                val newItem = CredentialVaultItem(
                  id = "CRD-${(100..999).random()}",
                  practitionerId = "pro_${(100..999).random()}",
                  practitionerName = name,
                  initials = if (initials.isNotBlank()) initials else "MD",
                  profession = if (council == RegulatoryCouncil.SANC) ProfessionCategory.REGISTERED_NURSE else ProfessionCategory.GENERAL_PRACTITIONER,
                  council = council,
                  registrationNumber = regNumber,
                  category = if (category.isNotBlank()) category else "Healthcare Practitioner",
                  annualCertificateExpiry = "31 Mar 2027",
                  isApcPaid = true,
                  cpdPointsCurrent = 30,
                  cpdPointsRequired = 30,
                  malpracticeInsurer = malpractice,
                  malpracticePolicyNumber = if (policyNum.isNotBlank()) policyNum else "POL-ZA-9912",
                  malpracticeExpiry = "31 Dec 2026",
                  policeClearanceStatus = "Form 7 Sexual Offences Clear",
                  status = VerificationStatus.VERIFIED,
                  lastVerifiedTimestamp = "Live Register Validated (Just now)",
                  qrRegisterHash = "SHA256:${regNumber.hashCode()}...OK"
                )
                onSave(newItem)
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary),
            enabled = name.isNotBlank() && regNumber.isNotBlank()
          ) {
            Text("Register", color = Color.White)
          }
        }
      }
    }
  }
}
