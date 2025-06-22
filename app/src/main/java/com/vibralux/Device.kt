data class Device(
    val id: String = "",
    val ssid: String = "",
    val status: String = "",
    val last_seen: Long = 0,
    val vibralux: Vibralux = Vibralux()
)

data class Vibralux(
    val lamp_status: Boolean = false,
    val quake_status: Boolean = false,
    val controls: Controls = Controls()
)

data class Controls(
    val mode: String = "",
    val manual_status: Boolean = false,
    val schedule: Schedule = Schedule()
)

data class Schedule(
    val from: Time = Time(),
    val to: Time = Time()
)

data class Time(
    val hour: Int = 0,
    val minute: Int = 0
)
