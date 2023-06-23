package co.tiagoaguiar.netflixremake

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.model.MovieDetail
import co.tiagoaguiar.netflixremake.util.DownloadImageTask
import co.tiagoaguiar.netflixremake.util.MovieTask
import java.lang.IllegalStateException

class MovieActivity : AppCompatActivity(),MovieTask.Callback {

    private lateinit var txtTitle :TextView
    private lateinit var txtDesc :TextView
    private lateinit var txtCast:TextView
    private lateinit var adapter:MovieAdapter
    private lateinit var progress:ProgressBar

    private val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)



         txtTitle  = findViewById(R.id.movie_txt_title)
         txtDesc = findViewById(R.id.movie_txt_desc)
         txtCast = findViewById(R.id.movie_txt_cast)
        progress = findViewById(R.id.movie_progress)

         val rv:RecyclerView = findViewById(R.id.movie_rv_simimlar)


        val id =intent?.getIntExtra("id",0) ?:throw IllegalStateException("ID não foi encontrado!")

        val url = "https://api.tiagoaguiar.co/netflixapp/movie/$id?apiKey=7f94b724-ff0b-4485-bde1-96babd46b3f0"

        MovieTask(this).execute(url)





        adapter = MovieAdapter(movies,R.layout.movie_item_similar)
        rv.layoutManager = GridLayoutManager(this,3)
        rv.adapter = adapter



        val toolbar: Toolbar = findViewById(R.id.movie_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null



    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    override fun onFailure(message: String) {
        progress.visibility = View.GONE
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTitle.text = movieDetail.movie.title
        txtDesc.text = movieDetail.movie.desc
        txtCast.text = getString(R.string.cast,movieDetail.movie.cast)

        movies.clear()
        movies.addAll(movieDetail.similar)
        adapter.notifyDataSetChanged()

        DownloadImageTask(object :DownloadImageTask.Callback{
            override fun onResult(bitmap: Bitmap) {
// busquei o desenhavel(layer-list)
                val layerDrawble : LayerDrawable = ContextCompat.getDrawable(this@MovieActivity, R.drawable.shadows) as LayerDrawable

                // busquei o filme que eu quero
                val movieCover = BitmapDrawable(resources,bitmap)

                // atribui a esse layer-list o novo filme
                layerDrawble.setDrawableByLayerId(R.id.cover_drawable,movieCover)

                // set no Imageview
                val coverImg:ImageView = findViewById(R.id.movie_img)
                coverImg.setImageDrawable(layerDrawble)
            }

        }).execute(movieDetail.movie.coverUrl)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}