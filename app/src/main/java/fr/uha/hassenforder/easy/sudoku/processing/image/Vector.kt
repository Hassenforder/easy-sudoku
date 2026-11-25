package fr.uha.hassenforder.easy.sudoku.processing.image

data class Vector(
    val rho: Double,
    val theta: Double
) {
    override fun toString(): String {
        val tmp = StringBuilder()
        tmp.append(rho)
        tmp.append(" / ")
        tmp.append(theta)
        return tmp.toString()
    }
}
