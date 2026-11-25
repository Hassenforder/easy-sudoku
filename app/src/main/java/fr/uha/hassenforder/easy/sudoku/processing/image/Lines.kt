package fr.uha.hassenforder.easy.sudoku.processing.image

data class Lines (
    val registry: MutableMap<String, MutableList<Line>> = mutableMapOf()
){
    fun register(lines: List<Line>) : Lines {
        lines.forEach { register(it) }
        return this
    }

    fun register(line: Line) : Lines {
        val lineKey = line.key
        if (!registry.containsKey(lineKey)) {
            registry.put(lineKey, mutableListOf<Line>())
        }
        val keys = registry[lineKey]
        keys!!.add(line)
        return this
    }

    fun getCount(startKey: String): Int {
        if (startKey === "") return registry.size
        val count = registry.keys.count { it.startsWith(startKey) }
        return count
    }

    val horizontalCount: Int
        get() = getCount("H")

    val verticalCount: Int
        get() = getCount("V")

    override fun toString(): String {
        val tmp = StringBuilder()
        for (entry in registry.entries) {
            for (line in entry.value) {
                tmp.append(entry.key)
                tmp.append(" : ")
                tmp.append(line.toString())
                tmp.append("\n")
            }
        }
        return tmp.toString()
    }

    companion object {
        fun merge(vararg linesToMerge: Lines): Lines {
            val result = Lines()
            for (lines in linesToMerge) {
                for (entry in lines.registry.entries) {
                    for (line in entry.value) {
                        result.register(line)
                    }
                }
            }
            return result
        }
    }
}
