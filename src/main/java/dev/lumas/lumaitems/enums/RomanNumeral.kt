package dev.lumas.lumaitems.enums

// https://stackoverflow.com/questions/3921866/how-do-you-find-a-roman-numeral-equivalent-of-an-integer
enum class RomanNumeral(val weight: Int) {
    I(1),
    IV(4),
    V(5),
    IX(9),
    X(10),
    XL(40),
    L(50),
    XC(90),
    C(100),
    CD(400),
    D(500),
    CM(900),
    M(1000);

    companion object {
        fun fromInt(n: Int): String {
            var n = n
            require(n > 0)

            val buf = StringBuilder()

            val values = RomanNumeral.entries.toTypedArray()
            for (i in values.indices.reversed()) {
                while (n >= values[i].weight) {
                    buf.append(values[i])
                    n -= values[i].weight
                }
            }
            return buf.toString()
        }
    }
}
