package com.example.ui.screens

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
import com.example.model.FreelancerProfile
import com.example.model.ProfessionCategory
import com.example.model.SortOption
import com.example.model.VerificationStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  onSelectFreelancer: (FreelancerProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val allFreelancers by MediGigRepository.freelancers.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ProfessionCategory.ALL) }
  var selectedSort by remember { mutableStateOf(SortOption.DISTANCE) }
  var maxDistanceKm by remember { mutableStateOf(35f) }
  var showFilters by remember { mutableStateOf(false) }
  var selectedLocation by remember { mutableStateOf("All Gauteng & SA") }

  val filteredFreelancers = remember(allFreelancers, searchQuery, selectedCategory, maxDistanceKm, selectedSort, selectedLocation) {
    allFreelancers
      .filter { pro ->
        val matchesCategory = selectedCategory == ProfessionCategory.ALL || pro.profession == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
          pro.fullName.contains(searchQuery, ignoreCase = true) ||
          pro.specialty.contains(searchQuery, ignoreCase = true) ||
          pro.title.contains(searchQuery, ignoreCase = true)
        val matchesDistance = pro.distanceKm <= maxDistanceKm
        val matchesLocation = selectedLocation == "All Gauteng & SA" || pro.generalLocation.contains(selectedLocation.split(" ").first(), ignoreCase = true)
        matchesCategory && matchesSearch && matchesDistance && matchesLocation
      }
      .sortedWith { a, b ->
        when (selectedSort) {
          SortOption.DISTANCE -> a.distanceKm.compareTo(b.distanceKm)
          SortOption.AVAILABLE_NOW -> b.isAvailableToday.compareTo(a.isAvailableToday)
          SortOption.RATE_LOW_HIGH -> a.dailyRateZar.compareTo(b.dailyRateZar)
          SortOption.RATE_HIGH_LOW -> b.dailyRateZar.compareTo(a.dailyRateZar)
          SortOption.RATING -> b.rating.compareTo(a.rating)
        }
      }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header Bar with Location Selector
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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Discover Healthcare Locums",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.clickable {
                selectedLocation = when (selectedLocation) {
                  "All Gauteng & SA" -> "Sandton"
                  "Sandton" -> "Pretoria"
                  "Pretoria" -> "Cape Town"
                  else -> "All Gauteng & SA"
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MediTealPrimary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = selectedLocation,
                fontSize = 12.sp,
                color = MediTealPrimary,
                fontWeight = FontWeight.Medium
              )
              Text("▾", fontSize = 10.sp, color = MediTealPrimary)
            }
          }

          IconButton(
            onClick = { showFilters = !showFilters },
            modifier = Modifier
              .clip(CircleShape)
              .background(if (showFilters) MediTealLight else Color.Transparent)
          ) {
            Icon(
              imageVector = Icons.Outlined.Tune,
              contentDescription = "Filters",
              tint = if (showFilters) MediTealPrimary else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search text field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search doctor, nurse, dentist, ICU...", fontSize = 13.sp) },
          leadingIcon = {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MediTextMuted)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MediCardBorder,
            focusedBorderColor = MediTealPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface
          )
        )

        // Expandable Filter Section
        if (showFilters) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Maximum Travel Radius",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Within ${maxDistanceKm.toInt()}km",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediTealPrimary
                )
              }
              Slider(
                value = maxDistanceKm,
                onValueChange = { maxDistanceKm = it },
                valueRange = 5f..50f,
                steps = 8,
                colors = SliderDefaults.colors(
                  thumbColor = MediTealPrimary,
                  activeTrackColor = MediTealPrimary
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Profession Category Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(ProfessionCategory.values()) { category ->
            val isSelected = selectedCategory == category
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(20.dp)
                )
                .clickable { selectedCategory = category },
              color = if (isSelected) MediTealPrimary else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = category.displayName,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // Sort Options Bar (Section 2 - 01)
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${filteredFreelancers.size} locums available",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MediTextSecondary
        )

        // Sort Pill Menu
        var sortMenuExpanded by remember { mutableStateOf(false) }
        Box {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MediTealLight)
              .clickable { sortMenuExpanded = true }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Outlined.Sort, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(14.dp))
            Text(
              text = selectedSort.displayName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary
            )
            Text("▾", fontSize = 10.sp, color = MediTealPrimary)
          }

          DropdownMenu(
            expanded = sortMenuExpanded,
            onDismissRequest = { sortMenuExpanded = false }
          ) {
            SortOption.values().forEach { option ->
              DropdownMenuItem(
                text = {
                  Text(
                    option.displayName,
                    fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedSort == option) MediTealPrimary else MaterialTheme.colorScheme.onSurface
                  )
                },
                onClick = {
                  selectedSort = option
                  sortMenuExpanded = false
                }
              )
            }
          }
        }
      }
    }

    // List of Freelancer Cards
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
    ) {
      items(filteredFreelancers) { pro ->
        FreelancerItemCard(
          freelancer = pro,
          onClick = { onSelectFreelancer(pro) }
        )
      }
    }
  }
}

@Composable
fun FreelancerItemCard(
  freelancer: FreelancerProfile,
  onClick: () -> Unit
) {
  AffectCard(onClick = onClick) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Initials Avatar (Rule #1: NO profile photos for now, initials avatar only)
      InitialsAvatar(
        initials = freelancer.initials,
        size = 52.dp,
        backgroundColor = when (freelancer.profession) {
          ProfessionCategory.GENERAL_PRACTITIONER -> MediTealPrimary
          ProfessionCategory.REGISTERED_NURSE -> MediCyanAccent
          ProfessionCategory.SPECIALIST -> Color(0xFF0F766E)
          ProfessionCategory.PHYSIOTHERAPIST -> Color(0xFFD97706)
          ProfessionCategory.DENTIST -> Color(0xFF7C3AED)
          ProfessionCategory.ALL -> MediTealPrimary
        },
        showOnlineDot = true,
        isOnline = freelancer.isAvailableToday
      )

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = freelancer.fullName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          VerifiedBadge(
            text = when (freelancer.verificationStatus) {
              VerificationStatus.VERIFIED -> if (freelancer.profession == ProfessionCategory.REGISTERED_NURSE) "SANC ✓" else "HPCSA ✓"
              VerificationStatus.PENDING_REVIEW -> "Review Pending"
              VerificationStatus.REJECTED -> "Rejected"
              VerificationStatus.EXPIRED -> "Expired"
            }
          )
        }

        Text(
          text = freelancer.title,
          fontSize = 12.sp,
          color = MediTealPrimary,
          fontWeight = FontWeight.Medium
        )

        Text(
          text = freelancer.specialty,
          fontSize = 11.sp,
          color = MediTextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Distance & Rating
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          DistanceChip(distanceKm = freelancer.distanceKm)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = MediEscrowAmber, modifier = Modifier.size(14.dp))
            Text(
              text = "${freelancer.rating}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "(${freelancer.reviewCount})",
              fontSize = 11.sp,
              color = MediTextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = MediDivider)
        Spacer(modifier = Modifier.height(8.dp))

        // Rate & Book Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "R${"%,d".format(freelancer.dailyRateZar)}",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "per day (ZAR)",
              fontSize = 10.sp,
              color = MediTextMuted
            )
          }

          Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = "View & Hire",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
