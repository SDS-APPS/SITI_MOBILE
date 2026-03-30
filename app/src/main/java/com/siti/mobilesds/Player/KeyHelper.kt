import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class KeyHelper(private val address: String) {

    fun getFirstKey(onKeyChanged : (String) -> Unit) : String{
        val currentTime = LocalDateTime.now()
        val currentDay = currentTime.dayOfMonth.toString()
        val currentMonth = currentTime.month.toString()
        val currentYear =  currentTime.year.toString()
        val currentHour = currentTime.hour.toString()
        val currentMinuteInt = currentTime.minute
        lateinit var currentMinute : String

        if(currentMinuteInt < 9) {
            currentMinute = "00"
        }else{
            val helperMin = currentMinuteInt.toString()
            val newString = helperMin.dropLast(1)
            currentMinute = newString + "0"
        }
        val valueToKey = "$currentDay$currentMonth$currentYear$currentHour$currentMinute$address"
        createScheduleToNewKey(onKeyChanged)
        return getKeyFromString(valueToKey)
    }

    private fun createScheduleToNewKey(onKeyChanged : (String) -> Unit) {
        val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        val initialDelay = calculateInitialDelay()
        val delay = 600L // Intervalo de 10 minutos en segundos

        scheduler.scheduleAtFixedRate(
                {
                    onKeyChanged(getNewKeyGenerated())
                },
                initialDelay,
                delay,
                TimeUnit.SECONDS
        )
    }

    private fun calculateInitialDelay(): Long {
        val currentTime = LocalTime.now()
        val minutes = currentTime.minute
        val seconds = currentTime.second
        val delayMinutes = (10 - minutes % 10) % 10 // Calcula el tiempo hasta el próximo múltiplo de 10 minutos
        val delaySeconds = 60 - seconds // Tiempo restante hasta el próximo minuto
        val tiempoRestate = delayMinutes * 60L + delaySeconds
        println("tiempo restate: ${tiempoRestate - 60}")
        return tiempoRestate - 60
    }

    private fun getKeyFromString(value : String) : String{
        val hash = createHash16Bytes(value)
        return bytesToHexString(hash)
    }

    fun getNewKeyGenerated() : String{
        val currentTime = LocalDateTime.now()
        val currentDay = currentTime.dayOfMonth.toString()
        val currentMonth = currentTime.month.toString()
        val currentYear =  currentTime.year.toString()
        val currentHour = currentTime.hour.toString()
        val currentMinute = currentTime.minute.toString()
        val valueToKey = "$currentDay$currentMonth$currentYear$currentHour$currentMinute$address"
        val hash = createHash16Bytes(valueToKey)
        return bytesToHexString(hash)
    }

    private fun createHash16Bytes(cadena: String): ByteArray {
        val sha256Digest = MessageDigest.getInstance("SHA-256")
        val hashCompleto = sha256Digest.digest(cadena.toByteArray(Charsets.UTF_8))
        val hash16Bytes = ByteArray(16)
        System.arraycopy(hashCompleto, 0, hash16Bytes, 0, 16)

        return hash16Bytes
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

}