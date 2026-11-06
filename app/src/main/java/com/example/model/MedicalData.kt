package com.example.model

enum class UserRole {
  FREELANCER,
  HIRER,
  BOTH
}

enum class ActiveRole {
  FREELANCER,
  HIRER
}

enum class VerificationStatus(val label: String) {
  VERIFIED("Verified"),
  PENDING_REVIEW("Pending Review"),
  REJECTED("Rejected"),
  EXPIRED("Expired")
}

enum class ProfessionCategory(val displayName: String) {
  ALL("All"),
  GENERAL_PRACTITIONER("General Practitioner"),
  REGISTERED_NURSE("Registered Nurse"),
  SPECIALIST("Specialist"),
  PHYSIOTHERAPIST("Physiotherapist"),
  DENTIST("Dentist")
}

enum class SortOption(val displayName: String) {
  DISTANCE("Nearest (KM)"),
  AVAILABLE_NOW("Available Today"),
  RATE_LOW_HIGH("Rate: Low to High"),
  RATE_HIGH_LOW("Rate: High to Low"),
  RATING("Highest Rated")
}

data class FreelancerProfile(
  val id: String,
  val initials: String,
  val fullName: String,
  val title: String,
  val profession: ProfessionCategory,
  val specialty: String,
  val dailyRateZar: Int,
  val distanceKm: Double,
  val generalLocation: String,
  val isAvailableToday: Boolean,
  val rating: Double,
  val reviewCount: Int,
  val yearsExperience: Int,
  val hpcsaOrSancNumber: String,
  val degrees: List<String>,
  val certifications: List<String>,
  val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
  val maxDistanceKm: Int = 30,
  val availableDays: Set<Int> = setOf(1, 2, 4, 5, 8, 9, 11, 12, 15, 16, 18, 19, 22, 23, 26, 29),
  val bookedDays: Set<Int> = emptySet(),
  val preferredTimeSlots: List<String> = listOf("08:00 - 16:00 (Standard)", "07:00 - 19:00 (Day 12h)", "19:00 - 07:00 (Night)")
)

data class PracticeProfile(
  val id: String,
  val name: String,
  val practiceType: String,
  val registrationNumber: String,
  val contactPerson: String,
  val distanceKm: Double,
  val generalLocation: String,
  val fullAddress: String
)

enum class BookingStatus(val label: String) {
  PENDING_RESPONSE("Pending Response"),
  ACCEPTED_ESCROW("Accepted (Escrow)"),
  IN_PROGRESS("In Progress"),
  COMPLETED_BY_PRO("Completed by Pro"),
  PENDING_RELEASE("Pending Release (5-10 Days)"),
  DISPUTED("Disputed (Escrow Frozen)"),
  FUNDS_RELEASED("Funds Released"),
  DECLINED("Declined"),
  CANCELLED("Cancelled & Refunded")
}

data class Booking(
  val id: String,
  val hirerId: String,
  val hirerName: String,
  val hirerGeneralLocation: String,
  val hirerFullAddress: String,
  val freelancerId: String,
  val freelancerName: String,
  val freelancerInitials: String,
  val freelancerTitle: String,
  val startDate: String,
  val endDate: String,
  val totalDays: Int,
  val bookedDayNumbers: Set<Int> = setOf(15, 16),
  val timeSlot: String,
  val ratePerDayZar: Int,
  val totalAmountZar: Int,
  val distanceKm: Double,
  val status: BookingStatus,
  val createdAt: String,
  val escrowDaysRemaining: Int = 7,
  val disputeReason: String? = null,
  val ratingGiven: Float? = null,
  val notes: String = ""
)

enum class TransactionType {
  CREDIT_ADD_FUNDS,
  ESCROW_HOLD,
  ESCROW_RELEASE,
  WITHDRAWAL,
  REFUND,
  DISPUTE_REFUND
}

data class WalletTransaction(
  val id: String,
  val type: TransactionType,
  val title: String,
  val subtitle: String,
  val amountZar: Double,
  val isPositive: Boolean,
  val dateFormatted: String,
  val status: String,
  val reference: String
)

data class WalletState(
  val availableBalanceZar: Double,
  val escrowPendingZar: Double,
  val totalEarnedZar: Double
)

data class AuditLogEntry(
  val id: String,
  val timestamp: String,
  val action: String,
  val details: String,
  val performedBy: String
)
