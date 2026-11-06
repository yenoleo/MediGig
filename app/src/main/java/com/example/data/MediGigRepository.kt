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
