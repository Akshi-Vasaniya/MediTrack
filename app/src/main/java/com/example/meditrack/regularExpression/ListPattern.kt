package com.example.meditrack.regularExpression

class ListPattern {
    companion object{
        const val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$"
        const val nameRegex = "^[A-Za-z]{2,30}$"
        const val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,16}$"

        fun getNameRegex():Regex
        {
            return nameRegex.toRegex()
        }
        fun getPasswordRegex():Regex
        {
            return passwordRegex.toRegex()
        }
        fun getEmailRegex():Regex
        {
            return emailRegex.toRegex()
        }
    }
}