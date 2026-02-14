package com.example.budgetrecommendation

import android.content.Context
import kotlin.math.round

data class BudgetResult(val category: String, val percent: Double)

object RecommendationEngine {

    private const val RAW_FILE = "raw_responses.csv"

    private var cached: List<Resp>? = null
    private var cachedCategories: List<String>? = null

    private data class Resp(
        val ageBin: String,
        val job: String,
        val gender: String,
        val income: String,
        val marital: String,
        val pct: Map<String, Double>
    )

    fun recommendFromRaw(
        context: Context,
        age: Int,
        job: String,
        gender: String,
        income: String,
        marital: String
    ): List<BudgetResult> {

        val data = cached ?: loadRaw(context).also { cached = it }
        val categories = cachedCategories ?: emptyList()

        val qAge = norm(toAgeBin(age))
        val qJob = norm(job)
        val qGender = norm(gender)
        val qIncome = norm(income)
        val qMarital = norm(marital)

        val levels: List<(Resp) -> Boolean> = listOf(
            { r -> norm(r.ageBin)==qAge && norm(r.job)==qJob && norm(r.gender)==qGender && norm(r.income)==qIncome && norm(r.marital)==qMarital },
            { r -> norm(r.ageBin)==qAge && norm(r.gender)==qGender && norm(r.income)==qIncome && norm(r.marital)==qMarital },
            { r -> norm(r.ageBin)==qAge && norm(r.income)==qIncome && norm(r.marital)==qMarital },
            { r -> norm(r.ageBin)==qAge && norm(r.income)==qIncome },
            { r -> norm(r.ageBin)==qAge }
        )

        var picked: List<Resp> = emptyList()
        for (pred in levels) {
            val m = data.filter(pred)
            if (m.isNotEmpty()) { picked = m; break }
        }
        if (picked.isEmpty()) picked = data

        val avg = averagePct(picked, categories)
        return avg.entries.sortedByDescending { it.value }
            .map { BudgetResult(it.key, round2(it.value)) }
    }

    private fun loadRaw(context: Context): List<Resp> {
        val lines = context.assets.open(RAW_FILE)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }

        val header = splitCsvLine(lines.first()).map { it.trim() }

        fun findIdx(vararg names: String): Int? {
            val set = names.map { normKeepSpace(it) }.toSet()
            val idx = header.indexOfFirst { set.contains(normKeepSpace(it)) }
            return if (idx >= 0) idx else null
        }

        val idxAge = findIdx("Age", "Usia saat ini", "Umur")
        val idxJob = findIdx("Current Job", "Pekerjaan saat ini", "Pekerjaan")
        val idxGender = findIdx("Gender", "Jenis kelamin anda", "Jenis Kelamin", "Jenis kelamin")
        val idxIncome = findIdx("Income", "Rentang penghasilan anda", "Rentang Penghasilan")
        val idxMarital = findIdx("Marital Status", "Status perkawinan saat ini", "Status Perkawinan")

        require(idxAge != null && idxJob != null && idxGender != null && idxIncome != null && idxMarital != null) {
            "CSV header mismatch. Need: Age, Current Job, Gender, Income, Marital Status"
        }

        val inputIdx = setOf(idxAge, idxJob, idxGender, idxIncome, idxMarital)

        fun canonicalCat(name: String): String = name.replace(Regex("\\.\\d+$"), "").trim()

        val catCols = mutableListOf<Pair<Int, String>>()
        val seen = mutableSetOf<String>()
        for (i in header.indices) {
            if (inputIdx.contains(i)) continue
            val cat = canonicalCat(header[i])
            if (cat.isBlank()) continue
            val key = normKeepSpace(cat)
            if (seen.add(key)) catCols.add(i to cat)
        }

        cachedCategories = catCols.map { it.second }

        val out = mutableListOf<Resp>()
        for (li in 1 until lines.size) {
            val cols = splitCsvLine(lines[li])
            if (cols.isEmpty()) continue

            val ageVal = cols.getOrNull(idxAge)?.trim()?.toIntOrNull() ?: continue
            val jobVal = cols.getOrNull(idxJob)?.trim().orEmpty()
            val genderVal = cols.getOrNull(idxGender)?.trim().orEmpty()
            val incomeVal = cols.getOrNull(idxIncome)?.trim().orEmpty()
            val maritalVal = cols.getOrNull(idxMarital)?.trim().orEmpty()

            val rawAlloc = mutableMapOf<String, Double>()
            for ((ci, cat) in catCols) {
                val v = cols.getOrNull(ci)?.trim()
                    ?.replace("Rp", "", ignoreCase = true)
                    ?.replace(".", "")
                    ?.replace(",", "")
                    ?.toDoubleOrNull()
                if (v != null) rawAlloc[cat] = v
            }

            val total = rawAlloc.values.sum()
            if (total <= 0.0) continue

            val pct = rawAlloc.mapValues { (_, v) -> v / total * 100.0 }

            out.add(
                Resp(
                    ageBin = toAgeBin(ageVal),
                    job = jobVal,
                    gender = genderVal,
                    income = incomeVal,
                    marital = maritalVal,
                    pct = pct
                )
            )
        }

        require(out.isNotEmpty()) { "CSV parsed but produced 0 usable rows." }
        return out
    }

    private fun averagePct(rows: List<Resp>, categories: List<String>): Map<String, Double> {
        val sum = mutableMapOf<String, Double>()
        for (c in categories) sum[c] = 0.0

        for (r in rows) for (c in categories) {
            sum[c] = (sum[c] ?: 0.0) + (r.pct[c] ?: 0.0)
        }

        val n = rows.size.toDouble().coerceAtLeast(1.0)
        val avg = sum.mapValues { (_, v) -> v / n }
        return normalizeTo100(avg)
    }

    private fun normalizeTo100(map: Map<String, Double>): Map<String, Double> {
        val s = map.values.sum().takeIf { it > 0.0 } ?: 1.0
        return map.mapValues { (_, v) -> v / s * 100.0 }
    }

    private fun toAgeBin(age: Int): String = when {
        age <= 17 -> "<=17"
        age in 18..20 -> "18-20"
        age in 21..24 -> "21-24"
        age in 25..29 -> "25-29"
        age in 30..39 -> "30-39"
        else -> "40+"
    }

    private fun norm(s: String): String =
        s.trim().lowercase()
            .replace("\u00A0", " ")
            .replace("–", "-")
            .replace("_", "-")
            .replace(" ", "")

    private fun normKeepSpace(s: String): String =
        s.trim().lowercase()
            .replace("\u00A0", " ")
            .replace("–", "-")

    private fun round2(x: Double): Double = round(x * 100.0) / 100.0

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> if (inQuotes) sb.append(ch) else { result.add(sb.toString()); sb.clear() }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }
}
