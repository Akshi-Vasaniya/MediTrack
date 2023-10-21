package com.example.meditrack.regularExpression

class ListPattern {
    companion object{
        private const val emailRegex = "^[A-Za-z\\d._%+-]+@[A-Za-z\\d.-]+\\.[A-Za-z]{2,4}$"
        private const val nameRegex = "^[A-Za-z]{2,30}$"
        private const val passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,16}$"
        private const val medNameRegex = "^[a-zA-Z][a-zA-Z\\d]{4,}"
        private const val dosageRegex = "^[ a-zA-Z0-9]+"

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

        fun getMedNameRegex():Regex
        {
            return medNameRegex.toRegex()
        }

        fun getDosageRegex():Regex
        {
            return dosageRegex.toRegex()
        }
    }
}