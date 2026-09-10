package com.semana04


import com.google.firebase.Timestamp
import java.time.LocalDateTime

data class Post(

    val texto : String = "",
    val fecha : Timestamp =  Timestamp.now()
)
