package com.gymhelper.app.ui

object NavRoutes {
    const val Home = "home"
    const val IntervalPrograms = "interval/programs"
    const val IntervalProgram = "interval/program/{programId}"
    const val IntervalDayEdit = "interval/day/{dayId}/edit"
    const val IntervalSession = "interval/session/{dayId}"

    const val WeightPrograms = "weight/programs"
    const val WeightProgram = "weight/program/{programId}"
    const val WeightDayEdit = "weight/day/{dayId}/edit"
    const val WeightSession = "weight/session/{dayId}"

    fun intervalProgram(programId: Long) = "interval/program/$programId"
    fun intervalDayEdit(dayId: Long) = "interval/day/$dayId/edit"
    fun intervalSession(dayId: Long) = "interval/session/$dayId"
    fun weightProgram(programId: Long) = "weight/program/$programId"
    fun weightDayEdit(dayId: Long) = "weight/day/$dayId/edit"
    fun weightSession(dayId: Long) = "weight/session/$dayId"
}
