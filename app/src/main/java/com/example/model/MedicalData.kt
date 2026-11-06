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

enum class RegulatoryCouncil(val displayName: String, val shortName: String) {
  HPCSA("Health Professions Council of South Africa", "HPCSA"),
  SANC("South African Nursing Council", "SANC")
}

data class CredentialVaultItem(
  val id: String,
  val practitionerId: String,
  val practitionerName: String,
  val initials: String,
  val profession: ProfessionCategory,
  val council: RegulatoryCouncil,
  val registrationNumber: String,
  val category: String,
  val annualCertificateExpiry: String,
  val isApcPaid: Boolean,
  val cpdPointsCurrent: Int,
  val cpdPointsRequired: Int = 30,
  val malpracticeInsurer: String,
  val malpracticePolicyNumber: String,
  val malpracticeExpiry: String,
  val policeClearanceStatus: String,
  val status: VerificationStatus,
  val lastVerifiedTimestamp: String,
  val qrRegisterHash: String
)

enum class RequisitionStatus(val label: String) {
  OPEN_BROADCASTING("Open & Broadcasting"),
  FILLED("Filled & Confirmed"),
  IN_PROGRESS("In Progress"),
  COMPLETED("Completed"),
  CANCELLED("Cancelled")
}

enum class HospitalDepartment(val displayName: String, val code: String) {
  EMERGENCY_CASUALTY("Emergency & Trauma (Casualty)", "ER"),
  ICU_CRITICAL_CARE("ICU & Critical Care", "ICU"),
  OBSTETRICS_NEONATAL("Obstetrics & Neonatal", "OB/NICU"),
  THEATRE_ANAESTHETICS("Operating Theatre & Anaesthetics", "OT"),
  GENERAL_WARD("General Medical & Surgical Ward", "WARD"),
  DENTAL_CLINIC("Outpatient Dental Clinic", "DENT")
}

data class HospitalRequisition(
  val id: String,
  val facilityId: String,
  val facilityName: String,
  val department: HospitalDepartment,
  val roleNeeded: String,
  val profession: ProfessionCategory,
  val date: String,
  val shiftType: String,
  val rateOfferedZar: Int,
  val locumsNeeded: Int = 1,
  val locumsAssigned: Int = 0,
  val status: RequisitionStatus,
  val urgency: String = "High",
  val broadcastRadiusKm: Int = 25,
  val assignedWorkerName: String? = null,
  val assignedWorkerInitials: String? = null,
  val notes: String = ""
)

enum class DisputeStatus(val label: String) {
  ACTIVE_INVESTIGATION("Active Dispute (Escrow Frozen)"),
  UNDER_MEDIATION("Under Mediation"),
  RESOLVED_RELEASED("Resolved (Released to Doctor)"),
  RESOLVED_REFUNDED("Resolved (Refunded to Hospital)"),
  RESOLVED_SPLIT("Resolved (50/50 Settlement Split)")
}

data class DisputeCase(
  val id: String,
  val caseNumber: String,
  val bookingId: String,
  val hospitalName: String,
  val practitionerName: String,
  val practitionerInitials: String,
  val practitionerTitle: String,
  val disputedAmountZar: Int,
  val reasonCategory: String,
  val hospitalStatement: String,
  val practitionerStatement: String,
  val checkInTimestamp: String,
  val checkOutTimestamp: String,
  val status: DisputeStatus,
  val createdAt: String,
  val resolutionSummary: String? = null,
  val resolvedAt: String? = null
)
