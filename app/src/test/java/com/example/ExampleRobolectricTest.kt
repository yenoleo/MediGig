package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MediGigRepository
import com.example.model.ActiveRole
import com.example.model.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MediGig", appName)
  }

  @Test
  fun `test role switching`() {
    MediGigRepository.setRole(ActiveRole.HIRER)
    assertEquals(ActiveRole.HIRER, MediGigRepository.activeRole.value)
    MediGigRepository.toggleRole()
    assertEquals(ActiveRole.FREELANCER, MediGigRepository.activeRole.value)
  }

  @Test
  fun `test freelancer list loaded with South African rates`() {
    val pros = MediGigRepository.freelancers.value
    assertTrue("Should have loaded freelancers", pros.isNotEmpty())
    assertNotNull(pros.firstOrNull { it.initials == "MJ" })
    val nurse = pros.firstOrNull { it.profession.name == "REGISTERED_NURSE" }
    assertNotNull(nurse)
    assertEquals(1200, nurse?.dailyRateZar)
  }

  @Test
  fun `test wallet and booking escrow with conflict prevention`() {
    val pro = MediGigRepository.freelancers.value.first()
    val booked = MediGigRepository.createBooking(
      freelancer = pro,
      startDate = "20 Nov",
      endDate = "21 Nov",
      totalDays = 2,
      timeSlot = "08:00 - 16:00",
      requestedDays = setOf(20, 21)
    )
    assertTrue("Booking should succeed", booked is MediGigRepository.BookingResult.Success)

    // Attempt double booking on overlapping day
    val conflictAttempt = MediGigRepository.createBooking(
      freelancer = pro,
      startDate = "21 Nov",
      endDate = "22 Nov",
      totalDays = 2,
      timeSlot = "08:00 - 16:00",
      requestedDays = setOf(21, 22)
    )
    assertTrue("Should detect double booking conflict", conflictAttempt is MediGigRepository.BookingResult.DateConflict)
  }

  @Test
  fun `test dispute workflow`() {
    val booking = MediGigRepository.bookings.value.first()
    MediGigRepository.raiseDispute(booking.id, "Discrepancy in hours")
    val updated = MediGigRepository.bookings.value.first { it.id == booking.id }
    assertEquals(BookingStatus.DISPUTED, updated.status)
  }

  @Test
  fun `test HPCSA and SANC credential vault live verification`() {
    val vault = MediGigRepository.credentialsVault.value
    assertTrue("Vault should contain seeded credentials", vault.isNotEmpty())

    val hpcsaDoc = vault.firstOrNull { it.council.name == "HPCSA" }
    assertNotNull("Should find HPCSA doctor in vault", hpcsaDoc)
    assertTrue("HPCSA Reg should not be empty", hpcsaDoc!!.registrationNumber.startsWith("MP") || hpcsaDoc.registrationNumber.startsWith("DP"))

    val sancNurse = vault.firstOrNull { it.council.name == "SANC" }
    assertNotNull("Should find SANC nurse in vault", sancNurse)
    assertTrue("SANC Reg should start with SANC", sancNurse!!.registrationNumber.startsWith("SANC"))

    // Test live register validation trigger
    MediGigRepository.triggerLiveRegisterVerification(hpcsaDoc.id)
    val updatedDoc = MediGigRepository.credentialsVault.value.first { it.id == hpcsaDoc.id }
    assertTrue(updatedDoc.lastVerifiedTimestamp.contains("Live Register Validated"))
  }

  @Test
  fun `test hospital requisition creation and broadcast`() {
    val initialCount = MediGigRepository.hospitalRequisitions.value.size
    MediGigRepository.createRequisition(
      department = com.example.model.HospitalDepartment.ICU_CRITICAL_CARE,
      roleNeeded = "Emergency ICU Locum Sister",
      profession = com.example.model.ProfessionCategory.REGISTERED_NURSE,
      date = "Tonight (19:00 - 07:00)",
      shiftType = "12h Night Shift",
      rateOfferedZar = 1600,
      urgency = "Critical",
      broadcastRadiusKm = 30,
      notes = "Ventilator patient coverage needed immediately"
    )
    val newCount = MediGigRepository.hospitalRequisitions.value.size
    assertEquals(initialCount + 1, newCount)

    val latestReq = MediGigRepository.hospitalRequisitions.value.first()
    assertEquals("Emergency ICU Locum Sister", latestReq.roleNeeded)
    assertEquals(com.example.model.RequisitionStatus.OPEN_BROADCASTING, latestReq.status)
  }

  @Test
  fun `test admin dispute portal tribunal resolution`() {
    val activeCase = MediGigRepository.disputeCases.value.firstOrNull { it.status == com.example.model.DisputeStatus.ACTIVE_INVESTIGATION }
    assertNotNull("Should have active dispute case", activeCase)

    // Admin resolves 50/50 split
    MediGigRepository.resolveDisputeSplit5050(activeCase!!.id, "Equitable split agreed after GPS log audit")
    val resolved = MediGigRepository.disputeCases.value.first { it.id == activeCase.id }
    assertEquals(com.example.model.DisputeStatus.RESOLVED_SPLIT, resolved.status)
    assertTrue(resolved.resolutionSummary?.contains("50/50") == true)
  }
}
