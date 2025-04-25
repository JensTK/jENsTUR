package com.example.avgangstider.entur

data class Quay(
    var customName: String?,
    val id: String,
    val name: String,
    val description: String?,
    val publicCode: String,
    val estimatedCalls: List<EstimatedCall>
) {
    fun getNameString(): String {
        if (description != null) { return "$name ($description)" }
        return name
    }
}
