package com.turkcell.lyraapp.ui.profile

import android.content.SharedPreferences
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.profile.MockProfileRepository
import com.turkcell.lyraapp.data.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import sun.misc.Unsafe

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: MockProfileRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProfileRepository()
        themePreferences = fakeThemePreferences()
        authRepository = fakeAuthRepository()
        viewModel = ProfileViewModel(repository, themePreferences, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile happy path fetches profile successfully`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Yukleme durumu false olmali", state.isLoading)
        assertNotNull("ProfileInfo bos olmamali", state.profileInfo)
        assertEquals("Zeynep Kaya", state.profileInfo?.name)
        assertEquals("ZK", state.profileInfo?.initials)
        assertEquals(127, state.profileInfo?.playlistCount)
        assertTrue("Premium olmali", state.profileInfo?.isPremium == true)
    }

    @Test
    fun `onThemeToggle updates theme state correctly`() = runTest {
        viewModel.onIntent(ProfileIntent.OnThemeToggle(isDarkMode = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Tema koyu olmali", state.isDarkMode)
    }

    private fun fakeAuthRepository(): AuthRepository =
        object : AuthRepository {
            override suspend fun requestOtp(phoneNumber: String): Result<Boolean> = Result.success(true)

            override suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean> =
                Result.success(true)

            override suspend fun completeProfile(
                firstName: String,
                lastName: String,
                birthDate: String
            ): Result<Unit> = Result.success(Unit)

            override suspend fun logout(): Result<Unit> = Result.success(Unit)
        }

    private fun fakeThemePreferences(): ThemePreferences {
        val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
            .apply { isAccessible = true }
            .get(null) as Unsafe
        val instance = unsafe.allocateInstance(ThemePreferences::class.java) as ThemePreferences
        val themeFlow = MutableStateFlow<Boolean?>(null)

        ThemePreferences::class.java.getDeclaredField("prefs").apply {
            isAccessible = true
            set(instance, fakeSharedPreferences())
        }
        ThemePreferences::class.java.getDeclaredField("_isDarkModeFlow").apply {
            isAccessible = true
            set(instance, themeFlow)
        }
        ThemePreferences::class.java.getDeclaredField("isDarkModeFlow").apply {
            isAccessible = true
            set(instance, themeFlow)
        }

        return instance
    }

    private fun fakeSharedPreferences(): SharedPreferences {
        val values = mutableMapOf<String, Any>()
        lateinit var editor: SharedPreferences.Editor
        editor = Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "putBoolean" -> {
                    values[args?.get(0) as String] = args[1] as Boolean
                    proxy
                }
                "remove" -> {
                    values.remove(args?.get(0) as String)
                    proxy
                }
                "apply" -> Unit
                "commit" -> true
                else -> defaultValue(method.returnType)
            }
        } as SharedPreferences.Editor

        return Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            when (method.name) {
                "contains" -> values.containsKey(args?.get(0) as String)
                "getBoolean" -> values[args?.get(0) as String] as? Boolean ?: args?.get(1) as Boolean
                "edit" -> editor
                "getAll" -> values.toMap()
                else -> defaultValue(method.returnType)
            }
        } as SharedPreferences
    }

    private fun defaultValue(returnType: Class<*>): Any? =
        when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Void.TYPE -> Unit
            else -> null
        }
}
