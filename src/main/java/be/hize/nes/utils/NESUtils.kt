package be.hize.nes.utils

import be.hize.nes.utils.renderables.Renderable
import io.github.moulberry.moulconfig.observer.Observer
import io.github.moulberry.moulconfig.observer.Property
import java.util.*

object NESUtils {

    private fun <T> onChange(vararg properties: Property<out T>, observer: Observer<T>) {
        for (property in properties) {
            property.whenChanged { a, b -> observer.observeChange(a, b) }
        }
    }

    fun <T> onToggle(vararg properties: Property<out T>, observer: Runnable) {
       onChange(*properties) { _, _ -> observer.run() }
    }

    fun <T> Property<out T>.afterChange(observer: T.() -> Unit) {
        whenChanged { _, new -> observer(new) }
    }

    fun <E> MutableList<List<E>>.addAsSingletonList(text: E) {
        add(Collections.singletonList(text))
    }

    fun <T> MutableList<List<Any>>.addSelector(
        prefix: String,
        values: Array<T>,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        onChange: (T) -> Unit,
    ) {
        val newList = mutableListOf<Any>()
        newList.add(prefix)
        for (entry in values) {
            val display = getName(entry)
            if (isCurrent(entry)) {
                newList.add("§a[$display]")
            } else {
                newList.add("§e[")
                newList.add(Renderable.link("§e$display") {
                    onChange(entry)
                })
                newList.add("§e]")
            }
            newList.add(" ")
        }
        add(newList)
    }


    fun <T> List<T>.editCopy(function: MutableList<T>.() -> Unit) =
        toMutableList().also { function(it) }.toList()

    fun <T> Property<out T>.onToggle(observer: Runnable) {
        whenChanged { _, _ -> observer.run() }
    }
}