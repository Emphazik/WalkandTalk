package ru.walkAndTalk.ui

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

abstract class Mvi<S : Any, SE : Any>(
    initialState: S
) : ContainerHost<S, SE>, ViewModel() {

    open suspend fun Syntax<S, SE>.onCreate() {}

    abstract val state: Any
    override val container: Container<S, SE> = container(
        initialState = initialState,
        onCreate = { onCreate() }
    )

}