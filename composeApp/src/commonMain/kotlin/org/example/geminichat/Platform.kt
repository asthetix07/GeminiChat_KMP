package org.example.geminichat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform