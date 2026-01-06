package com.siti.mobile.network.package_expiry

data class PackageExpiryResponse(
    val `data`: List<PackageExpiry>,
    val error: String,
    val message: String,
    val status: String
)