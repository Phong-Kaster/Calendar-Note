package com.example.skeleton.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skeleton.R
import com.example.skeleton.core.LocalNavController
import com.example.skeleton.domain.enums.BottomBarDestination
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.NavigationUtil


/**
 * The bottom navigation bar every top-level screen wears: the tabs, plus the round action button
 * in the middle that creates a note.
 *
 * [onCreateNote] has **no default**, and that is deliberate. This component is shared by Home and
 * Settings alike, and a defaulted `= {}` would let a screen host the app's primary create action
 * as a button that does nothing — which is precisely the state this bar was in before the note
 * feature existed. Without a default, adding a third screen forces whoever adds it to decide what
 * the middle button means there.
 *
 * **What it means is each screen's own decision, and two answers are in use.** Home and Settings
 * create a note dated today. **The Calendar screen creates one dated the day the user has picked**
 * — see `CalendarFragment`'s `onCreateNote`, which explains why that screen refuses to have two
 * add affordances filing notes on two different days. Creating a note is not a Home-only idea, so
 * the button never goes grey; but do not assume "today" when adding a fourth screen — read what
 * that screen is for.
 *
 * **The tabs are read from [BottomBarDestination] rather than listed here**, and the list is split
 * down the middle so the action button keeps the true centre of the bar. With an odd number of
 * screens one slot on the right is left empty on purpose: the alternative — dividing the width
 * between the tabs alone — slides the app's primary action off-centre, which reads as a mistake
 * rather than as a layout. Adding a fourth top-level screen fills that slot and needs no change
 * here. The centre "+" is drawn on every screen, Alarms included — Alarms also carries its own
 * floating action button for adding an alarm, and the two are deliberately different actions:
 * this one always writes a note, wherever it is tapped from.
 *
 * @param onCreateNote the user tapped the centre action button.
 * @author Phong-Kaster
 */
@Composable
fun CoreBottomBar(
    onCreateNote: () -> Unit,
) {
    // For navigating to other destinations
    val navController = LocalNavController.current ?: rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // The left half is the larger one when the count is odd, so Home always stays on the left.
    val leftCount = (BottomBarDestination.entries.size + 1) / 2
    val leftDestinations = BottomBarDestination.entries.take(leftCount)
    val rightDestinations = BottomBarDestination.entries.drop(leftCount)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(color = Color.Transparent)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        leftDestinations.forEach { item ->
            BottomBarElement(
                enable = currentDestination?.hierarchy?.any { it.id == item.destinationId } == true,
                drawableId = item.drawableId,
                stringId = item.nameId,
                modifier = Modifier.weight(1f)
            ) {
                if (!NavigationUtil.canNavigate()) return@BottomBarElement

                if (currentDestination?.id != item.homeDestinationId) {
                    navController.navigate(item.directions)
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(48.dp)
                .clip(shape = CircleShape)
                .background(color = MaterialTheme.colorScheme.primary)
                // Debounced like the tabs beside it: this navigates, and two taps arriving inside
                // the same moment would push two copies of the Note screen onto the back stack.
                .clickable(onClick = { if (NavigationUtil.canNavigate()) onCreateNote() }),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                // Not null. The tabs each carry their label, but this button is icon-only, so
                // without a description the app's primary create action is an unnamed button to
                // anybody using a screen reader.
                contentDescription = stringResource(R.string.add_note),
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // The empty slots that keep the action button centred. Placed *before* the remaining tabs
        // so the outermost tab still hugs the right edge, mirroring Home on the left; put them
        // after, and the bar has a hole at its end that reads as a missing button.
        repeat(times = leftCount - rightDestinations.size) {
            Spacer(modifier = Modifier.weight(1f))
        }

        rightDestinations.forEach { item ->
            BottomBarElement(
                enable = currentDestination?.hierarchy?.any { it.id == item.destinationId } == true,
                drawableId = item.drawableId,
                stringId = item.nameId,
                modifier = Modifier.weight(1f)
            ) {
                if (!NavigationUtil.canNavigate()) return@BottomBarElement

                if (currentDestination?.id != item.homeDestinationId) {
                    navController.navigate(item.directions)
                }
            }
        }
    }
}

/**
 * One tab of the bottom bar: an icon, and a label that appears only while that tab is the one you
 * are on.
 *
 * `internal` rather than `private` so a screenshot case can render it with [enable] true. The
 * previews of the whole bar cannot: they have no `NavController`, so every tab there comes out
 * unselected and the label is never composed at all — which meant the one state that can overflow
 * was the one state no picture covered.
 *
 * @param enable true when this is the screen the user is on; decides the tint and whether the
 *   label is drawn.
 * @param drawableId the tab's icon.
 * @param stringId the tab's name, used as the label and as the icon's content description.
 * @param modifier applied to the tab; callers pass a weight so every slot is the same width.
 * @param onClick the user tapped this tab.
 * @author Phong-Kaster
 */
@Composable
internal fun BottomBarElement(
    enable: Boolean,
    @DrawableRes drawableId: Int,
    @StringRes stringId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 35.dp),
                onClick = onClick
            )
            .padding(top = 4.dp, bottom = 12.dp),
    ) {
        // The selected tab is painted in the accent, the others in the ordinary foreground. Before
        // this, both states were drawn in `onBackground` and the *only* signal that a tab was
        // selected was that its text label appeared at all — so on a bar where every label is
        // hidden, or read by somebody who does not know the label only shows when active, the bar
        // never said where you were.
        val tint =
            if (enable) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onBackground

        Icon(
            painter = painterResource(drawableId),
            contentDescription = stringResource(id = stringId),
            modifier = Modifier.size(24.dp),
            tint = tint,
        )

        if (enable) {
            // Still 14sp. A third tab halved every slot — roughly 66dp on a 360dp screen — and
            // the obvious reaction was to shrink the label to fit. That was wrong twice over: it
            // charged every screen and every language a legibility cost to fit one English word,
            // and it did not actually solve the case it was changed for, because no size in the
            // readable range fits German "Einstellungen" in 66dp.
            //
            // The house rule (.claude/jetpack-compose-ui.md § Text) answers overflow with
            // `basicMarquee`, and it is deliberately not used here: this bar is on every screen
            // for the whole life of the app, so a label that scrolls sideways for ever is a
            // permanent distraction rather than a fix. The same deviation, for the same reason,
            // is already made and explained in HomeNoteList's date line.
            //
            // So the long translations ellipsize, and that is a known, recorded defect rather
            // than a decision — see knowledge/ISSUES.md. The reference image below is what makes
            // it visible instead of arguable.
            Text(
                text = stringResource(stringId),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 600,
                ),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}

@Preview
@Composable
private fun PreviewBottomBar() {
    // The bar is transparent and reads its colours from the theme, so it has to be previewed
    // inside the real theme on the real ground colour. A preview on Studio's default white
    // background proves nothing about what a user actually sees.
    MyApplicationTheme(
        content = {
            Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                CoreBottomBar(onCreateNote = {})
            }
        }
    )
}