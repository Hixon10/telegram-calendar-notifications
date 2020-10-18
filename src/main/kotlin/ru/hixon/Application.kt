package ru.hixon

import io.micronaut.runtime.Micronaut.*

public fun main(args: Array<String>) {
	build()
			.args(*args)
			.packages("ru.hixon")
			.start()
}

