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
}
