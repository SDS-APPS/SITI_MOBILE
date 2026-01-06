package com.siti.mobile.network

enum class ProvidersService {
    LIVE_TV,ADVERTISMENT,FINGERPRINT,ENGINEERING, AREA_CODE
}

val defaultProviderServices  = listOf(
    ProvidersService.LIVE_TV,
    ProvidersService.ADVERTISMENT,
    ProvidersService.FINGERPRINT,
    ProvidersService.ENGINEERING,
    ProvidersService.AREA_CODE
)