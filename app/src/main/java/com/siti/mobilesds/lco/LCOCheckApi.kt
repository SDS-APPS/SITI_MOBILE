import com.siti.mobilesds.lco.LCOCheckRequest
import com.siti.mobilesds.lco.LCOCheckResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LCOCheckApi {
    @Headers("Content-Type: application/json")
    @POST("osmsapi/sdsdrm/lcocheck")
    fun checkLCO(@Body request: LCOCheckRequest): Call<LCOCheckResponse>
}