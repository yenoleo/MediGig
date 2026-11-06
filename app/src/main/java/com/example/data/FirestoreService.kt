package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context) {

  private val TAG = "FirestoreService"

  // MANDATORY INVARIANT: Always initialize with R.string.firestore_database_id
  val db: FirebaseFirestore by lazy {
    val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
    FirebaseFirestore.getInstance(databaseId)
  }

  private fun requireUserId(): String {
    return Firebase.auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  suspend fun saveUserProfile(
    fullName: String,
    email: String,
    role: UserRole,
    activeRole: ActiveRole
  ) {
    val uid = requireUserId()
    val data = mapOf(
      "id" to uid,
      "fullName" to fullName,
      "email" to email,
      "role" to role.name,
      "activeRole" to activeRole.name,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      db.collection("users").document(uid).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving user profile to Firestore", e)
    }
  }

  suspend fun saveFreelancerProfile(profile: FreelancerProfile) {
    val uid = requireUserId()
    val data = mapOf(
      "userId" to uid,
      "initials" to profile.initials,
      "fullName" to profile.fullName,
      "title" to profile.title,
      "profession" to profile.profession.name,
      "specialty" to profile.specialty,
      "dailyRateZar" to profile.dailyRateZar,
      "distanceKm" to profile.distanceKm,
      "generalLocation" to profile.generalLocation,
      "isAvailableToday" to profile.isAvailableToday,
      "rating" to profile.rating,
      "reviewCount" to profile.reviewCount,
      "yearsExperience" to profile.yearsExperience,
      "hpcsaOrSancNumber" to profile.hpcsaOrSancNumber,
      "verificationStatus" to profile.verificationStatus.name,
      "maxDistanceKm" to profile.maxDistanceKm,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      db.collection("freelancer_profiles").document(uid).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving freelancer profile", e)
    }
  }

  suspend fun savePracticeProfile(practice: PracticeProfile) {
    val uid = requireUserId()
    val data = mapOf(
      "userId" to uid,
      "name" to practice.name,
      "practiceType" to practice.practiceType,
      "registrationNumber" to practice.registrationNumber,
      "contactPerson" to practice.contactPerson,
      "generalLocation" to practice.generalLocation,
      "fullAddress" to practice.fullAddress,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      db.collection("practice_profiles").document(uid).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving practice profile", e)
    }
  }

  suspend fun saveBooking(booking: Booking) {
    val uid = requireUserId()
    val data = mapOf(
      "id" to booking.id,
      "hirerId" to booking.hirerId,
      "freelancerId" to booking.freelancerId,
      "hirerName" to booking.hirerName,
      "hirerGeneralLocation" to booking.hirerGeneralLocation,
      "hirerFullAddress" to booking.hirerFullAddress,
      "freelancerName" to booking.freelancerName,
      "freelancerInitials" to booking.freelancerInitials,
      "freelancerTitle" to booking.freelancerTitle,
      "startDate" to booking.startDate,
      "endDate" to booking.endDate,
      "totalDays" to booking.totalDays,
      "timeSlot" to booking.timeSlot,
      "ratePerDayZar" to booking.ratePerDayZar,
      "totalAmountZar" to booking.totalAmountZar,
      "distanceKm" to booking.distanceKm,
      "status" to booking.status.name,
      "escrowDaysRemaining" to booking.escrowDaysRemaining,
      "disputeReason" to (booking.disputeReason ?: ""),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      db.collection("bookings").document(booking.id).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving booking", e)
    }
  }

  suspend fun updateWallet(walletState: WalletState) {
    val uid = requireUserId()
    val data = mapOf(
      "userId" to uid,
      "availableBalanceZar" to walletState.availableBalanceZar,
      "escrowPendingZar" to walletState.escrowPendingZar,
      "totalEarnedZar" to walletState.totalEarnedZar,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      db.collection("wallets").document(uid).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving wallet", e)
    }
  }
}
