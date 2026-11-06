package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun PaystackAddFundsModal(
  currentBalanceZar: Double,
  onAddFunds: (amountZar: Double, method: String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedAmount by remember { mutableStateOf(2500.0) }
  var customAmountText by remember { mutableStateOf("") }
  var isCustom by remember { mutableStateOf(false) }
  var selectedMethod by remember { mutableStateOf("Instant EFT (Ozow/Capitec)") }

  val presetAmounts = listOf(1000.0, 2500.0, 5000.0, 10000.0)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Add Funds to Wallet",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Powered by Paystack ZA • Instant & Secure",
              fontSize = 11.sp,
              color = MediTealPrimary
            )
          }
          Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MediTealPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Amount Selection Grid
        Text(
          text = "Select Amount (ZAR)",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          presetAmounts.take(2).forEach { amount ->
            val isSelected = !isCustom && selectedAmount == amount
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(10.dp)
                )
                .clickable {
                  selectedAmount = amount
                  isCustom = false
                },
              color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "R${"%,.0f".format(amount)}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isSelected) MediTealPrimary else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          presetAmounts.drop(2).forEach { amount ->
            val isSelected = !isCustom && selectedAmount == amount
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(10.dp)
                )
                .clickable {
                  selectedAmount = amount
                  isCustom = false
                },
              color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "R${"%,.0f".format(amount)}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isSelected) MediTealPrimary else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        // Custom Amount Field
        OutlinedTextField(
          value = customAmountText,
          onValueChange = {
            customAmountText = it.filter { char -> char.isDigit() }
            if (customAmountText.isNotEmpty()) {
              isCustom = true
              selectedAmount = customAmountText.toDoubleOrNull() ?: 0.0
            }
          },
          label = { Text("Custom Amount (ZAR)") },
          placeholder = { Text("e.g. 3500") },
          prefix = { Text("R ") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // Payment Method Options
        Text(
          text = "Payment Method",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        val methods = listOf(
          Triple("Instant EFT (Ozow / Capitec Pay)", Icons.Filled.AccountBalance, "Immediate reflection"),
          Triple("Credit / Debit Card (Visa, Mastercard)", Icons.Filled.CreditCard, "3D Secure enabled"),
          Triple("SnapScan / Masterpass", Icons.Filled.QrCode, "QR code scan")
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          methods.forEach { (name, icon, subtitle) ->
            val isSelected = selectedMethod == name
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                  1.dp,
                  if (isSelected) MediTealPrimary else MediCardBorder,
                  RoundedCornerShape(10.dp)
                )
                .background(if (isSelected) MediTealLight.copy(alpha = 0.5f) else Color.Transparent)
                .clickable { selectedMethod = name }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { selectedMethod = name },
                colors = RadioButtonDefaults.colors(selectedColor = MediTealPrimary)
              )
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MediTealPrimary else MediTextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Column {
                Text(
                  text = name,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = subtitle,
                  fontSize = 10.sp,
                  color = MediTextMuted
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }

          val finalAmount = if (isCustom) (customAmountText.toDoubleOrNull() ?: 0.0) else selectedAmount
          Button(
            onClick = {
              if (finalAmount > 0) {
                onAddFunds(finalAmount, selectedMethod)
              }
            },
            modifier = Modifier
              .weight(1.5f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary),
            enabled = finalAmount >= 50.0
          ) {
            Text("Pay R${"%,.0f".format(finalAmount)}")
          }
        }
      }
    }
  }
}

@Composable
fun WithdrawalModal(
  availableBalanceZar: Double,
  onWithdraw: (amountZar: Double, bank: String, account: String) -> Unit,
  onDismiss: () -> Unit
) {
  var amountText by remember { mutableStateOf("") }
  var selectedBank by remember { mutableStateOf("ABSA Bank") }
  var accountNumber by remember { mutableStateOf("") }
  var branchCode by remember { mutableStateOf("") }
  var accountType by remember { mutableStateOf("Cheque / Current") }

  val saBanks = listOf("ABSA Bank", "FNB (First National Bank)", "Standard Bank", "Nedbank", "Capitec Bank", "Investec")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Withdraw to South African Bank",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
          color = MediTealLight
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Available for Payout:", fontSize = 12.sp, color = MediTealOnContainer)
            Text(
              "R${"%,.2f".format(availableBalanceZar)}",
              fontWeight = FontWeight.Bold,
              color = MediTealPrimary
            )
          }
        }

        // Amount Field
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { char -> char.isDigit() } },
          label = { Text("Amount to Withdraw (ZAR)") },
          prefix = { Text("R ") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // Bank Selection
        Text(
          text = "Select Bank",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(selectedBank, fontSize = 13.sp)
              Text("▼", fontSize = 10.sp)
            }
          }
          DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            saBanks.forEach { bank ->
              DropdownMenuItem(
                text = { Text(bank) },
                onClick = {
                  selectedBank = bank
                  expanded = false
                }
              )
            }
          }
        }

        // Account Number & Branch Code
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it.filter { char -> char.isDigit() } },
            label = { Text("Account No.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.3f),
            shape = RoundedCornerShape(10.dp)
          )

          OutlinedTextField(
            value = branchCode,
            onValueChange = { branchCode = it.filter { char -> char.isDigit() } },
            label = { Text("Branch Code") },
            placeholder = { Text("6 digits") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
        }

        Text(
          text = "EFT payouts reflect in 1-2 business days. Escrow holds cannot be withdrawn until released.",
          fontSize = 11.sp,
          color = MediTextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        val withdrawAmount = amountText.toDoubleOrNull() ?: 0.0
        val isValid = withdrawAmount > 0 && withdrawAmount <= availableBalanceZar && accountNumber.length >= 6

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (isValid) {
                onWithdraw(withdrawAmount, selectedBank, accountNumber)
              }
            },
            modifier = Modifier
              .weight(1.4f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediTealPrimary),
            enabled = isValid
          ) {
            Text("Confirm Transfer")
          }
        }
      }
    }
  }
}
