package com.example.geofencing.ui.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartLabelVisibilityTest {

    @Test
    fun `does not show a cart label on a selected cart map even at the size threshold`() {
        val visible = shouldShowCartLabel(
            hasSelectedCart = true,
            markerOuterDiameterPx = 30.2f,
            minLabelMarkerDiameterPx = 30.2f
        )

        assertFalse(visible)
    }

    @Test
    fun `shows a cart label on a sector map at the size threshold`() {
        val visible = shouldShowCartLabel(
            hasSelectedCart = false,
            markerOuterDiameterPx = 30.2f,
            minLabelMarkerDiameterPx = 30.2f
        )

        assertTrue(visible)
    }
}
