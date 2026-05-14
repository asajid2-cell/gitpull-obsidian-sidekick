package dev.gitpull.app.data

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SecureTokenStoreTest {
    @Test
    fun savesLoadsAndClearsTokenOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = SecureTokenStore(context)

        store.clear()
        assertNull(store.load())

        store.save("test-token-value")
        assertEquals("test-token-value", store.load())

        store.clear()
        assertNull(store.load())
    }
}
