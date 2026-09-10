package com.example.skeleton.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the promise `Theme.kt` makes: the app's dark scheme assigns **every** Material role, and
 * leaves none at Material's default.
 *
 * Why a test and not just a careful read? Two reasons, and both are about the future rather than
 * today.
 *
 * 1. A partly-filled scheme fails in the least helpful way possible. The roles you did set look
 *    right, so the screen you are looking at is fine, and some component you never thought about
 *    picks up a lilac from Material's baseline. Nothing fails — not the build, not lint.
 * 2. Compose keeps adding roles. Material3 1.4 introduces a dozen "fixed" accent roles. On a
 *    version bump those would silently arrive holding Material's defaults, while `Theme.kt`'s KDoc
 *    went on claiming otherwise. This test fails instead, which is the whole point: a promise
 *    nothing can break is not a promise, it is a comment.
 *
 * The trick used below: build a reference scheme from `darkColorScheme()` with no arguments — that
 * is Material's baseline — and assert our scheme differs from it in every role.
 *
 * That trick has one blind spot, and this test found it on first run: a role we assign *on purpose*
 * to the same value Material would have picked is indistinguishable from a role we forgot. Exactly
 * one role is in that position — `scrim`, which we deliberately set to black, and black is also
 * Material's default. So [DELIBERATELY_MATCHES_BASELINE] carves it out of the sweep and
 * [scrimIsBlackOnPurpose] pins its value directly instead. Anything else that lands on a baseline
 * value is treated as forgotten, which is the safe way round.
 *
 * @author Phong-Kaster
 */
class DarkColorSchemeTest {

    /**
     * Every role, paired with the accessor that reads it. Adding a role to `Theme.kt` means adding
     * it here too — and if Compose adds one that nobody assigns, [everyRoleIsAssigned] fails and
     * says which.
     */
    private val roles: Map<String, (ColorScheme) -> Long> = mapOf(
        "primary" to { it.primary.value.toLong() },
        "onPrimary" to { it.onPrimary.value.toLong() },
        "primaryContainer" to { it.primaryContainer.value.toLong() },
        "onPrimaryContainer" to { it.onPrimaryContainer.value.toLong() },
        "inversePrimary" to { it.inversePrimary.value.toLong() },
        "secondary" to { it.secondary.value.toLong() },
        "onSecondary" to { it.onSecondary.value.toLong() },
        "secondaryContainer" to { it.secondaryContainer.value.toLong() },
        "onSecondaryContainer" to { it.onSecondaryContainer.value.toLong() },
        "tertiary" to { it.tertiary.value.toLong() },
        "onTertiary" to { it.onTertiary.value.toLong() },
        "tertiaryContainer" to { it.tertiaryContainer.value.toLong() },
        "onTertiaryContainer" to { it.onTertiaryContainer.value.toLong() },
        "background" to { it.background.value.toLong() },
        "onBackground" to { it.onBackground.value.toLong() },
        "surface" to { it.surface.value.toLong() },
        "onSurface" to { it.onSurface.value.toLong() },
        "surfaceVariant" to { it.surfaceVariant.value.toLong() },
        "onSurfaceVariant" to { it.onSurfaceVariant.value.toLong() },
        "surfaceTint" to { it.surfaceTint.value.toLong() },
        "surfaceDim" to { it.surfaceDim.value.toLong() },
        "surfaceBright" to { it.surfaceBright.value.toLong() },
        "surfaceContainerLowest" to { it.surfaceContainerLowest.value.toLong() },
        "surfaceContainerLow" to { it.surfaceContainerLow.value.toLong() },
        "surfaceContainer" to { it.surfaceContainer.value.toLong() },
        "surfaceContainerHigh" to { it.surfaceContainerHigh.value.toLong() },
        "surfaceContainerHighest" to { it.surfaceContainerHighest.value.toLong() },
        "inverseSurface" to { it.inverseSurface.value.toLong() },
        "inverseOnSurface" to { it.inverseOnSurface.value.toLong() },
        "outline" to { it.outline.value.toLong() },
        "outlineVariant" to { it.outlineVariant.value.toLong() },
        "error" to { it.error.value.toLong() },
        "onError" to { it.onError.value.toLong() },
        "errorContainer" to { it.errorContainer.value.toLong() },
        "onErrorContainer" to { it.onErrorContainer.value.toLong() },
        "scrim" to { it.scrim.value.toLong() },
    )

    /** No role may still be holding the value Material would have given it. */
    @Test
    fun everyRoleIsAssigned() {
        val materialBaseline = darkColorScheme()

        val leftAtDefault = roles
            .filterKeys { name -> name !in DELIBERATELY_MATCHES_BASELINE }
            .filter { (_, read) -> read(DarkColorScheme) == read(materialBaseline) }
            .keys

        assertTrue(
            "These roles are still at Material's default, so a component reaching for one gets a " +
                "colour nobody chose: $leftAtDefault",
            leftAtDefault.isEmpty(),
        )
    }

    /**
     * `scrim` is black by choice. It is excluded from [everyRoleIsAssigned] because black is also
     * Material's default, so this test is what actually holds it in place.
     */
    @Test
    fun scrimIsBlackOnPurpose() {
        assertEquals(0xFF000000, DarkColorScheme.scrim.value.toLong() ushr 32)
    }

    /**
     * Catches the one thing [everyRoleIsAssigned] cannot: a role that Compose *adds*.
     *
     * That sweep only looks at roles named in [roles], so if a Compose upgrade introduces new ones
     * — Material3 1.4 adds a dozen "fixed" accent roles — they would arrive holding Material's
     * defaults and no test would notice, because nothing would be looking for them.
     *
     * `Color` is a value class wrapping a `ULong`, so on the JVM every colour role on `ColorScheme`
     * compiles down to a getter returning `long`. Counting those getters and comparing against
     * [roles] means a version bump that changes the role set fails here, with a message telling you
     * to go update the map. Brittle by design: it is meant to break when the world changes.
     */
    @Test
    fun roleListCoversEveryRoleThisComposeVersionDefines() {
        val colorGetters = ColorScheme::class.java.methods
            .filter { it.parameterCount == 0 && it.returnType == java.lang.Long.TYPE }
            .map { it.name }
            .filter { it.startsWith("get") }
            .distinct()

        assertEquals(
            "ColorScheme exposes ${colorGetters.size} colour roles but this test knows about " +
                "${roles.size}. Compose has changed the role set — add the new roles to " +
                "Theme.kt's DarkColorScheme and to `roles` here. Getters seen: " +
                colorGetters.sorted(),
            colorGetters.size,
            roles.size,
        )
    }

    private companion object {
        /**
         * Roles whose intended value happens to equal Material's baseline. Each one must have its
         * own explicit assertion in this class — keep this set as small as it can possibly be,
         * because every name in it is a role [everyRoleIsAssigned] can no longer protect.
         */
        val DELIBERATELY_MATCHES_BASELINE = setOf("scrim")
    }

    /** `primary` is the blue the product asked for, not merely *a* blue. */
    @Test
    fun primaryIsTheProductBlue() {
        assertEquals(0xFF35A0F5, DarkColorScheme.primary.value.toLong() ushr 32)
    }

    /**
     * The app is dark-only, so the ground is black and text on it is white. If these two ever swap
     * or drift, something has reintroduced a light theme.
     */
    @Test
    fun groundIsBlackAndTextOnItIsWhite() {
        assertEquals(0xFF000000, DarkColorScheme.background.value.toLong() ushr 32)
        assertEquals(0xFFFFFFFF, DarkColorScheme.onBackground.value.toLong() ushr 32)
    }
}
