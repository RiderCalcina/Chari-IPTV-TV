package com.example.chariiptv.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.chariiptv.theme.*
import com.example.chariiptv.util.Constants

@Composable
fun CountrySelectorRow(
    countryKeys: List<String>,
    selectedCountryCode: String,
    countryRowState: LazyListState,
    selectedCountryFocusRequester: FocusRequester,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = countryRowState,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .focusRestorer(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(countryKeys) { code ->
            val name = Constants.SUPPORTED_COUNTRIES[code] ?: code
            val flag = Constants.COUNTRY_FLAGS[code] ?: "🌐"
            val isCountryActive = code == selectedCountryCode

            Surface(
                onClick = { onCountrySelected(code) },
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                border = ClickableSurfaceDefaults.border(
                    border = Border(
                        BorderStroke(
                            1.dp,
                            if (isCountryActive) Color(0x662563EB) else Color(0x22FFFFFF)
                        )
                    ),
                    focusedBorder = Border(BorderStroke(2.5.dp, Color.White))
                ),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isCountryActive) TvPrimary else TvSurfaceContainerHigh,
                    focusedContainerColor = TvPrimaryFocused
                ),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .then(if (isCountryActive) Modifier.focusRequester(selectedCountryFocusRequester) else Modifier)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    androidx.compose.material3.Text(text = flag, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = name,
                        color = Color.White,
                        fontWeight = if (isCountryActive) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
