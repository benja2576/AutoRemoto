package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Auto IoT Control", appName)
  }

  @Test
  fun `verify password hashing and verification`() {
    val password = "AutoIoT2026!"
    val salt = CryptoManager.generateSalt()
    val saltedHash = CryptoManager.hashPassword(password, salt)

    assertTrue(CryptoManager.verifyPassword("AutoIoT2026!", saltedHash))
    org.junit.Assert.assertFalse(CryptoManager.verifyPassword("WrongPassword", saltedHash))
  }
}
