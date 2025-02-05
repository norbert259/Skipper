package com.nksoftware.skipper.coreui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nksoftware.library.composables.NkAppBarButton
import com.nksoftware.skipper.core.ScreenMode
import com.nksoftware.skipper.core.SkipperViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(viewModel: SkipperViewModel, modes: List<ScreenMode>) {
    PrimaryTabRow(
        modifier = Modifier
            .width((modes.size * 50).dp)
            .padding(0.dp),
        selectedTabIndex = modes.indexOf(viewModel.mode),
        divider = { }
    ) {
        if (ScreenMode.Navigation in modes)
            NkAppBarButton(
                onClick = { viewModel.mode = ScreenMode.Navigation },
                icon = Outlined.Directions,
                contentDescription = "Navigation Mode"
            )

        if (ScreenMode.Anchor in modes)
            NkAppBarButton(
                onClick = { viewModel.mode = ScreenMode.Anchor },
                icon = Outlined.Anchor,
                contentDescription = "Anchor Mode"
            )

        if (ScreenMode.Weather in modes)
            NkAppBarButton(
                onClick = { viewModel.mode = ScreenMode.Weather },
                icon = Outlined.Cloud,
                contentDescription = "Weather Mode"
            )

        if (ScreenMode.Grib in modes)
            NkAppBarButton(
                onClick = { viewModel.mode = ScreenMode.Grib },
                icon = Outlined.GridOn,
                contentDescription = "Weather on Sea Mode"
            )

        if (ScreenMode.AstroNavigation in modes)
            NkAppBarButton(
                onClick = { viewModel.mode = ScreenMode.AstroNavigation },
                icon = Outlined.Architecture,
                contentDescription = "Astro Navigation"
            )
    }
}