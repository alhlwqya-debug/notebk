package com.ahd.notebk.domain.engine

/**
 * Single source of truth for piece-based production calculations.
 * Production amount is always quantity × unit price.
 */
object PieceCalculator {
    fun calculateTotal(quantity: Int, unitPrice: Double): Double {
        require(quantity >= 0) { "quantity must be non-negative" }
        require(unitPrice >= 0.0) { "unitPrice must be non-negative" }
        return quantity.toDouble() * unitPrice
    }
}
