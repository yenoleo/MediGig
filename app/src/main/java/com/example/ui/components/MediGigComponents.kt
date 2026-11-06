package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookingStatus
import com.example.ui.theme.*

@Composable
fun InitialsAvatar(
  initials: String,
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  backgroundColor: Color = MediTealPrimary,
  textColor: Color = Color.White,
  showOnlineDot: Boolean = false,
  isOnline: Boolean = true
) {
  Box(modifier = modifier.size(size)) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .background(backgroundColor),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = initials,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = (size.value * 0.38f).sp,
        letterSpacing = 0.5.sp
      )
    }

    if (showOnlineDot) {
      Box(
        modifier = Modifier
          .size(size * 0.28f)
          .align(Alignment.BottomEnd)
          .clip(CircleShape)
          .background(Color.White)
          .padding(2.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isOnline) MediMint else MediTextMuted)
        )
      }
    }
  }
}

@Composable
fun AffectCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = MediCardBorder,
  cornerRadius: Dp = 16.dp,
  content: @Composable ColumnScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)
  val clickableModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else {
    Modifier
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(1.dp, borderColor, shape)
      .then(clickableModifier),
    color = backgroundColor,
    shape = shape,
    tonalElevation = 1.dp,
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      content = content
    )
  }
}

@Composable
fun DistanceChip(
  distanceKm: Double,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(MediTealLight)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = Icons.Outlined.NearMe,
      contentDescription = null,
      modifier = Modifier.size(13.dp),
      tint = MediTealPrimary
    )
    Text(
      text = "${"%.1f".format(distanceKm)}km away",
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = MediTealPrimary
    )
  }
}

@Composable
fun StatusBadge(
  status: BookingStatus,
  modifier: Modifier = Modifier
) {
  val (bgColor, fgColor, icon) = when (status) {
    BookingStatus.PENDING_RESPONSE -> Triple(MediEscrowLight, MediEscrowAmber, Icons.Outlined.Schedule)
    BookingStatus.ACCEPTED_ESCROW -> Triple(MediTealLight, MediTealPrimary, Icons.Outlined.Lock)
    BookingStatus.IN_PROGRESS -> Triple(MediCyanLight, MediCyanAccent, Icons.Outlined.PlayArrow)
    BookingStatus.COMPLETED_BY_PRO -> Triple(MediMintLight, MediMint, Icons.Outlined.CheckCircle)
    BookingStatus.PENDING_RELEASE -> Triple(MediEscrowLight, MediEscrowAmber, Icons.Outlined.Timer)
    BookingStatus.DISPUTED -> Triple(MediDangerLight, MediDangerRed, Icons.Outlined.ReportProblem)
    BookingStatus.FUNDS_RELEASED -> Triple(MediMintLight, MediMint, Icons.Filled.CheckCircle)
    BookingStatus.DECLINED -> Triple(MediDangerLight, MediDangerRed, Icons.Outlined.Cancel)
    BookingStatus.CANCELLED -> Triple(Color(0xFFF1F5F9), MediTextSecondary, Icons.Outlined.Block)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(bgColor)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(14.dp),
      tint = fgColor
    )
    Text(
      text = status.label,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = fgColor
    )
  }
}

@Composable
fun VerifiedBadge(
  text: String = "HPCSA Verified",
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(MediTealLight)
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    Icon(
      imageVector = Icons.Filled.Verified,
      contentDescription = null,
      modifier = Modifier.size(13.dp),
      tint = MediTealPrimary
    )
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MediTealPrimary
    )
  }
}

@Composable
fun PrimaryAffectButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  containerColor: Color = MediTealPrimary,
  contentColor: Color = Color.White
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp),
    enabled = enabled,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor,
      disabledContainerColor = MediCardBorder,
      disabledContentColor = MediTextMuted
    ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
      }
      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun SecondaryAffectButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  borderColor: Color = MediCardBorder,
  textColor: Color = MediTextPrimary
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = textColor
    )
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
      }
      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
