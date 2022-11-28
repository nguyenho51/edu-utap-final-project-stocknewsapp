package edu.utap.stocknewsapp.api

import android.text.SpannableString
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

interface FinnHubApi {
    // function prototypes with Retrofit annotations
    // @GET contains a string appended to the base URL
    // the string is called a path name
    @GET("/api/v1/quote?token=ce20qh2ad3idecbgmsa0ce20qh2ad3idecbgmsag")
    suspend fun getQuote(@Query("symbol") entity: String) : QuoteResponse

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
        var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("finnhub.io")
            .build()
        fun create(): FinnHubApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): FinnHubApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    // Enable basic HTTP logging to help with debugging.
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(FinnHubApi::class.java)
        }
    }
}

