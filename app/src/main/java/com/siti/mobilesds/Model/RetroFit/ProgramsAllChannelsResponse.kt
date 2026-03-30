package com.siti.mobilesds.Model.RetroFit

data class ProgramsAllChannelsResponse(
    val `data`: List<ProgramsAllChannelsModel>,
    val error: String,
    val message: String,
    val status: String
)