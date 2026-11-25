package fr.uha.hassenforder.easy.sudoku.processing.image

class LineStraightener {
    /*
        * straighten a line means :
        *    + set start and end at the same value (mean of start and end) according to the orientation
        *    + use the previous value for the other direction
        */
    private fun straight(line: Line): Line? {
        return when (line.orientation) {
            Line.Orientation.VERTICAL -> {
                val x = line.x
                Line(x, x, line.startY, line.endY)
            }
            Line.Orientation.HORIZONTAL -> {
                val y = line.y
                Line(line.startX, line.endX, y, y)
            }
            Line.Orientation.OTHER -> null
        }
    }

    /*
     * straighten all lines in the map
     * warning with this straightener if start and end are far (seriously slanted) the middle can change severely
     * and the key could change...
     */
    fun process(initial: Lines): Lines {
        val selected: MutableList<Line> = mutableListOf()
        for (entry in initial.registry.entries) {
            for (line in entry.value) {
                val straight = straight(line)
                if (straight == null) continue
                selected.add(straight)
            }
        }
        return Lines().register(selected)
    }
}