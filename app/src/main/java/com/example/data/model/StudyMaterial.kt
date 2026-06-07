package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class StudyMaterial(
    @Json(name = "FileID") val fileId: String,
    @Json(name = "FileName") val fileName: String,
    @Json(name = "FileSize") val fileSize: Double,
    @Json(name = "Timestamp") val timestamp: Long,
    @Json(name = "Date") val dateString: String
) : Serializable {

    // dynamic classification for board categories
    fun matchesCategory(category: String): Boolean {
        val nameLower = fileName.lowercase()
        return when (category.lowercase().replace(" ", "").trim()) {
            "class8" -> nameLower.contains("8th") || (nameLower.contains("class 8") && !nameLower.contains("18th") && !nameLower.contains("28th"))
            "class9" -> nameLower.contains("9th") || (nameLower.contains("class 9") && !nameLower.contains("19th") && !nameLower.contains("29th"))
            "class10" -> nameLower.contains("10th") || nameLower.contains("class x") || nameLower.contains("class-x") || nameLower.contains("science class 10")
            "class11" -> nameLower.contains("11th") || nameLower.contains("class xi") || nameLower.contains("class-xi")
            "class12" -> nameLower.contains("12th") || nameLower.contains("class xii") || nameLower.contains("class-xii")
            "jkbose" -> nameLower.contains("jkbose") || nameLower.contains("jkb")
            "cbse" -> nameLower.contains("cbse") || nameLower.contains("padhle akshay")
            "neet" -> nameLower.contains("neet") || nameLower.contains("biology") || nameLower.contains("zoology") || nameLower.contains("botany") || nameLower.contains("respiration") || nameLower.contains("biomolecules") || nameLower.contains("locomotion") || nameLower.contains("flowering plants") || nameLower.contains("frog") || nameLower.contains("macqs")
            "jee" -> nameLower.contains("jee") || nameLower.contains("mains") || nameLower.contains("differentiation") || nameLower.contains("math") || nameLower.contains("kinetic") || nameLower.contains("atomic") || nameLower.contains("electromagnetic")
            "cuet" -> nameLower.contains("cuet")
            "ssc" -> nameLower.contains("ssc")
            "upsc" -> nameLower.contains("upsc") || nameLower.contains("civics") || nameLower.contains("democracy") || nameLower.contains("nationalism") || nameLower.contains("political parties") || nameLower.contains("geography") || nameLower.contains("history") || nameLower.contains("economics")
            "generalknowledge" -> nameLower.contains("gk") || nameLower.contains("general knowledge") || nameLower.contains("gazette") || nameLower.contains("calendar") || nameLower.contains("syllabus") || nameLower.contains("date sheet")
            "currentaffairs" -> nameLower.contains("current affairs") || nameLower.contains("news") || nameLower.contains("affairs") || nameLower.contains("daily")
            else -> false
        }
    }

    // dynamic classification for study material types
    fun matchesMaterialType(type: String): Boolean {
        val nameLower = fileName.lowercase()
        return when (type.lowercase().replace(" ", "").trim()) {
            "notes" -> nameLower.contains("notes") || nameLower.contains("handwritten") || nameLower.contains("summary") || nameLower.contains("mind map")
            "pyqs" -> nameLower.contains("pyp") || nameLower.contains("pyq") || nameLower.contains("previous year") || nameLower.contains("past 10 years") || nameLower.contains("solved") || nameLower.contains("paper")
            "guesspapers" -> nameLower.contains("guess") || nameLower.contains("expected")
            "importantquestions" -> nameLower.contains("important") || nameLower.contains("imp") || nameLower.contains("question bank") || nameLower.contains("miq") || nameLower.contains("expected question")
            "samplepapers" -> nameLower.contains("sample") || nameLower.contains("periodic test") || nameLower.contains("model")
            "books" -> nameLower.contains("book") || nameLower.contains("ncert") || nameLower.contains("textbook")
            "chapterwisenotes" -> nameLower.contains("ch-") || nameLower.contains("chapter") || nameLower.contains("ch.") || nameLower.contains("unit")
            "mcqs" -> nameLower.contains("mcq") || nameLower.contains("mcqs")
            "practicalfiles" -> nameLower.contains("practical")
            "assignments" -> nameLower.contains("assignment") || nameLower.contains("homework")
            else -> false
        }
    }

    fun inferMaterialTypeString(): String {
        return when {
            matchesMaterialType("notes") -> "Notes"
            matchesMaterialType("pyqs") -> "PYQs"
            matchesMaterialType("guesspapers") -> "Guess Paper"
            matchesMaterialType("importantquestions") -> "Important Qs"
            matchesMaterialType("samplepapers") -> "Sample Paper"
            matchesMaterialType("books") -> "Book"
            matchesMaterialType("chapterwisenotes") -> "Chapter Wise Notes"
            matchesMaterialType("mcqs") -> "MCQs"
            matchesMaterialType("practicalfiles") -> "Practical File"
            matchesMaterialType("assignments") -> "Assignment"
            else -> "Study Material"
        }
    }

    fun inferCategoryString(): String {
        return when {
            matchesCategory("class12") -> "Class 12"
            matchesCategory("class11") -> "Class 11"
            matchesCategory("class10") -> "Class 10"
            matchesCategory("class9") -> "Class 9"
            matchesCategory("class8") -> "Class 8"
            matchesCategory("neet") -> "NEET Prep"
            matchesCategory("jee") -> "JEE Prep"
            matchesCategory("jkbose") -> "JKBOSE Board"
            matchesCategory("cbse") -> "CBSE Board"
            matchesCategory("upsc") -> "UPSC Prep"
            matchesCategory("generalknowledge") -> "GK"
            matchesCategory("currentaffairs") -> "Current Affairs"
            else -> "General Study"
        }
    }
}
