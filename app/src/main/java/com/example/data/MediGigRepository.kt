package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object MediGigRepository {

  private val _activeRole = MutableStateFlow(ActiveRole.HIRER)
  val activeRole: StateFlow<ActiveRole> = _activeRole.asStateFlow()

  private val _freelancerProfile = MutableStateFlow(
    FreelancerProfile(
      id = "pro_me",
      initials = "LM",
      fullName = "Dr. L. Mokoena",
      title = "Cardiology Specialist",
      profession = ProfessionCategory.SPECIALIST,
      specialty = "General Cardiology & Echo Consultation",
      dailyRateZar = 4200, // South African Specialist market rate
      distanceKm = 0.0,
      generalLocation = "Sandton, Johannesburg, SA",
      isAvailableToday = true,
      rating = 5.0,
      reviewCount = 18,
      yearsExperience = 7,
      hpcsaOrSancNumber = "HPCSA: MP0534211",
      degrees = listOf("MBChB - University of Cape Town (2018)", "MMed Cardiology - Wits University"),
      certifications = listOf("BLS Healthcare Provider", "ACLS Advanced Cardiovascular", "HPCSA Annual Practising Cert 2026"),
      verificationStatus = VerificationStatus.VERIFIED,
      maxDistanceKm = 25,
      availableDays = setOf(1, 2, 4, 5, 8, 9, 11, 12, 17, 18, 22, 23, 24, 25, 29, 30),
      bookedDays = setOf(15, 16),
      preferredTimeSlots = listOf("08:00 - 16:00 (Standard Shift)", "07:00 - 19:00 (12h Day Call)", "19:00 - 07:00 (Night Duty)")
    )
  )
  val myFreelancerProfile: StateFlow<FreelancerProfile> = _freelancerProfile.asStateFlow()

  val myPracticeProfile = PracticeProfile(
    id = "prac_sandton",
    name = "Netcare Hospital Sandton",
    practiceType = "Private Tertiary Hospital",
    registrationNumber = "PR-88201-ZA",
    contactPerson = "Dr. Keith Evans (Clinical Director)",
    distanceKm = 2.1,
    generalLocation = "Sandton, Johannesburg",
    fullAddress = "Corner 155 West St & Rivonia Rd, Sandton Central, 2196"
  )

  // Seeded with authentic South African doctors, nurses, dentists with exact suggested rates
  private val _freelancers = MutableStateFlow<List<FreelancerProfile>>(
    listOf(
      FreelancerProfile(
        id = "doc_1",
        initials = "MJ",
        fullName = "Dr. M. Johnson",
        title = "General Practitioner",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        specialty = "Acute Urgent Care & Outpatient Consults",
        dailyRateZar = 2500, // Suggested Doctor Rate: R2,500/day
        distanceKm = 2.1,
        generalLocation = "Cape Town Waterfront / City Bowl",
        isAvailableToday = true,
        rating = 4.9,
        reviewCount = 42,
        yearsExperience = 8,
        hpcsaOrSancNumber = "HPCSA: MP0492100",
        degrees = listOf("MBChB - Stellenbosch University", "Dip PEC (SA) Emergency Medicine"),
        certifications = listOf("ACLS Certified 2026", "ATLS Trauma Care", "GCP Ethics Certified"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 25,
        availableDays = setOf(1, 3, 5, 7, 9, 11, 14, 15, 18, 20, 22, 25, 28)
      ),
      FreelancerProfile(
        id = "nurse_1",
        initials = "KN",
        fullName = "KN Nurse",
        title = "Registered Nurse (ICU)",
        profession = ProfessionCategory.REGISTERED_NURSE,
        specialty = "Critical Care ICU & High Care Unit",
        dailyRateZar = 1200, // Suggested Nurse Rate: R1,200/day
        distanceKm = 4.5,
        generalLocation = "Cape Town / Rondebosch",
        isAvailableToday = true,
        rating = 4.8,
        reviewCount = 29,
        yearsExperience = 5,
        hpcsaOrSancNumber = "SANC: 14209931",
        degrees = listOf("BCur Nursing Science - University of the Western Cape"),
        certifications = listOf("SANC Professional Nurse License", "Critical Care Nursing Cert", "BLS"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 30,
        availableDays = setOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 26)
      ),
      FreelancerProfile(
        id = "dentist_1",
        initials = "LP",
        fullName = "Dr. Lerato P.",
        title = "Dental Surgeon",
        profession = ProfessionCategory.DENTIST,
        specialty = "General Dentistry, Minor Oral Surgery & Relief",
        dailyRateZar = 3000, // Suggested Dentist Rate: R3,000/day
        distanceKm = 7.2,
        generalLocation = "Intercare Castle Gate, Pretoria East",
        isAvailableToday = true,
        rating = 4.9,
        reviewCount = 31,
        yearsExperience = 7,
        hpcsaOrSancNumber = "HPCSA: DP0038102",
        degrees = listOf("BChD - Sefako Makgatho Health Sciences University"),
        certifications = listOf("Conscious Sedation in Dentistry", "Digital Dental Radiography"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 25,
        availableDays = setOf(1, 2, 3, 8, 9, 10, 15, 16, 22, 23, 29, 30)
      ),
      FreelancerProfile(
        id = "nurse_2",
        initials = "TM",
        fullName = "Thandi M.",
        title = "Registered Nurse - Neonatal ICU",
        profession = ProfessionCategory.REGISTERED_NURSE,
        specialty = "Neonatal Intensive Care & Paediatric Ward",
        dailyRateZar = 1200, // Suggested Nurse Rate: R1,200/day
        distanceKm = 3.2,
        generalLocation = "Sandton / Johannesburg Central",
        isAvailableToday = true,
        rating = 4.9,
        reviewCount = 51,
        yearsExperience = 6,
        hpcsaOrSancNumber = "SANC: 15488102",
        degrees = listOf("BSc Nursing - Wits University"),
        certifications = listOf("SANC Certified", "Advanced Neonatal Resuscitation (NRP)", "Paediatric BLS"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 20,
        availableDays = setOf(3, 4, 5, 11, 12, 13, 17, 18, 19, 25, 26)
      ),
      FreelancerProfile(
        id = "physio_1",
        initials = "SK",
        fullName = "Sipho K.",
        title = "Senior Physiotherapist",
        profession = ProfessionCategory.PHYSIOTHERAPIST,
        specialty = "Cardiopulmonary & ICU Chest Rehabilitation",
        dailyRateZar = 1800,
        distanceKm = 6.0,
        generalLocation = "Pretoria East / Castle Gate",
        isAvailableToday = false,
        rating = 4.7,
        reviewCount = 19,
        yearsExperience = 7,
        hpcsaOrSancNumber = "HPCSA: PT0091823",
        degrees = listOf("BSc Physiotherapy - University of Pretoria"),
        certifications = listOf("SASP Registered", "Hospital Chest Physiotherapy Diploma"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 35,
        availableDays = setOf(1, 4, 8, 11, 15, 18, 22, 25, 29)
      ),
      FreelancerProfile(
        id = "spec_2",
        initials = "PV",
        fullName = "Dr. Pieter Van Zyl",
        title = "Anaesthesiologist Specialist",
        profession = ProfessionCategory.SPECIALIST,
        specialty = "Obstetric, Trauma & Paediatric Theatre Call",
        dailyRateZar = 4800,
        distanceKm = 12.4,
        generalLocation = "Mediclinic Pretoria & Sandton",
        isAvailableToday = true,
        rating = 5.0,
        reviewCount = 38,
        yearsExperience = 12,
        hpcsaOrSancNumber = "HPCSA: MP0381921",
        degrees = listOf("MBChB - University of Pretoria", "FCA (SA) Colleges of Medicine SA"),
        certifications = listOf("Difficult Airway Management", "Ultrasound Regional Blocks", "ATLS Instructor"),
        verificationStatus = VerificationStatus.VERIFIED,
        maxDistanceKm = 50,
        availableDays = setOf(2, 5, 8, 12, 16, 19, 23, 26, 30)
      ),
      FreelancerProfile(
        id = "doc_pending",
        initials = "ZN",
        fullName = "Dr. Zanele Ndlovu",
        title = "Emergency Medical Doctor",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        specialty = "Trauma & Emergency Unit Locum",
        dailyRateZar = 2500,
        distanceKm = 8.1,
        generalLocation = "Rosebank, Johannesburg",
        isAvailableToday = true,
        rating = 4.8,
        reviewCount = 6,
        yearsExperience = 4,
        hpcsaOrSancNumber = "HPCSA: MP0591044",
        degrees = listOf("MBChB - University of the Free State"),
        certifications = listOf("ATLS Provider", "ACLS Certified"),
        verificationStatus = VerificationStatus.PENDING_REVIEW,
        maxDistanceKm = 30,
        availableDays = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      )
    )
  )
  val freelancers: StateFlow<List<FreelancerProfile>> = _freelancers.asStateFlow()

  private val _bookings = MutableStateFlow<List<Booking>>(
    listOf(
      Booking(
        id = "BK-83921",
        hirerId = "prac_sandton",
        hirerName = "Netcare Hospital Sandton",
        hirerGeneralLocation = "Sandton, Johannesburg",
        hirerFullAddress = "Corner 155 West St & Rivonia Rd, Sandton Central, 2196",
        freelancerId = "doc_1",
        freelancerName = "Dr. M. Johnson",
        freelancerInitials = "MJ",
        freelancerTitle = "General Practitioner",
        startDate = "15 Nov",
        endDate = "16 Nov",
        totalDays = 2,
        bookedDayNumbers = setOf(15, 16),
        timeSlot = "08:00 - 16:00 • 8 Hours Shift",
        ratePerDayZar = 2500,
        totalAmountZar = 5000,
        distanceKm = 2.1,
        status = BookingStatus.PENDING_RESPONSE,
        createdAt = "Today, 09:15 AM",
        escrowDaysRemaining = 7
      ),
      Booking(
        id = "BK-71044",
        hirerId = "prac_mediclinic",
        hirerName = "Mediclinic Pretoria",
        hirerGeneralLocation = "Pretoria, Gauteng",
        hirerFullAddress = "Park Street & Beckett St, Arcadia, Pretoria, 0007",
        freelancerId = "pro_me",
        freelancerName = "Dr. L. Mokoena",
        freelancerInitials = "LM",
        freelancerTitle = "Cardiology Specialist",
        startDate = "04 Nov",
        endDate = "06 Nov",
        totalDays = 3,
        bookedDayNumbers = setOf(4, 5, 6),
        timeSlot = "09:00 - 18:00 • Day Shift",
        ratePerDayZar = 4200,
        totalAmountZar = 12600,
        distanceKm = 24.5,
        status = BookingStatus.PENDING_RELEASE,
        createdAt = "04 Nov 2026",
        escrowDaysRemaining = 4
      ),
      Booking(
        id = "BK-55219",
        hirerId = "prac_intercare",
        hirerName = "Intercare Castle Gate",
        hirerGeneralLocation = "Pretoria East",
        hirerFullAddress = "Castle Gate Shopping Centre, 478 Koedoesnek Ave, Waterkloof Ridge, 0181",
        freelancerId = "dentist_1",
        freelancerName = "Dr. Lerato P.",
        freelancerInitials = "LP",
        freelancerTitle = "Dental Surgeon",
        startDate = "28 Oct",
        endDate = "28 Oct",
        totalDays = 1,
        bookedDayNumbers = setOf(28),
        timeSlot = "08:30 - 17:00",
        ratePerDayZar = 3000,
        totalAmountZar = 3000,
        distanceKm = 7.2,
        status = BookingStatus.FUNDS_RELEASED,
        createdAt = "28 Oct 2026",
        escrowDaysRemaining = 0,
        ratingGiven = 5.0f
      )
    )
  )
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

  private val _walletState = MutableStateFlow(
    WalletState(
      availableBalanceZar = 14500.0,
      escrowPendingZar = 5000.0,
      totalEarnedZar = 64200.0
    )
  )
  val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

  private val _transactions = MutableStateFlow<List<WalletTransaction>>(
    listOf(
      WalletTransaction(
        id = "TX-9901",
        type = TransactionType.ESCROW_HOLD,
        title = "Escrow Lock: Netcare Gig",
        subtitle = "Booking #BK-83921 (2 Days @ R2,500)",
        amountZar = 5000.0,
        isPositive = false,
        dateFormatted = "Today, 09:15 AM",
        status = "Held in Escrow",
        reference = "ESC_SANDTON_83921"
      ),
      WalletTransaction(
        id = "TX-8820",
        type = TransactionType.ESCROW_RELEASE,
        title = "Escrow Payout: Intercare Castle Gate",
        subtitle = "Booking #BK-55219 (Dr. Lerato P.)",
        amountZar = 3000.0,
        isPositive = true,
        dateFormatted = "28 Oct 2026, 17:30",
        status = "Completed",
        reference = "ESC_REL_INTERCARE"
      ),
      WalletTransaction(
        id = "TX-7711",
        type = TransactionType.WITHDRAWAL,
        title = "Withdrawal to FNB Cheque",
        subtitle = "FNB Bank •••• 7190",
        amountZar = 10000.0,
        isPositive = false,
        dateFormatted = "22 Oct 2026, 11:20",
        status = "Completed",
        reference = "EFT_FNB_77119"
      ),
      WalletTransaction(
        id = "TX-6401",
        type = TransactionType.CREDIT_ADD_FUNDS,
        title = "Funds added via Paystack",
        subtitle = "Ozow Instant EFT (Standard Bank)",
        amountZar = 20000.0,
        isPositive = true,
        dateFormatted = "18 Oct 2026, 14:10",
        status = "Completed",
        reference = "PSTK_CAP_4492"
      )
    )
  )
  val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(
    listOf(
      AuditLogEntry(
        id = "AUD-101",
        timestamp = "Today, 09:15 AM",
        action = "ESCROW_LOCKED",
        details = "R5,000 locked for Booking #BK-83921 with Dr. M. Johnson",
        performedBy = "System (Netcare Sandton)"
      ),
      AuditLogEntry(
        id = "AUD-100",
        timestamp = "04 Nov 2026, 18:00",
        action = "JOB_COMPLETED",
        details = "Shift marked completed. Escrow timer started (4 days remaining)",
        performedBy = "Dr. L. Mokoena"
      ),
      AuditLogEntry(
        id = "AUD-099",
        timestamp = "28 Oct 2026, 17:30",
        action = "ESCROW_RELEASED",
        details = "R3,000 released to Dr. Lerato P. for Intercare locum",
        performedBy = "System (Auto-release)"
      )
    )
  )
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  private val _pendingNotification = MutableStateFlow<Booking?>(_bookings.value.firstOrNull { it.status == BookingStatus.PENDING_RESPONSE })
  val pendingNotification: StateFlow<Booking?> = _pendingNotification.asStateFlow()

  fun setRole(role: ActiveRole) {
    _activeRole.value = role
  }

  fun toggleRole() {
    _activeRole.value = if (_activeRole.value == ActiveRole.HIRER) ActiveRole.FREELANCER else ActiveRole.HIRER
  }

  // Double-booking conflict detection & atomic booking creation
  sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    object InsufficientFunds : BookingResult()
    data class DateConflict(val message: String) : BookingResult()
  }

  fun createBooking(
    freelancer: FreelancerProfile,
    startDate: String,
    endDate: String,
    totalDays: Int,
    timeSlot: String,
    requestedDays: Set<Int> = setOf(15, 16)
  ): BookingResult {
    // 1. Conflict Prevention: Check double-booking using latest repository state
    val currentPro = _freelancers.value.firstOrNull { it.id == freelancer.id } ?: freelancer
    val conflicts = currentPro.bookedDays.intersect(requestedDays)
    if (conflicts.isNotEmpty()) {
      return BookingResult.DateConflict(
        "${freelancer.fullName} is already booked on dates ${conflicts.joinToString()} Nov 2026. Please pick alternative dates."
      )
    }

    val totalAmount = freelancer.dailyRateZar * totalDays
    val currentWallet = _walletState.value

    if (currentWallet.availableBalanceZar < totalAmount) {
      return BookingResult.InsufficientFunds
    }

    val newBookingId = "BK-${(10000..99999).random()}"
    val newBooking = Booking(
      id = newBookingId,
      hirerId = myPracticeProfile.id,
      hirerName = myPracticeProfile.name,
      hirerGeneralLocation = myPracticeProfile.generalLocation,
      hirerFullAddress = myPracticeProfile.fullAddress,
      freelancerId = freelancer.id,
      freelancerName = freelancer.fullName,
      freelancerInitials = freelancer.initials,
      freelancerTitle = freelancer.title,
      startDate = startDate,
      endDate = endDate,
      totalDays = totalDays,
      bookedDayNumbers = requestedDays,
      timeSlot = timeSlot,
      ratePerDayZar = freelancer.dailyRateZar,
      totalAmountZar = totalAmount,
      distanceKm = freelancer.distanceKm,
      status = BookingStatus.PENDING_RESPONSE,
      createdAt = "Just now",
      escrowDaysRemaining = 7
    )

    // Deduct available balance and hold in escrow
    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar - totalAmount,
      escrowPendingZar = currentWallet.escrowPendingZar + totalAmount
    )

    // Update freelancer's booked days to prevent conflict
    _freelancers.value = _freelancers.value.map { pro ->
      if (pro.id == freelancer.id) {
        pro.copy(bookedDays = pro.bookedDays + requestedDays)
      } else pro
    }

    val newTx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.ESCROW_HOLD,
      title = "Escrow Lock: ${freelancer.fullName}",
      subtitle = "Booking #$newBookingId ($totalDays Days @ R${freelancer.dailyRateZar})",
      amountZar = totalAmount.toDouble(),
      isPositive = false,
      dateFormatted = "Just now",
      status = "Held in Escrow",
      reference = "ESC_$newBookingId"
    )

    _bookings.value = listOf(newBooking) + _bookings.value
    _transactions.value = listOf(newTx) + _transactions.value

    logAudit("ESCROW_LOCKED", "R$totalAmount locked into Escrow for #$newBookingId (${freelancer.fullName})")

    if (freelancer.id == _freelancerProfile.value.id || freelancer.fullName == _freelancerProfile.value.fullName) {
      _pendingNotification.value = newBooking
    }

    return BookingResult.Success(newBooking)
  }

  fun acceptBooking(bookingId: String) {
    _bookings.value = _bookings.value.map { booking ->
      if (booking.id == bookingId) {
        booking.copy(status = BookingStatus.ACCEPTED_ESCROW)
      } else booking
    }
    logAudit("BOOKING_ACCEPTED", "Shift #$bookingId accepted by freelancer. Workplace address unlocked.")
    if (_pendingNotification.value?.id == bookingId) {
      _pendingNotification.value = null
    }
  }

  fun declineBooking(bookingId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.DECLINED) else it
    }

    // Release conflict lock
    _freelancers.value = _freelancers.value.map { pro ->
      if (pro.id == booking.freelancerId) {
        pro.copy(bookedDays = pro.bookedDays - booking.bookedDayNumbers)
      } else pro
    }

    // Refund escrow amount back to available balance
    val currentWallet = _walletState.value
    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar + booking.totalAmountZar,
      escrowPendingZar = maxOf(0.0, currentWallet.escrowPendingZar - booking.totalAmountZar)
    )

    val refundTx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.REFUND,
      title = "Escrow Refund: Booking Declined",
      subtitle = "#$bookingId refunded to balance",
      amountZar = booking.totalAmountZar.toDouble(),
      isPositive = true,
      dateFormatted = "Just now",
      status = "Completed",
      reference = "REF_$bookingId"
    )
    _transactions.value = listOf(refundTx) + _transactions.value
    logAudit("BOOKING_DECLINED", "Booking #$bookingId declined. Full escrow refunded to hirer.")

    if (_pendingNotification.value?.id == bookingId) {
      _pendingNotification.value = null
    }
  }

  fun cancelBookingBeforeAcceptance(bookingId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.PENDING_RESPONSE) return

    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CANCELLED) else it
    }

    _freelancers.value = _freelancers.value.map { pro ->
      if (pro.id == booking.freelancerId) {
        pro.copy(bookedDays = pro.bookedDays - booking.bookedDayNumbers)
      } else pro
    }

    val currentWallet = _walletState.value
    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar + booking.totalAmountZar,
      escrowPendingZar = maxOf(0.0, currentWallet.escrowPendingZar - booking.totalAmountZar)
    )

    val refundTx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.REFUND,
      title = "Booking Cancelled (Instant Refund)",
      subtitle = "Refunded to wallet balance",
      amountZar = booking.totalAmountZar.toDouble(),
      isPositive = true,
      dateFormatted = "Just now",
      status = "Completed",
      reference = "REF_$bookingId"
    )
    _transactions.value = listOf(refundTx) + _transactions.value
    logAudit("BOOKING_CANCELLED", "Hirer cancelled #$bookingId before acceptance. Instant refund executed.")

    if (_pendingNotification.value?.id == bookingId) {
      _pendingNotification.value = null
    }
  }

  fun markJobCompleteByWorker(bookingId: String) {
    _bookings.value = _bookings.value.map { booking ->
      if (booking.id == bookingId) {
        booking.copy(
          status = BookingStatus.PENDING_RELEASE,
          escrowDaysRemaining = 7
        )
      } else booking
    }
    logAudit("JOB_COMPLETED", "Shift #$bookingId marked complete by healthcare pro. 7-day escrow countdown started.")
  }

  fun releaseEscrowToWorker(bookingId: String, rating: Float = 5.0f) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) {
        it.copy(
          status = BookingStatus.FUNDS_RELEASED,
          ratingGiven = rating,
          escrowDaysRemaining = 0
        )
      } else it
    }

    val currentWallet = _walletState.value
    _walletState.value = currentWallet.copy(
      escrowPendingZar = maxOf(0.0, currentWallet.escrowPendingZar - booking.totalAmountZar),
      totalEarnedZar = currentWallet.totalEarnedZar + booking.totalAmountZar
    )

    val releaseTx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.ESCROW_RELEASE,
      title = "Escrow Released to Freelancer",
      subtitle = "Gig payout for ${booking.freelancerName}",
      amountZar = booking.totalAmountZar.toDouble(),
      isPositive = true,
      dateFormatted = "Just now",
      status = "Completed",
      reference = "ESC_REL_$bookingId"
    )
    _transactions.value = listOf(releaseTx) + _transactions.value
    logAudit("ESCROW_RELEASED", "Escrow R${booking.totalAmountZar} released for #$bookingId with ${rating}★ review.")
  }

  // Dispute Workflow (Section 2 - 03 & Section 4)
  fun raiseDispute(bookingId: String, reason: String) {
    _bookings.value = _bookings.value.map { booking ->
      if (booking.id == bookingId) {
        booking.copy(
          status = BookingStatus.DISPUTED,
          disputeReason = reason
        )
      } else booking
    }
    logAudit("DISPUTE_RAISED", "Dispute raised for #$bookingId: '$reason'. Escrow frozen for compliance review.")
  }

  fun resolveDisputeRefundHirer(bookingId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CANCELLED) else it
    }

    val currentWallet = _walletState.value
    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar + booking.totalAmountZar,
      escrowPendingZar = maxOf(0.0, currentWallet.escrowPendingZar - booking.totalAmountZar)
    )

    val refundTx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.DISPUTE_REFUND,
      title = "Dispute Resolved: Full Refund",
      subtitle = "#$bookingId refunded after compliance audit",
      amountZar = booking.totalAmountZar.toDouble(),
      isPositive = true,
      dateFormatted = "Just now",
      status = "Completed",
      reference = "DISP_REF_$bookingId"
    )
    _transactions.value = listOf(refundTx) + _transactions.value
    logAudit("DISPUTE_REFUNDED", "Dispute #$bookingId resolved by admin: refunded to Hirer.")
  }

  fun topUpWalletPaystack(amountZar: Double, paymentMethod: String) {
    val currentWallet = _walletState.value
    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar + amountZar
    )

    val tx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.CREDIT_ADD_FUNDS,
      title = "Added funds via Paystack",
      subtitle = paymentMethod,
      amountZar = amountZar,
      isPositive = true,
      dateFormatted = "Just now",
      status = "Completed",
      reference = "PSTK_${(100000..999999).random()}"
    )
    _transactions.value = listOf(tx) + _transactions.value
    logAudit("PAYSTACK_TOPUP", "R$amountZar added to wallet via $paymentMethod.")
  }

  fun withdrawToBank(amountZar: Double, bankName: String, accountNumber: String): Boolean {
    val currentWallet = _walletState.value
    if (currentWallet.availableBalanceZar < amountZar) return false

    _walletState.value = currentWallet.copy(
      availableBalanceZar = currentWallet.availableBalanceZar - amountZar
    )

    val maskedAcc = if (accountNumber.length > 4) "•••• ${accountNumber.takeLast(4)}" else accountNumber
    val tx = WalletTransaction(
      id = "TX-${(1000..9999).random()}",
      type = TransactionType.WITHDRAWAL,
      title = "Withdrawal to $bankName",
      subtitle = "$bankName $maskedAcc",
      amountZar = amountZar,
      isPositive = false,
      dateFormatted = "Just now",
      status = "Processing (1-2 business days)",
      reference = "EFT_${(100000..999999).random()}"
    )
    _transactions.value = listOf(tx) + _transactions.value
    logAudit("BANK_WITHDRAWAL", "Withdrawal of R$amountZar initiated to $bankName ($maskedAcc).")
    return true
  }

  // Admin Credential Verification Actions (Section 2 - 04)
  fun approveFreelancerVerification(freelancerId: String) {
    _freelancers.value = _freelancers.value.map { pro ->
      if (pro.id == freelancerId) pro.copy(verificationStatus = VerificationStatus.VERIFIED) else pro
    }
    logAudit("ADMIN_VERIFY", "HPCSA / SANC credentials verified for candidate #$freelancerId.")
  }

  fun rejectFreelancerVerification(freelancerId: String, reason: String) {
    _freelancers.value = _freelancers.value.map { pro ->
      if (pro.id == freelancerId) pro.copy(verificationStatus = VerificationStatus.REJECTED) else pro
    }
    logAudit("ADMIN_REJECT", "Credentials rejected for #$freelancerId: $reason.")
  }

  fun updateFreelancerSettings(
    dailyRate: Int,
    maxDistance: Int,
    timeSlots: List<String>,
    availableDays: Set<Int>
  ) {
    _freelancerProfile.value = _freelancerProfile.value.copy(
      dailyRateZar = dailyRate,
      maxDistanceKm = maxDistance,
      preferredTimeSlots = timeSlots,
      availableDays = availableDays
    )
    logAudit("PROFILE_UPDATED", "Freelancer rate set to R$dailyRate/day, radius ${maxDistance}km.")
  }

  fun dismissNotification() {
    _pendingNotification.value = null
  }

  fun triggerSampleNotification() {
    _pendingNotification.value = _bookings.value.firstOrNull { it.status == BookingStatus.PENDING_RESPONSE }
  }

  // --- CREDENTIAL VAULT STATE & ACTIONS (HPCSA / SANC) ---
  private val _credentialsVault = MutableStateFlow<List<CredentialVaultItem>>(
    listOf(
      CredentialVaultItem(
        id = "CRD-01",
        practitionerId = "pro_me",
        practitionerName = "Dr. L. Mokoena",
        initials = "LM",
        profession = ProfessionCategory.SPECIALIST,
        council = RegulatoryCouncil.HPCSA,
        registrationNumber = "MP 0534211",
        category = "Medical Practitioner (Specialist Cardiologist)",
        annualCertificateExpiry = "31 Mar 2027",
        isApcPaid = true,
        cpdPointsCurrent = 30,
        cpdPointsRequired = 30,
        malpracticeInsurer = "MPS (Medical Protection Society)",
        malpracticePolicyNumber = "MPS-ZA-90182",
        malpracticeExpiry = "31 Dec 2026",
        policeClearanceStatus = "SAPS Form 7 Sexual Offences Clear (Verified 2026)",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "HPCSA Live Register Verified (Today 08:30)",
        qrRegisterHash = "SHA256:7a9f8c01d4e2...HPCSA-OK"
      ),
      CredentialVaultItem(
        id = "CRD-02",
        practitionerId = "doc_1",
        practitionerName = "Dr. M. Johnson",
        initials = "MJ",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        council = RegulatoryCouncil.HPCSA,
        registrationNumber = "MP 0492100",
        category = "Independent Practice General Practitioner",
        annualCertificateExpiry = "31 Mar 2027",
        isApcPaid = true,
        cpdPointsCurrent = 28,
        cpdPointsRequired = 30,
        malpracticeInsurer = "EthiQal Medical Indemnity",
        malpracticePolicyNumber = "ETH-ZA-77219",
        malpracticeExpiry = "28 Feb 2027",
        policeClearanceStatus = "Police Clearance Certificate Valid (Exp 2027)",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "HPCSA Live Register Verified (02 Nov 2026)",
        qrRegisterHash = "SHA256:4b1e8820f99a...HPCSA-OK"
      ),
      CredentialVaultItem(
        id = "CRD-03",
        practitionerId = "nurse_1",
        practitionerName = "Sister K. Ndebele",
        initials = "KN",
        profession = ProfessionCategory.REGISTERED_NURSE,
        council = RegulatoryCouncil.SANC,
        registrationNumber = "SANC 14209931",
        category = "Registered Professional Nurse & Critical Care Specialist",
        annualCertificateExpiry = "31 Dec 2026",
        isApcPaid = true,
        cpdPointsCurrent = 30,
        cpdPointsRequired = 30,
        malpracticeInsurer = "DENOSA Professional Indemnity",
        malpracticePolicyNumber = "DEN-41029-ZA",
        malpracticeExpiry = "31 Dec 2026",
        policeClearanceStatus = "SAPS Criminal Record Clear & Form 7 Valid",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "SANC Live Register Confirmed (Yesterday 16:15)",
        qrRegisterHash = "SHA256:91da3371bc02...SANC-OK"
      ),
      CredentialVaultItem(
        id = "CRD-04",
        practitionerId = "dentist_1",
        practitionerName = "Dr. Lerato P.",
        initials = "LP",
        profession = ProfessionCategory.DENTIST,
        council = RegulatoryCouncil.HPCSA,
        registrationNumber = "DP 0038102",
        category = "Dental Practitioner (General Dentistry & Minor Surgery)",
        annualCertificateExpiry = "31 Mar 2027",
        isApcPaid = true,
        cpdPointsCurrent = 25,
        cpdPointsRequired = 30,
        malpracticeInsurer = "MPS (Medical Protection Society)",
        malpracticePolicyNumber = "MPS-ZA-88410",
        malpracticeExpiry = "31 Dec 2026",
        policeClearanceStatus = "Verified SAPS Background Check",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "HPCSA Live Register Verified (28 Oct 2026)",
        qrRegisterHash = "SHA256:11bb7409ffae...HPCSA-OK"
      ),
      CredentialVaultItem(
        id = "CRD-05",
        practitionerId = "nurse_2",
        practitionerName = "Sister Thandi M.",
        initials = "TM",
        profession = ProfessionCategory.REGISTERED_NURSE,
        council = RegulatoryCouncil.SANC,
        registrationNumber = "SANC 15488102",
        category = "Professional Nurse & Neonatal ICU Specialist",
        annualCertificateExpiry = "31 Dec 2026",
        isApcPaid = true,
        cpdPointsCurrent = 29,
        cpdPointsRequired = 30,
        malpracticeInsurer = "HOSPERSA Indemnity Cover",
        malpracticePolicyNumber = "HOS-99120-ZA",
        malpracticeExpiry = "30 Jun 2027",
        policeClearanceStatus = "Clear - Sexual Offences Register Form 7",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "SANC Live Register Confirmed (01 Nov 2026)",
        qrRegisterHash = "SHA256:55ef8902cd34...SANC-OK"
      ),
      CredentialVaultItem(
        id = "CRD-06",
        practitionerId = "spec_2",
        practitionerName = "Dr. Pieter Van Zyl",
        initials = "PV",
        profession = ProfessionCategory.SPECIALIST,
        council = RegulatoryCouncil.HPCSA,
        registrationNumber = "MP 0381921",
        category = "Anaesthesiologist Specialist (FCA SA)",
        annualCertificateExpiry = "31 Mar 2027",
        isApcPaid = true,
        cpdPointsCurrent = 30,
        cpdPointsRequired = 30,
        malpracticeInsurer = "MPS (High Risk Surgical Cover R30M)",
        malpracticePolicyNumber = "MPS-SPEC-38192",
        malpracticeExpiry = "31 Dec 2026",
        policeClearanceStatus = "SAPS Form 7 Sexual Offences Clear",
        status = VerificationStatus.VERIFIED,
        lastVerifiedTimestamp = "HPCSA Live Register Verified (03 Nov 2026)",
        qrRegisterHash = "SHA256:33bc8811eef0...HPCSA-OK"
      ),
      CredentialVaultItem(
        id = "CRD-07",
        practitionerId = "doc_pending",
        practitionerName = "Dr. Zanele Ndlovu",
        initials = "ZN",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        council = RegulatoryCouncil.HPCSA,
        registrationNumber = "MP 0591044",
        category = "Medical Practitioner (Public Service to Independent)",
        annualCertificateExpiry = "31 Mar 2026",
        isApcPaid = true,
        cpdPointsCurrent = 22,
        cpdPointsRequired = 30,
        malpracticeInsurer = "EthiQal (Under Review)",
        malpracticePolicyNumber = "ETH-PENDING-44",
        malpracticeExpiry = "31 Dec 2026",
        policeClearanceStatus = "Awaiting Form 7 Criminal Stamp",
        status = VerificationStatus.PENDING_REVIEW,
        lastVerifiedTimestamp = "Audit in progress by MediGig Compliance",
        qrRegisterHash = "SHA256:pending...AUDIT"
      )
    )
  )
  val credentialsVault: StateFlow<List<CredentialVaultItem>> = _credentialsVault.asStateFlow()

  fun triggerLiveRegisterVerification(credentialId: String) {
    _credentialsVault.value = _credentialsVault.value.map { item ->
      if (item.id == credentialId) {
        item.copy(
          status = VerificationStatus.VERIFIED,
          lastVerifiedTimestamp = "Live Register Validated (Just now • ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())})"
        )
      } else item
    }
    logAudit("VAULT_LIVE_VERIFY", "Live lookup performed for credential $credentialId on regulatory register.")
  }

  fun addNewCredential(item: CredentialVaultItem) {
    _credentialsVault.value = listOf(item) + _credentialsVault.value
    logAudit("VAULT_CREDENTIAL_ADDED", "New credential added to vault: ${item.registrationNumber} (${item.practitionerName})")
  }

  // --- HOSPITAL REQUISITION DASHBOARD STATE & ACTIONS ---
  private val _hospitalRequisitions = MutableStateFlow<List<HospitalRequisition>>(
    listOf(
      HospitalRequisition(
        id = "REQ-101",
        facilityId = "prac_sandton",
        facilityName = "Netcare Hospital Sandton",
        department = HospitalDepartment.ICU_CRITICAL_CARE,
        roleNeeded = "ICU Sister (Day Shift)",
        profession = ProfessionCategory.REGISTERED_NURSE,
        date = "Tomorrow (07:00 - 19:00)",
        shiftType = "12h Day Shift",
        rateOfferedZar = 1400,
        locumsNeeded = 1,
        locumsAssigned = 0,
        status = RequisitionStatus.OPEN_BROADCASTING,
        urgency = "Critical",
        broadcastRadiusKm = 25,
        notes = "Emergency cover needed for Bed 8 & 9 ICU ventilators. Must have SANC Critical Care."
      ),
      HospitalRequisition(
        id = "REQ-102",
        facilityId = "prac_sandton",
        facilityName = "Netcare Hospital Sandton",
        department = HospitalDepartment.EMERGENCY_CASUALTY,
        roleNeeded = "Casualty Trauma Locum GP",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        date = "10 Nov 2026 (19:00 - 07:00)",
        shiftType = "12h Night Duty",
        rateOfferedZar = 2800,
        locumsNeeded = 1,
        locumsAssigned = 0,
        status = RequisitionStatus.OPEN_BROADCASTING,
        urgency = "High",
        broadcastRadiusKm = 30,
        notes = "Saturday night trauma cover. ATLS and ACLS certification required."
      ),
      HospitalRequisition(
        id = "REQ-103",
        facilityId = "prac_mediclinic",
        facilityName = "Mediclinic Pretoria",
        department = HospitalDepartment.THEATRE_ANAESTHETICS,
        roleNeeded = "Anaesthesiologist Theatre Call",
        profession = ProfessionCategory.SPECIALIST,
        date = "12 Nov 2026 (08:00 - 17:00)",
        shiftType = "Day Theatre Call",
        rateOfferedZar = 4800,
        locumsNeeded = 1,
        locumsAssigned = 1,
        status = RequisitionStatus.FILLED,
        urgency = "High",
        broadcastRadiusKm = 40,
        assignedWorkerName = "Dr. Pieter Van Zyl",
        assignedWorkerInitials = "PV",
        notes = "Elective orthopaedic and general surgery theatre list."
      ),
      HospitalRequisition(
        id = "REQ-104",
        facilityId = "prac_sandton",
        facilityName = "Netcare Hospital Sandton",
        department = HospitalDepartment.GENERAL_WARD,
        roleNeeded = "Ward Locum Doctor",
        profession = ProfessionCategory.GENERAL_PRACTITIONER,
        date = "15 Nov - 16 Nov 2026",
        shiftType = "08:00 - 16:00 (Standard)",
        rateOfferedZar = 2500,
        locumsNeeded = 1,
        locumsAssigned = 1,
        status = RequisitionStatus.FILLED,
        urgency = "Standard",
        broadcastRadiusKm = 20,
        assignedWorkerName = "Dr. M. Johnson",
        assignedWorkerInitials = "MJ",
        notes = "General medical ward rounds & admissions (Booking #BK-83921)."
      ),
      HospitalRequisition(
        id = "REQ-105",
        facilityId = "prac_intercare",
        facilityName = "Intercare Castle Gate",
        department = HospitalDepartment.DENTAL_CLINIC,
        roleNeeded = "Dental Surgeon Locum",
        profession = ProfessionCategory.DENTIST,
        date = "18 Nov 2026 (08:30 - 17:00)",
        shiftType = "Outpatient Shift",
        rateOfferedZar = 3000,
        locumsNeeded = 1,
        locumsAssigned = 0,
        status = RequisitionStatus.OPEN_BROADCASTING,
        urgency = "Standard",
        broadcastRadiusKm = 25,
        notes = "Routine dental restorations, extractions and patient check-ups."
      )
    )
  )
  val hospitalRequisitions: StateFlow<List<HospitalRequisition>> = _hospitalRequisitions.asStateFlow()

  fun createRequisition(
    department: HospitalDepartment,
    roleNeeded: String,
    profession: ProfessionCategory,
    date: String,
    shiftType: String,
    rateOfferedZar: Int,
    urgency: String,
    broadcastRadiusKm: Int,
    notes: String
  ) {
    val newReq = HospitalRequisition(
      id = "REQ-${(100..999).random()}",
      facilityId = myPracticeProfile.id,
      facilityName = myPracticeProfile.name,
      department = department,
      roleNeeded = roleNeeded,
      profession = profession,
      date = date,
      shiftType = shiftType,
      rateOfferedZar = rateOfferedZar,
      status = RequisitionStatus.OPEN_BROADCASTING,
      urgency = urgency,
      broadcastRadiusKm = broadcastRadiusKm,
      notes = notes
    )
    _hospitalRequisitions.value = listOf(newReq) + _hospitalRequisitions.value
    logAudit("REQUISITION_BROADCAST", "Hospital requisition ${newReq.id} broadcast for $roleNeeded (R$rateOfferedZar) in ${department.displayName}")
  }

  fun cancelRequisition(reqId: String) {
    _hospitalRequisitions.value = _hospitalRequisitions.value.map { req ->
      if (req.id == reqId) req.copy(status = RequisitionStatus.CANCELLED) else req
    }
    logAudit("REQUISITION_CANCELLED", "Requisition $reqId was cancelled by Hospital Manager.")
  }

  // --- ADMIN DISPUTE PORTAL STATE & ACTIONS ---
  private val _disputeCases = MutableStateFlow<List<DisputeCase>>(
    listOf(
      DisputeCase(
        id = "DSP-01",
        caseNumber = "DISP-8841",
        bookingId = "BK-91823",
        hospitalName = "Netcare Hospital Sandton",
        practitionerName = "Dr. Zanele Ndlovu",
        practitionerInitials = "ZN",
        practitionerTitle = "Emergency Medical Doctor",
        disputedAmountZar = 2500,
        reasonCategory = "Delayed Shift Arrival & Handover Discrepancy",
        hospitalStatement = "Doctor arrived 45 minutes after shift start (07:45 instead of 07:00), leading to emergency coverage delay in Casualty. Facility requests a 20% rate penalty reduction.",
        practitionerStatement = "Heavy congestion due to severe multi-vehicle collision on N1 Ben Schoeman highway. Provided timestamped highway alert. Worked 50 minutes overtime after 19:00 to complete full patient handover.",
        checkInTimestamp = "07:46 (GPS Verified: Netcare Sandton ER)",
        checkOutTimestamp = "19:52 (Signed off by Sister in Charge)",
        status = DisputeStatus.ACTIVE_INVESTIGATION,
        createdAt = "Yesterday, 14:20"
      ),
      DisputeCase(
        id = "DSP-02",
        caseNumber = "DISP-7720",
        bookingId = "BK-64102",
        hospitalName = "Mediclinic Pretoria",
        practitionerName = "Sister Thandi M.",
        practitionerInitials = "TM",
        practitionerTitle = "Registered Nurse - Neonatal ICU",
        disputedAmountZar = 1200,
        reasonCategory = "Shift Hour Calculation Disagreement",
        hospitalStatement = "Ward register logged 11 hours on duty due to required 1h orientation interval. Shift pay requested for 11h instead of 12h.",
        practitionerStatement = "Orientation was mandatory hospital compliance and counted toward active bedside duty per SANC guidelines. Full 12h attendance rendered.",
        checkInTimestamp = "06:55 (Biometric scan confirmed)",
        checkOutTimestamp = "19:05 (Ward departure)",
        status = DisputeStatus.UNDER_MEDIATION,
        createdAt = "03 Nov 2026, 09:10"
      ),
      DisputeCase(
        id = "DSP-03",
        caseNumber = "DISP-6109",
        bookingId = "BK-55201",
        hospitalName = "Life Healthcare Rosebank",
        practitionerName = "Dr. Pieter Van Zyl",
        practitionerInitials = "PV",
        practitionerTitle = "Anaesthesiologist Specialist",
        disputedAmountZar = 4800,
        reasonCategory = "Emergency Theatre Cancellation by Hospital",
        hospitalStatement = "Emergency theatre was rescheduled after power trip in auxiliary generator. Surgeon cancelled elective slate.",
        practitionerStatement = "Specialist was on site ready in scrub room, turning down another hospital call. Standby fee of 50% requested.",
        checkInTimestamp = "07:30 (Theatre Reception)",
        checkOutTimestamp = "11:30 (Cleared by Clinical Manager)",
        status = DisputeStatus.RESOLVED_SPLIT,
        createdAt = "29 Oct 2026",
        resolutionSummary = "Mediated 50/50 settlement: R2,400 paid to Dr. Van Zyl for reserved theatre standby, R2,400 refunded to Life Healthcare.",
        resolvedAt = "30 Oct 2026, 11:00"
      )
    )
  )
  val disputeCases: StateFlow<List<DisputeCase>> = _disputeCases.asStateFlow()

  fun resolveDisputeReleaseToDoctor(caseId: String, resolutionNote: String) {
    val dispute = _disputeCases.value.firstOrNull { it.id == caseId } ?: return
    _disputeCases.value = _disputeCases.value.map { item ->
      if (item.id == caseId) {
        item.copy(
          status = DisputeStatus.RESOLVED_RELEASED,
          resolutionSummary = resolutionNote,
          resolvedAt = "Resolved Just Now"
        )
      } else item
    }
    // Update repository wallet & booking state
    _walletState.value = _walletState.value.copy(
      escrowPendingZar = maxOf(0.0, _walletState.value.escrowPendingZar - dispute.disputedAmountZar),
      totalEarnedZar = _walletState.value.totalEarnedZar + dispute.disputedAmountZar
    )
    _bookings.value = _bookings.value.map { b ->
      if (b.id == dispute.bookingId) b.copy(status = BookingStatus.FUNDS_RELEASED) else b
    }
    logAudit("DISPUTE_ADMIN_RELEASE", "Admin resolved dispute ${dispute.caseNumber}: R${dispute.disputedAmountZar} released to ${dispute.practitionerName}. Note: $resolutionNote")
  }

  fun resolveDisputeRefundHospital(caseId: String, resolutionNote: String) {
    val dispute = _disputeCases.value.firstOrNull { it.id == caseId } ?: return
    _disputeCases.value = _disputeCases.value.map { item ->
      if (item.id == caseId) {
        item.copy(
          status = DisputeStatus.RESOLVED_REFUNDED,
          resolutionSummary = resolutionNote,
          resolvedAt = "Resolved Just Now"
        )
      } else item
    }
    // Refund escrow to hospital available balance
    _walletState.value = _walletState.value.copy(
      availableBalanceZar = _walletState.value.availableBalanceZar + dispute.disputedAmountZar,
      escrowPendingZar = maxOf(0.0, _walletState.value.escrowPendingZar - dispute.disputedAmountZar)
    )
    _bookings.value = _bookings.value.map { b ->
      if (b.id == dispute.bookingId) b.copy(status = BookingStatus.CANCELLED) else b
    }
    logAudit("DISPUTE_ADMIN_REFUND", "Admin resolved dispute ${dispute.caseNumber}: R${dispute.disputedAmountZar} refunded to ${dispute.hospitalName}. Note: $resolutionNote")
  }

  fun resolveDisputeSplit5050(caseId: String, resolutionNote: String) {
    val dispute = _disputeCases.value.firstOrNull { it.id == caseId } ?: return
    val halfAmount = dispute.disputedAmountZar / 2.0
    _disputeCases.value = _disputeCases.value.map { item ->
      if (item.id == caseId) {
        item.copy(
          status = DisputeStatus.RESOLVED_SPLIT,
          resolutionSummary = "50/50 Split: $resolutionNote (R${"%,.0f".format(halfAmount)} to Doctor / R${"%,.0f".format(halfAmount)} to Hospital)",
          resolvedAt = "Resolved Just Now"
        )
      } else item
    }
    _walletState.value = _walletState.value.copy(
      availableBalanceZar = _walletState.value.availableBalanceZar + halfAmount,
      escrowPendingZar = maxOf(0.0, _walletState.value.escrowPendingZar - dispute.disputedAmountZar),
      totalEarnedZar = _walletState.value.totalEarnedZar + halfAmount
    )
    _bookings.value = _bookings.value.map { b ->
      if (b.id == dispute.bookingId) b.copy(status = BookingStatus.FUNDS_RELEASED) else b
    }
    logAudit("DISPUTE_ADMIN_SPLIT", "Admin resolved dispute ${dispute.caseNumber} with 50/50 split. Note: $resolutionNote")
  }

  private fun logAudit(action: String, details: String) {
    val entry = AuditLogEntry(
      id = "AUD-${(100..999).random()}",
      timestamp = "Just now",
      action = action,
      details = details,
      performedBy = if (_activeRole.value == ActiveRole.HIRER) "Netcare Sandton" else "Dr. L. Mokoena"
    )
    _auditLogs.value = listOf(entry) + _auditLogs.value
  }
}
