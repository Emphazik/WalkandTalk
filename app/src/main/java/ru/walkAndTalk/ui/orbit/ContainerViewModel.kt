package ru.walkAndTalk.ui.orbit

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container

abstract class ContainerViewModel<S: Any, SE: Any>(
    initialState: S
) : ViewModel(), ContainerHost<S, SE> {

    open suspend fun Syntax<S, SE>.onCreate() {}

    override val container = container(
        initialState = initialState,
        onCreate = { onCreate() }
    )

}