package top.ma6jia.qianzha.enchantnote.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Log4j {
    companion object {
        val <reified T> T.log: Logger
            inline get() = LogManager.getLogger(T::class.java)
    }
}
