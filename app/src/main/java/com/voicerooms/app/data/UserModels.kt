package com.voicerooms.app.data

enum class UserRole {
    USER,
    HOST,
    AGENT,
    ADMIN,
    SUPER_ADMIN
}

data class AppUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.USER,
    val coins: Int = 0,
    val isVip: Boolean = false,
    val vipLevel: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isAdmin(): Boolean = role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN
    fun isHostOrAbove(): Boolean = role == UserRole.HOST || role == UserRole.AGENT || isAdmin()
    fun isAgentOrAbove(): Boolean = role == UserRole.AGENT || isAdmin()
}

data class GiftPackage(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val priceCoins: Int = 0,
    val priceMoney: Double = 0.0,
    val icon: String = "🎁",
    val forRoles: List<UserRole> = listOf(UserRole.USER),
    val isActive: Boolean = true
)
