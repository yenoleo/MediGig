package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.ui.components.AffectCard
import com.example.ui.theme.*

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()
  val isLoading by AuthManager.isLoading.collectAsState()
  val authError by AuthManager.authError.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // App Branding
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(MediTealPrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.LocalHospital,
          contentDescription = "MediGig Logo",
          tint = Color.White,
          modifier = Modifier.size(42.dp)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "MediGig",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "ZA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MediTealPrimary
          )
        }
        Text(
          text = "South Africa's Medical Freelance & Locum Marketplace",
          fontSize = 13.sp,
          color = MediTextSecondary,
          textAlign = TextAlign.Center
        )
      }

      // Value Props
      AffectCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Filled.Security, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
            Text("Protected ZAR Escrow Payments", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MediMint, modifier = Modifier.size(18.dp))
            Text("Verified HPCSA & SANC Healthcare Professionals", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MediCyanAccent, modifier = Modifier.size(18.dp))
            Text("Privacy First: Workplace address revealed only on hire", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      if (authError != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
          color = MediDangerLight
        ) {
          Text(
            text = authError ?: "",
            color = MediDangerRed,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      // Mandatory Interactive Google Sign-In Button
      Button(
        onClick = {
          if (activity != null) {
            AuthManager.signInWithGoogle(
              activity = activity,
              scope = coroutineScope,
              onSuccess = onAuthSuccess
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MediTealPrimary,
          contentColor = Color.White
        )
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = Color.White,
            strokeWidth = 2.dp
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Lock,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Sign in with Google",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Text(
        text = "By signing in, you agree to South African healthcare compliance regulations and POPIA data protection terms.",
        fontSize = 11.sp,
        color = MediTextMuted,
        textAlign = TextAlign.Center,
        lineHeight = 15.sp
      )
    }
  }
}
