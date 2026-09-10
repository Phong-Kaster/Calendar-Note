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
 * The answer for both current screens is the same: create a note dated today. Creating a note is
 * not a Home-only idea, so there is no reason for the button to change meaning or go grey when the
 * user happens to be in Settings.
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(color = Color.Transparent)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf(BottomBarDestination.Home).forEach { item ->
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

        listOf(BottomBarDestination.Setting).forEach { item ->
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

@Composable
private fun BottomBarElement(
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