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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.skeleton.core.LocalNavController
import com.example.skeleton.domain.enums.BottomBarDestination
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.NavigationUtil


@Composable
fun CoreBottomBar() {
    // For navigating to other destinations
    val navController = LocalNavController.current ?: rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showBottomSheet by remember { mutableStateOf(false) }


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
                .background(color = Color(0xFF35A0F5))

                .clickable { showBottomSheet = !showBottomSheet },
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.White
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
        Icon(
            painter = painterResource(drawableId),
            contentDescription = stringResource(id = stringId),
            modifier = Modifier.size(24.dp),
            tint = Color.White,
        )

        if (enable) {
            Text(
                text = stringResource(stringId),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 600,
                ),
                color =  Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}

@Preview
@Composable
fun PreviewBottomBar() {
    CoreBottomBar()
}