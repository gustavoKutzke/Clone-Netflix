package co.tiagoaguiar.netflixremake.util

import android.os.Handler
import android.os.Looper
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.model.MovieDetail
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback :Callback) {



    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback{
        fun onPreExecute()
        fun onResult(movieDetail:MovieDetail)
        fun onFailure(message:String)
    }

    fun execute(url: String) {
        callback.onPreExecute()

        // nesse momento , estamos utilizando a UI-Thread(1)


        executor.execute {
            var urlConnection:HttpsURLConnection?= null
            var buffer :BufferedInputStream? = null
            var stream : InputStream? = null

            try {
                //Nesse momento estamos utilizando a nova-thread(2)
                val requestUrl = URL(url)//Abir uma URl
                urlConnection = requestUrl.openConnection() as HttpsURLConnection // Abrir conexão
                urlConnection.readTimeout = 2000 // tempo leitura (2s)
                urlConnection.connectTimeout = 2000//tempo conexão(2s)

                val statusCode: Int = urlConnection.responseCode

                if(statusCode == 400){
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)

                    val json = JSONObject(jsonAsString)

                    val message = json.getString("message")
                    throw IOException(message)

                    //Aqui eu ja tenho o JSON
                }else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }
                stream = urlConnection.inputStream // sequencia de bytes
                // Primeira forma :
                //val jsonAsString = stream.bufferedReader().use { it.readText() }// bytes --> String

                // segunda forma :
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                val movieDetail = toMovieDetail(jsonAsString)

                handler.post{
                    //aqui  roda dentro da UI thread
                    callback.onResult(movieDetail)
                }


            } catch (e: IOException) {
                val message = e.message ?: "erro desconhecido"
                handler.post {
                    callback.onFailure(message)
                }
            }finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toMovieDetail(jsonAsString:String):MovieDetail{
       val json = JSONObject(jsonAsString)

        val id = json.getInt("id")
        val title = json.getString("title")
        val cast = json.getString("cast")
        val desc = json.getString("desc")
        val coverUrl = json.getString ("cover_url")
        val jsonMovies = json.getJSONArray("movie")

        val similars = mutableListOf<Movie>()
        for(i in 0  until jsonMovies.length()){
            val jsonMovie = jsonMovies.getJSONObject(i)

            val similarId = jsonMovie.getInt("id")
            val similarCoverUrl = jsonMovie.getString("cover_url")

            val m = Movie(similarId,similarCoverUrl)
            similars.add(m)
        }
        val movie = Movie(id,coverUrl,title,cast,desc)
        return MovieDetail(movie,similars)
    }

    private fun toString(stream:InputStream) : String{
        val bytes = ByteArray(1024)
        val baos =  ByteArrayOutputStream()
        var read :Int
        while(true){
            read =stream.read(bytes)
            if(read <= 0){
                break
            }
            baos.write(bytes,0,read)
        }
        return String(baos.toByteArray())
    }
}