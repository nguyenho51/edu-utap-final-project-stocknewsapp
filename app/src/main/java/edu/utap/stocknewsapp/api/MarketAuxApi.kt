package edu.utap.stocknewsapp.api

import android.os.SystemClock
import android.text.SpannableString
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

interface MarketAuxApi {
    // Two function prototypes with Retrofit annotations
    // @GET contains a string appended to the base URL
    // the string is called a path name
    @GET("/v1/news/all?api_token=0x9eN1hvQ1ZaGxCb5ZGalLNNzfbfzfPieVe29Y1F"
            +"&filter_entities=true" + "&language=en"
            + "&exchanges=NASDAQ,NYSE,NYSEARCA" + "&countries=us")
    suspend fun getNewsToStart(@Query("symbols") entity: String) : NewsResponse

    @GET("/v1/news/all?api_token=0x9eN1hvQ1ZaGxCb5ZGalLNNzfbfzfPieVe29Y1F"
            +"&filter_entities=true" + "&language=en"
            + "&exchanges=NASDAQ,NYSE,NYSEARCA" + "&countries=us" + "&limit=6")
    suspend fun getNews(@Query("symbols") entity: String) : NewsResponse

    @GET("/v1/entity/search?api_token=0x9eN1hvQ1ZaGxCb5ZGalLNNzfbfzfPieVe29Y1F"
            +"&countries=us" + "&exchanges=NASDAQ,NYSE,NYSEARCA" )
    suspend fun searchEntity(@Query("search") entity: String) : NewsResponse

    data class NewsResponse(val data: List<NewsData>)

    // This class allows Retrofit to parse items in our model of type
    // SpannableString.  Note, given the amount of "work" we do to
    // enable this behavior, one can argue that Retrofit is a bit...."simple."
    class SpannableDeserializer : JsonDeserializer<SpannableString> {
        // @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SpannableString {
            return SpannableString(json.asString)
        }
    }

    companion object {
        // Tell Gson to use our SpannableString deserializer
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder().registerTypeAdapter(
                SpannableString::class.java, SpannableDeserializer()
            )
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        // Keep the base URL simple
        private var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("api.marketaux.com")
            .build()
        fun create(): MarketAuxApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): MarketAuxApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .callTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    // Enable basic HTTP logging to help with debugging.
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .addInterceptor{ chain ->
                    val request = chain.request()
                    //var response = chain.proceed(request)
                    var response: Response? = null
                    var responseOK = false
                    var tryCount = 0
                    while (!responseOK && tryCount < 3) {
                        try {
                            SystemClock.sleep(3000)
                            response = chain.proceed(request)
                            responseOK = response.isSuccessful
                        } catch (e: Exception) {
                            Log.d("intercept", "Request is not successful - $tryCount")
                        } finally {
                            tryCount++
                        }
                    }
                    response!!
                }
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(MarketAuxApi::class.java)
        }
    }
}
