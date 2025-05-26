package ru.walkAndTalk.ui.orbit

import org.orbitmvi.orbit.ContainerHost

abstract class MviViewModel<STATE : Any, INTENT : Any, SIDE_EFFECT : Any>(initialState: STATE) : ContainerHost<STATE, SIDE_EFFECT> {
    abstract fun reduce(state: STATE, intent: INTENT): STATE
    fun intent(intent: INTENT) = intent { reduce(state, intent) }
}