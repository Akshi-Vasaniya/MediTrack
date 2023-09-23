package com.example.meditrack.regularExpression

class MatchPattern {
    companion object{
        fun String.validate(regex:Regex):Boolean{
            return  regex.matches(this)
        }
    }
}