package com.trazavoz.domain.model

object Silabeador {

    private fun isBaseVowel(c: Char): Boolean {
        return c == 'A' || c == 'E' || c == 'O' || c == 'I' || c == 'U' ||
               c == 'Á' || c == 'É' || c == 'Ó' || c == 'Í' || c == 'Ú' || c == 'Ü'
    }

    private fun isYVowel(index: Int, word: String): Boolean {
        if (word[index] != 'Y') return false
        val nextIndex = index + 1
        if (nextIndex >= word.length) return true
        val nextChar = word[nextIndex]
        return !isBaseVowel(nextChar) && nextChar != 'H'
    }

    private fun isVowel(index: Int, word: String): Boolean {
        return isBaseVowel(word[index]) || isYVowel(index, word)
    }

    private fun isOpen(c: Char): Boolean {
        return c == 'A' || c == 'E' || c == 'O' || c == 'Á' || c == 'É' || c == 'Ó'
    }

    private fun isClosedAccented(c: Char): Boolean {
        return c == 'Í' || c == 'Ú'
    }

    private fun baseChar(c: Char): Char {
        return when (c) {
            'Á' -> 'A'
            'É' -> 'E'
            'Ó' -> 'O'
            'Í' -> 'I'
            'Ú', 'Ü' -> 'U'
            'Y' -> 'I'
            else -> c
        }
    }

    private fun isDiphthong(v1: Char, v2: Char): Boolean {
        if (baseChar(v1) == baseChar(v2)) return false
        if (isOpen(v1) && isOpen(v2)) return false
        if ((isOpen(v1) && isClosedAccented(v2)) || (isClosedAccented(v1) && isOpen(v2))) return false
        return true
    }

    private fun isTriphthong(v1: Char, v2: Char, v3: Char): Boolean {
        val isClosedV1 = v1 == 'I' || v1 == 'U' || v1 == 'Ü' || v1 == 'Y'
        val isClosedV3 = v3 == 'I' || v3 == 'U' || v3 == 'Ü' || v3 == 'Y'
        return isClosedV1 && isOpen(v2) && isClosedV3 && !isClosedAccented(v1) && !isClosedAccented(v3)
    }

    private fun isInseparable(c1: Char, c2: Char): Boolean {
        val group = "$c1$c2"
        // Para TL se adopta la variante inseparable: A-TLAS, A-TLE-TA.
        return group in listOf(
            "BL", "BR", "CL", "CR", "DR", "FL", "FR", "GL", "GR", "PL", "PR", "TR", "TL",
            "CH", "LL", "RR"
        )
    }

    private data class VowelNucleus(val start: Int, val end: Int)

    fun separar(palabra: String): String {
        val limpia = palabra.trim().uppercase()
        if (limpia.isBlank()) return ""

        val vIndices = mutableListOf<Int>()
        for (i in limpia.indices) {
            if (isVowel(i, limpia)) {
                vIndices.add(i)
            }
        }

        if (vIndices.isEmpty()) return limpia

        val nuclei = mutableListOf<VowelNucleus>()
        var start = vIndices[0]
        var currEnd = start

        for (idx in 1 until vIndices.size) {
            val nextVowel = vIndices[idx]
            val isConsecutive = nextVowel == currEnd + 1 || 
                                (nextVowel == currEnd + 2 && limpia[currEnd + 1] == 'H')

            if (isConsecutive) {
                val len = currEnd - start + 1
                if (len == 1) {
                    val v1 = limpia[currEnd]
                    val v2 = limpia[nextVowel]
                    if (isDiphthong(v1, v2)) {
                        currEnd = nextVowel
                    } else {
                        nuclei.add(VowelNucleus(start, currEnd))
                        start = nextVowel
                        currEnd = nextVowel
                    }
                } else if (len == 2) {
                    val v1 = limpia[start]
                    val v2 = limpia[currEnd]
                    val v3 = limpia[nextVowel]
                    if (isTriphthong(v1, v2, v3)) {
                        currEnd = nextVowel
                    } else {
                        nuclei.add(VowelNucleus(start, currEnd))
                        start = nextVowel
                        currEnd = nextVowel
                    }
                } else {
                    nuclei.add(VowelNucleus(start, currEnd))
                    start = nextVowel
                    currEnd = nextVowel
                }
            } else {
                nuclei.add(VowelNucleus(start, currEnd))
                start = nextVowel
                currEnd = nextVowel
            }
        }
        nuclei.add(VowelNucleus(start, currEnd))

        if (nuclei.size == 1) return limpia

        val splitPoints = mutableListOf<Int>()
        for (idx in 0 until nuclei.size - 1) {
            val n1 = nuclei[idx]
            val n2 = nuclei[idx + 1]
            val k = n2.start - n1.end - 1

            when (k) {
                0 -> {
                    splitPoints.add(n1.end)
                }
                1 -> {
                    splitPoints.add(n1.end)
                }
                2 -> {
                    val p1 = n1.end + 1
                    val p2 = n1.end + 2
                    if (isInseparable(limpia[p1], limpia[p2])) {
                        splitPoints.add(n1.end)
                    } else {
                        splitPoints.add(p1)
                    }
                }
                3 -> {
                    val p1 = n1.end + 1
                    val p2 = n1.end + 2
                    val p3 = n1.end + 3
                    if (isInseparable(limpia[p2], limpia[p3])) {
                        splitPoints.add(p1)
                    } else {
                        splitPoints.add(p2)
                    }
                }
                4 -> {
                    val p1 = n1.end + 1
                    val p2 = n1.end + 2
                    splitPoints.add(p2)
                }
                else -> {
                    splitPoints.add(n1.end + k / 2)
                }
            }
        }

        val resultado = StringBuilder()
        var currentStart = 0
        for (point in splitPoints) {
            resultado.append(limpia.substring(currentStart, point + 1))
            resultado.append("-")
            currentStart = point + 1
        }
        resultado.append(limpia.substring(currentStart))

        return resultado.toString()
    }
}
