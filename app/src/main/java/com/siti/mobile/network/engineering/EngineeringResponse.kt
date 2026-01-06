package com.siti.mobile.network.engineering

data class EngineeringResponse(
    val `data`: List<EngineeringModel>,
    val error: String,
    val message: String,
    val status: String
)