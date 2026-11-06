package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.TransactionType
import com.example.model.WalletTransaction
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WalletScreen(
  onOpenAddFunds: () -> Unit,
  onOpenWithdrawal: () -> Unit,
  modifier: Modifier = Modifier
) {
  val wallet by MediGigRepository.walletState.collectAsState()
  val transactions by MediGigRepository.transactions.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Wallet & Escrow",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "South Africa • ZAR Escrow Ledger",
            fontSize = 12.sp,
            color = MediTealPrimary
          )
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MediTealLight
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(12.dp))
            Text("Protected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MediTealPrimary)
          }
        }
      }
    }

    // Hero Balance Card (Matching Screen 8 from PDF mockup)
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        color = MediTealLight
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MediTealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column {
              Text(
                text = "ZAR Balance",
                fontSize = 12.sp,
                color = MediTealOnContainer
              )
              Text(
                text = "R${"%,.2f".format(wallet.availableBalanceZar)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MediTealPrimary
              )
            }
          }

          // Two Sub-cards: Escrow Pending vs Available to Withdraw
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Escrow Pending
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MediEscrowAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
              color = MediEscrowLight
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(Icons.Outlined.HourglassEmpty, contentDescription = null, tint = MediEscrowAmber, modifier = Modifier.size(14.dp))
                  Text("Escrow Pending", fontSize = 11.sp, color = Color(0xFF92400E))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "R${"%,.0f".format(wallet.escrowPendingZar)}",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediEscrowAmber
                )
                Text("Release in 5-10 days", fontSize = 10.sp, color = MediTextSecondary)
              }
            }

            // Available to Withdraw
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MediMint.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
              color = MediMintLight
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MediMint, modifier = Modifier.size(14.dp))
                  Text("Available to Payout", fontSize = 11.sp, color = Color(0xFF065F46))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "R${"%,.0f".format(wallet.availableBalanceZar)}",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MediMint
                )
                Text("Instant transfer", fontSize = 10.sp, color = MediTextSecondary)
              }
            }
          }

          // Action Buttons: Paystack add funds & Withdraw to Bank
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onOpenAddFunds,
              modifier = Modifier
                .weight(1f)
                .height(46.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary)
            ) {
              Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Paystack Add Funds", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = onOpenWithdrawal,
              modifier = Modifier
                .weight(1f)
                .height(46.dp),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MediTealPrimary),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MediTealPrimary)
            ) {
              Icon(Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Withdraw to Bank", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    // Linked South African Bank Accounts
    item {
      AffectCard {
        Text(
          text = "Saved Payout Accounts",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MediTealLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MediTealPrimary, modifier = Modifier.size(18.dp))
            }
            Column {
              Text("ABSA Bank •••• 4582", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text("Standard 1-2 business days", fontSize = 11.sp, color = MediTextMuted)
            }
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MediTealLight
          ) {
            Text("Primary", fontSize = 10.sp, color = MediTealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      }
    }

    // Recent Activity Ledger
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Recent Activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Escrow releases typically take 5-10 days",
            fontSize = 11.sp,
            color = MediTextMuted
          )
        }
      }
    }

    items(transactions) { tx ->
      TransactionItemCard(tx = tx)
    }
  }
}

@Composable
fun TransactionItemCard(tx: WalletTransaction) {
  AffectCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              when (tx.type) {
                TransactionType.CREDIT_ADD_FUNDS -> MediMintLight
                TransactionType.ESCROW_HOLD -> MediEscrowLight
                TransactionType.ESCROW_RELEASE -> MediTealLight
                TransactionType.WITHDRAWAL -> MaterialTheme.colorScheme.surfaceVariant
                TransactionType.REFUND -> MediCyanLight
                TransactionType.DISPUTE_REFUND -> MediDangerLight
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (tx.type) {
              TransactionType.CREDIT_ADD_FUNDS -> Icons.Filled.Add
              TransactionType.ESCROW_HOLD -> Icons.Filled.Lock
              TransactionType.ESCROW_RELEASE -> Icons.Filled.Check
              TransactionType.WITHDRAWAL -> Icons.Filled.NorthEast
              TransactionType.REFUND -> Icons.Filled.Replay
              TransactionType.DISPUTE_REFUND -> Icons.Filled.ReportProblem
            },
            contentDescription = null,
            tint = when (tx.type) {
              TransactionType.CREDIT_ADD_FUNDS -> MediMint
              TransactionType.ESCROW_HOLD -> MediEscrowAmber
              TransactionType.ESCROW_RELEASE -> MediTealPrimary
              TransactionType.WITHDRAWAL -> MediTextSecondary
              TransactionType.REFUND -> MediCyanAccent
              TransactionType.DISPUTE_REFUND -> MediDangerRed
            },
            modifier = Modifier.size(18.dp)
          )
        }

        Column {
          Text(
            text = tx.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = tx.subtitle,
            fontSize = 11.sp,
            color = MediTextSecondary
          )
          Text(
            text = tx.dateFormatted,
            fontSize = 10.sp,
            color = MediTextMuted
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (tx.isPositive) "+" else "-"}R${"%,.0f".format(tx.amountZar)}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = if (tx.isPositive) MediMint else MaterialTheme.colorScheme.onSurface
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (tx.status.contains("Pending") || tx.status.contains("Escrow")) MediEscrowLight else MediMintLight
        ) {
          Text(
            text = tx.status,
            fontSize = 9.sp,
            color = if (tx.status.contains("Pending") || tx.status.contains("Escrow")) MediEscrowAmber else MediMint,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}
