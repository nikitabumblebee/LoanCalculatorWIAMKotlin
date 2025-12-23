package com.example.loancalculatorwiamkotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loancalculatorwiamkotlin.utils.formatAmount

@Composable
fun ConverterSliderView(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: String,
    valueLabel: String,
    accentColor: Color,
    rangeValue: ClosedFloatingPointRange<Float>,
    step: Float = 1f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = Color(0xFF134252)
            )

            Text(
                text = valueLabel,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = accentColor
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = rangeValue,
            steps = ((rangeValue.endInclusive - rangeValue.start) / step).toInt() - 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rangeValue.start.toDouble().formatAmount(),
                fontSize = 12.sp,
                color = Color(0xFF627C7C)
            )

            Text(
                text = rangeValue.endInclusive.toDouble().formatAmount(),
                fontSize = 12.sp,
                color = Color(0xFF627C7C)
            )
        }
    }
}
