package org.sazz.logger.outputmethod

class ConsoleLog: LogOutputMethod {
    override fun log(message: String) {
        println(message)
    }
}