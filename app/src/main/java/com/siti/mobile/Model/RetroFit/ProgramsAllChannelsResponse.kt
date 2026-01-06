package com.siti.mobile.Model.RetroFit

data class ProgramsAllChannelsResponse(
    val `data`: List<ProgramsAllChannelsModel>,
    val error: String,
    val message: String,
    val status: String
)